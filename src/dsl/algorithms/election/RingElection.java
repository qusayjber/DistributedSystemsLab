package dsl.algorithms.election;

import dsl.model.Message;
import dsl.model.MessageType;
import dsl.model.Node;
import dsl.network.NetworkSimulator;
import dsl.services.EventLogService;
import dsl.simulation.SimulationEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Ring election. Nodes are arranged in a logical ring (ordered by id).
 * The initiator sends ELECTION around the ring; each node appends its id;
 * when the token returns, the max id is the coordinator and a COORDINATOR
 * message is circulated.
 */
public final class RingElection {

    private final NetworkSimulator network;
    private final SimulationEngine engine;
    private final EventLogService log;
    private volatile String coordinator;

    public RingElection(NetworkSimulator network, SimulationEngine engine, EventLogService log) {
        this.network = network;
        this.engine = engine;
        this.log = log;
    }

    public String coordinator() { return coordinator; }

    public void startElection(String initiatorId) {
        Node n = network.node(initiatorId);
        if (n == null || !n.isOperational()) return;

        log.algorithm("RING", initiatorId + " started ring election");
        List<String> candidates = new ArrayList<>();
        candidates.add(initiatorId);

        String next = nextId(initiatorId);
        if (next == null) {
            announce(initiatorId);
            return;
        }
        network.send(initiatorId, next, MessageType.ELECTION,
                "list=" + String.join(",", candidates));
    }

    public void onMessage(Message m) {
        switch (m.type()) {
            case ELECTION -> {
                String list = m.payload().substring("list=".length());
                log.algorithm("RING", m.destinationId() + " received election list [" + list + "]");
                List<String> candidates = new ArrayList<>(List.of(list.split(",")));
                if (!candidates.contains(m.destinationId())) {
                    candidates.add(m.destinationId());
                }
                String next = nextId(m.destinationId());
                if (next == null || next.equals(candidates.get(0))) {
                    // token came back to initiator
                    String max = candidates.stream().max(String::compareTo).orElse(m.destinationId());
                    announce(max);
                    // circulate coordinator
                    String nb = nextId(max);
                    if (nb != null) network.send(max, nb, MessageType.COORDINATOR, "coordinator=" + max);
                } else {
                    engine.schedule(250, "ring hop", e ->
                            network.send(m.destinationId(), next, MessageType.ELECTION,
                                    "list=" + String.join(",", candidates)));
                }
            }
            case COORDINATOR -> {
                String coord = m.payload().substring("coordinator=".length());
                if (coord.equals(coordinator)) return;
                coordinator = coord;
                Node me = network.node(m.destinationId());
                if (me != null) me.setRole(dsl.model.NodeRole.FOLLOWER);
                log.algorithm("RING", m.destinationId() + " accepted coordinator " + coord);
                String nb = nextId(m.destinationId());
                if (nb != null && !nb.equals(coord)) {
                    network.send(m.destinationId(), nb, MessageType.COORDINATOR,
                            "coordinator=" + coord);
                }
            }
            default -> { }
        }
    }

    private void announce(String id) {
        coordinator = id;
        Node n = network.node(id);
        if (n != null) n.setRole(dsl.model.NodeRole.LEADER);
        log.algorithm("RING", "★ " + id + " is the new COORDINATOR");
    }

    /** Returns the next operational node in id order, wrapping around. */
    private String nextId(String id) {
        List<String> ring = network.nodeList().stream()
                .filter(Node::isOperational)
                .map(Node::id)
                .sorted()
                .toList();
        if (ring.isEmpty()) return null;
        int i = ring.indexOf(id);
        if (i < 0) return ring.get(0);
        return ring.get((i + 1) % ring.size());
    }
}