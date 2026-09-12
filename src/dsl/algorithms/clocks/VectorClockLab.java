package dsl.algorithms.clocks;

import dsl.model.CausalRelation;
import dsl.model.Node;
import dsl.model.VectorClock;
import dsl.network.NetworkSimulator;
import dsl.services.EventLogService;
import dsl.simulation.SimulationEngine;
import dsl.model.MessageType;

import java.util.Map;

public final class VectorClockLab {

    private final NetworkSimulator network;
    private final SimulationEngine engine;
    private final EventLogService log;

    public VectorClockLab(NetworkSimulator network, SimulationEngine engine, EventLogService log) {
        this.network = network;
        this.engine = engine;
        this.log = log;
    }

    public CausalRelation compare(String aId, String bId) {
        Node a = network.node(aId);
        Node b = network.node(bId);
        if (a == null || b == null) return CausalRelation.EQUAL;
        Map<String, Integer> va = a.getVectorClock().snapshot();
        Map<String, Integer> vb = b.getVectorClock().snapshot();
        CausalRelation rel = VectorClock.compare(va, vb);
        log.algorithm("VECTOR", aId + " vs " + bId + " → " + rel);
        return rel;
    }

    public void demonstrateConcurrent(String a, String b, String c) {
        engine.schedule(0,   "A→B", e -> network.send(a, b, MessageType.PING, "evt"));
        engine.schedule(0,   "A→C", e -> network.send(a, c, MessageType.PING, "evt"));
        engine.schedule(700, "C→B", e -> network.send(c, b, MessageType.PING, "evt"));
    }
}