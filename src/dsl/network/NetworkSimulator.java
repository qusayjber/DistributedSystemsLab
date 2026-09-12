package dsl.network;

import dsl.model.*;
import dsl.services.EventLogService;
import dsl.services.MetricsService;
import dsl.simulation.SimulationEngine;
import dsl.utils.IdGenerator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Owns the topology, the conditions and the message bus. Algorithms send
 * messages through {@link #send(String, String, MessageType, String)}; the bus
 * applies latency / loss / partition rules and schedules delivery.
 */
public final class NetworkSimulator {

    private final Map<String, Node> nodes = new ConcurrentHashMap<>();
    private final Set<Edge> edges = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> partitionGroup = new ConcurrentHashMap<>();
    private final List<Message> inFlight = new CopyOnWriteArrayList<>();
    private final List<Consumer<Message>> deliveryListeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> topologyListeners = new CopyOnWriteArrayList<>();

    private final SimulationEngine engine;
    private final EventLogService log;
    private final MetricsService metrics;
    private final NetworkConditions conditions = new NetworkConditions();

    public NetworkSimulator(SimulationEngine engine, EventLogService log, MetricsService metrics) {
        this.engine = engine;
        this.log = log;
        this.metrics = metrics;
    }

    /* ------------------------------- topology ------------------------------- */

    public Node addNode(String id, String address) {
        Node n = new Node(id, address);
        nodes.put(id, n);
        metrics.setTotalNodes(nodes.size());
        topologyChanged();
        log.info("NETWORK", "Node " + id + " added (" + address + ")");
        return n;
    }

    public void removeNode(String id) {
        Node n = nodes.remove(id);
        if (n == null) return;
        edges.removeIf(e -> e.touches(id));
        partitionGroup.remove(id);
        metrics.setTotalNodes(nodes.size());
        topologyChanged();
        log.warn("NETWORK", "Node " + id + " removed");
    }

    public Node node(String id) { return nodes.get(id); }

    public Collection<Node> nodes() { return nodes.values(); }

    public List<Node> nodeList() {
        List<Node> list = new ArrayList<>(nodes.values());
        list.sort(Comparator.comparing(Node::id));
        return list;
    }

    public Set<Edge> edges() { return Collections.unmodifiableSet(edges); }

    public boolean connect(String a, String b) {
        if (a.equals(b) || !nodes.containsKey(a) || !nodes.containsKey(b)) return false;
        Edge e = Edge.of(a, b);
        if (!edges.add(e)) return false;
        nodes.get(a).addNeighbor(b);
        nodes.get(b).addNeighbor(a);
        topologyChanged();
        log.info("NETWORK", "Connection established " + a + " ↔ " + b);
        return true;
    }

    public boolean disconnect(String a, String b) {
        Edge e = Edge.of(a, b);
        if (!edges.remove(e)) return false;
        Node na = nodes.get(a), nb = nodes.get(b);
        if (na != null) na.removeNeighbor(b);
        if (nb != null) nb.removeNeighbor(a);
        topologyChanged();
        log.info("NETWORK", "Connection removed " + a + " ↔ " + b);
        return true;
    }

    public boolean areConnected(String a, String b) {
        return edges.contains(Edge.of(a, b));
    }

    /* ------------------------------- messaging ------------------------------ */

    public Message send(String sourceId, String destinationId, MessageType type, String payload) {
        Node source = nodes.get(sourceId);
        if (source == null) return null;

        source.getVectorClock().increment(sourceId);
        Message m = new Message(
                IdGenerator.message(),
                sourceId,
                destinationId,
                type,
                payload,
                source.getLamportClock().send(),
                source.getVectorClock().snapshot());

        dispatch(m);
        return m;
    }

    public List<Message> broadcast(String sourceId, MessageType type, String payload) {
        Node source = nodes.get(sourceId);
        if (source == null) return List.of();
        List<Message> sent = new ArrayList<>();
        for (String neighbor : new ArrayList<>(source.neighbors())) {
            Message m = send(sourceId, neighbor, type, payload);
            if (m != null) sent.add(m);
        }
        return sent;
    }

    public void dispatch(Message m) {
        Node source = nodes.get(m.sourceId());
        Node target = nodes.get(m.destinationId());

        if (source == null || target == null) {
            m.setStatus(MessageStatus.FAILED);
            return;
        }
        if (!source.isOperational() || !target.isOperational()) {
            m.setStatus(MessageStatus.FAILED);
            metrics.incrementMessagesLost();
            log.warn("NETWORK", "Dropped " + m + " (node not operational)");
            return;
        }
        if (!edges.contains(Edge.of(m.sourceId(), m.destinationId()))) {
            m.setStatus(MessageStatus.FAILED);
            metrics.incrementMessagesLost();
            log.warn("NETWORK", "Dropped " + m + " (no link)");
            return;
        }
        if (isPartitioned(m.sourceId(), m.destinationId())) {
            m.setStatus(MessageStatus.FAILED);
            metrics.incrementMessagesLost();
            log.warn("NETWORK", "Blocked by partition: " + m);
            return;
        }
        if (conditions.shouldDrop()) {
            m.setStatus(MessageStatus.LOST);
            metrics.incrementMessagesLost();
            log.warn("NETWORK", "Packet lost: " + m);
            return;
        }

        source.incrementSent();
        metrics.incrementMessagesSent();

        long latency = conditions.sampleLatency();
        m.setDepartureTime(engine.currentTime());
        m.setArrivalTime(engine.currentTime() + latency);
        m.setStatus(MessageStatus.IN_FLIGHT);
        inFlight.add(m);

        engine.schedule(latency, m.toString(), e -> deliver(m));

        if (conditions.shouldDuplicate()) {
            long extra = conditions.sampleLatency() + 20;
            Message copy = new Message(m.id() + "-dup", m.sourceId(), m.destinationId(),
                    m.type(), m.payload(), m.logicalTimestamp(), m.vector());
            copy.markDuplicate();
            copy.setDepartureTime(engine.currentTime());
            copy.setArrivalTime(engine.currentTime() + extra);
            copy.setStatus(MessageStatus.IN_FLIGHT);
            inFlight.add(copy);
            engine.schedule(extra, "duplicate " + copy, e -> deliver(copy));
        }
    }

    private void deliver(Message m) {
        inFlight.remove(m);
        if (m.status() != MessageStatus.IN_FLIGHT) return;

        Node target = nodes.get(m.destinationId());
        if (target == null || !target.isOperational()) {
            m.setStatus(MessageStatus.FAILED);
            metrics.incrementMessagesLost();
            return;
        }

        m.setStatus(MessageStatus.DELIVERED);
        target.incrementReceived();
        target.enqueue(m);
        target.getLamportClock().receive(m.logicalTimestamp());
        target.getVectorClock().merge(target.id(), m.vector());

        metrics.incrementMessagesReceived();
        metrics.recordLatency(m.latency());

        if (!m.duplicate()) {
            log.info("NETWORK", m.sourceId() + " → " + m.destinationId() + " : " + m.type());
        }
        deliveryListeners.forEach(l -> l.accept(m));
    }

    /* ------------------------------- partition ------------------------------ */

    public void partition(Collection<String> groupA, Collection<String> groupB) {
        partitionGroup.clear();
        groupA.forEach(id -> partitionGroup.put(id, 1));
        groupB.forEach(id -> partitionGroup.put(id, 2));
        log.warn("NETWORK", "Network partition created (" + groupA.size() + " | " + groupB.size() + ")");
        topologyChanged();
    }

    public void healPartition() {
        partitionGroup.clear();
        log.info("NETWORK", "Network partition healed");
        topologyChanged();
    }

    public boolean isPartitioned() { return !partitionGroup.isEmpty(); }

    public int groupOf(String nodeId) { return partitionGroup.getOrDefault(nodeId, 0); }

    private boolean isPartitioned(String a, String b) {
        return groupOf(a) != groupOf(b);
    }

    /* ------------------------------- utilities ------------------------------ */

    public List<Message> inFlight() { return inFlight; }

    public NetworkConditions conditions() { return conditions; }

    public void onDelivery(Consumer<Message> listener) { deliveryListeners.add(listener); }

    public void onTopologyChanged(Runnable listener) { topologyListeners.add(listener); }

    private void topologyChanged() {
        topologyListeners.forEach(Runnable::run);
    }

    public void reset() {
        inFlight.clear();
        partitionGroup.clear();
        for (Node n : nodes.values()) {
            n.getLamportClock().reset();
            n.getVectorClock().reset();
            n.clearNeighbors();
        }
        edges.clear();
        topologyChanged();
    }
}