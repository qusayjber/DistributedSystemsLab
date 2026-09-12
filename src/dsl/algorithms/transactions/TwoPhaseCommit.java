package dsl.algorithms.transactions;

import dsl.model.MessageType;
import dsl.network.NetworkSimulator;
import dsl.services.EventLogService;
import dsl.simulation.SimulationEngine;
import dsl.utils.IdGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class TwoPhaseCommit {

    public enum Phase { INIT, PREPARE, COMMIT, ABORT, DONE }

    private final NetworkSimulator network;
    private final SimulationEngine engine;
    private final EventLogService log;

    private volatile Phase phase = Phase.INIT;
    private volatile String txId;
    private volatile String coordinator;
    private final List<String> participants = new ArrayList<>();
    private final Map<String, Boolean> votes = new LinkedHashMap<>();

    public TwoPhaseCommit(NetworkSimulator network, SimulationEngine engine, EventLogService log) {
        this.network = network;
        this.engine = engine;
        this.log = log;
    }

    public Phase phase()               { return phase; }
    public String txId()               { return txId; }
    public String coordinator()        { return coordinator; }
    public List<String> participants() { return List.copyOf(participants); }
    public Map<String, Boolean> votes() { return Map.copyOf(votes); }

    public void begin(String coordinatorId, List<String> peers) {
        txId = IdGenerator.transaction();
        coordinator = coordinatorId;
        participants.clear();
        participants.addAll(peers);
        votes.clear();
        phase = Phase.PREPARE;

        log.algorithm("2PC", "Transaction " + txId + " begins (coordinator " + coordinatorId + ")");

        for (String p : participants) {
            network.send(coordinatorId, p, MessageType.PREPARE, "tx=" + txId);
        }

        // collect votes after a small delay, then decide
        engine.schedule(1200, "vote collection", e -> decide());
    }

    /** Called by views when a participant decides how to vote. */
    public void vote(String participantId, boolean yes) {
        votes.put(participantId, yes);
        log.algorithm("2PC", participantId + (yes ? " votes YES" : " votes NO"));
    }

    /** Force a specific participant to fail (e.g. simulate crash before voting). */
    public void failParticipant(String id) {
        var n = network.node(id);
        if (n != null) n.fail();
        log.warn("2PC", id + " failed during " + txId);
    }

    private void decide() {
        boolean allYes = !participants.isEmpty()
                && participants.stream().allMatch(p -> Boolean.TRUE.equals(votes.get(p)));
        phase = allYes ? Phase.COMMIT : Phase.ABORT;

        log.algorithm("2PC", txId + " → " + phase);
        for (String p : participants) {
            network.send(coordinator, p,
                    allYes ? MessageType.COMMIT : MessageType.ABORT, "tx=" + txId);
        }
        engine.schedule(500, "finish", e -> phase = Phase.DONE);
    }

    public void reset() {
        phase = Phase.INIT;
        txId = null;
        coordinator = null;
        participants.clear();
        votes.clear();
    }
}