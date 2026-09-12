package dsl.algorithms.replication;

import dsl.network.NetworkSimulator;
import dsl.services.EventLogService;
import dsl.simulation.SimulationEngine;
import dsl.model.MessageType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Primary → replicas data replication. Supports synchronous (wait for all
 * replicas) and asynchronous (fire-and-forget) modes.
 */
public final class ReplicationManager {

    public enum Mode { SYNC, ASYNC }

    private final NetworkSimulator network;
    private final SimulationEngine engine;
    private final EventLogService log;

    private volatile Mode mode = Mode.SYNC;
    private volatile String primary = "N1";
    private final List<String> replicas = new ArrayList<>();
    private final Map<String, String> store = new LinkedHashMap<>();

    public ReplicationManager(NetworkSimulator network, SimulationEngine engine, EventLogService log) {
        this.network = network;
        this.engine = engine;
        this.log = log;
    }

    public Mode mode()              { return mode; }
    public void setMode(Mode m)     { this.mode = m; }
    public String primary()         { return primary; }
    public void setPrimary(String p) { this.primary = p; }
    public List<String> replicas()  { return List.copyOf(replicas); }
    public Map<String, String> store() { return Map.copyOf(store); }

    public void addReplica(String id) {
        if (!replicas.contains(id)) replicas.add(id);
    }

    public void write(String key, String value) {
        store.put(key, value);
        log.algorithm("REPL", "WRITE " + key + "=" + value + " via " + primary + " [" + mode + "]");

        long delay = mode == Mode.SYNC ? 300 : 900;
        for (String r : replicas) {
            engine.schedule(delay, "replicate " + key, e -> {
                network.send(primary, r, MessageType.REPLICATION, key + "=" + value);
                log.algorithm("REPL", r + " stored " + key + "=" + value);
            });
        }
    }

    public void failPrimary() {
        var node = network.node(primary);
        if (node != null) node.fail();
        log.warn("REPL", "Primary " + primary + " failed");
    }

    public void recoverPrimary() {
        var node = network.node(primary);
        if (node != null) node.recover();
        log.info("REPL", "Primary " + primary + " recovered");
    }
}