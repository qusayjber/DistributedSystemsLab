package dsl.algorithms.clocks;

import dsl.model.Node;
import dsl.network.NetworkSimulator;
import dsl.services.EventLogService;
import dsl.simulation.SimulationEngine;
import dsl.model.MessageType;

public final class LamportLab {

    private final NetworkSimulator network;
    private final SimulationEngine engine;
    private final EventLogService log;

    public LamportLab(NetworkSimulator network, SimulationEngine engine, EventLogService log) {
        this.network = network;
        this.engine = engine;
        this.log = log;
    }

    public void localEvent(String nodeId) {
        Node n = network.node(nodeId);
        if (n == null) return;
        long t = n.getLamportClock().tick();
        log.algorithm("LAMPORT", nodeId + " local event → L=" + t);
    }

    public void sendTick(String from, String to) {
        network.send(from, to, MessageType.PING, "tick");
    }

    public void chainA(String a, String b, String c) {
        engine.schedule(0,   "A→B", e -> network.send(a, b, MessageType.PING, "e1"));
        engine.schedule(600, "B→C", e -> network.send(b, c, MessageType.PING, "e2"));
    }
}