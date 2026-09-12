package dsl.algorithms.election;

import dsl.model.Message;
import dsl.model.MessageType;
import dsl.model.Node;
import dsl.network.NetworkSimulator;
import dsl.services.EventLogService;
import dsl.simulation.SimulationEngine;

import java.util.Comparator;
import java.util.List;

public final class BullyElection {

    private final NetworkSimulator network;
    private final SimulationEngine engine;
    private final EventLogService log;
    private volatile String coordinator;

    public BullyElection(NetworkSimulator network, SimulationEngine engine, EventLogService log) {
        this.network = network;
        this.engine = engine;
        this.log = log;
    }

    public String coordinator() { return coordinator; }

    /** Starts an election from the given node using the Bully algorithm. */
    public void startElection(String initiatorId) {
        Node initiator = network.node(initiatorId);
        if (initiator == null || !initiator.isOperational()) return;

        log.algorithm("BULLY", initiatorId + " started election");

        // send ELECTION to all higher-ID nodes
        boolean higherExists = false;
        for (Node n : network.nodeList()) {
            if (n.id().compareTo(initiatorId) > 0 && n.isOperational()
                    && network.areConnected(initiatorId, n.id())) {
                network.send(initiatorId, n.id(), MessageType.ELECTION, "from=" + initiatorId);
                higherExists = true;
            }
        }

        if (!higherExists) {
            announce(initiatorId);
        } else {
            engine.schedule(800, "election timeout", e -> {
                if (coordinator == null || !coordinator.equals(initiatorId)) {
                    log.algorithm("BULLY", initiatorId + " got no OK, declaring itself coordinator");
                    announce(initiatorId);
                }
            });
        }
    }

    public void onMessage(Message m) {
        switch (m.type()) {
            case ELECTION -> {
                Node me = network.node(m.destinationId());
                if (me == null || !me.isOperational()) return;
                log.algorithm("BULLY", m.destinationId() + " got ELECTION, sends OK");
                network.send(m.destinationId(), m.sourceId(), MessageType.RESPONSE, "OK");
                // this node now holds its own election
                engine.schedule(200, "escalate", e -> startElection(m.destinationId()));
            }
            case COORDINATOR -> {
                coordinator = m.sourceId();
                log.algorithm("BULLY", m.destinationId() + " acknowledges coordinator " + coordinator);
            }
            default -> { }
        }
    }

    private void announce(String id) {
        coordinator = id;
        Node n = network.node(id);
        if (n != null) n.setRole(dsl.model.NodeRole.LEADER);
        log.algorithm("BULLY", "★ " + id + " is the new COORDINATOR");
        for (Node other : network.nodeList()) {
            if (!other.id().equals(id) && other.isOperational()
                    && network.areConnected(id, other.id())) {
                network.send(id, other.id(), MessageType.COORDINATOR, "coordinator=" + id);
            }
        }
    }

    public List<String> higherNodes(String id) {
        return network.nodeList().stream()
                .map(Node::id)
                .filter(s -> s.compareTo(id) > 0)
                .sorted(Comparator.naturalOrder())
                .toList();
    }
}