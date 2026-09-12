package dsl.algorithms.raft;

import dsl.model.Message;
import dsl.model.MessageType;
import dsl.model.Node;
import dsl.network.NetworkSimulator;
import dsl.services.EventLogService;
import dsl.simulation.SimulationEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Functional Raft cluster: election timeouts, RequestVote, AppendEntries
 * (as heartbeats), terms, log replication and commit index.
 *
 * <p>The cluster only talks to {@link NetworkSimulator}; every decision is
 * logged through {@link EventLogService}.
 */
public final class RaftCluster {

    private final NetworkSimulator network;
    private final SimulationEngine engine;
    private final EventLogService log;

    private final List<String> ids = new ArrayList<>();
    private final Map<String, RaftState> states = new ConcurrentHashMap<>();

    private volatile String leaderId;
    private volatile long term;
    private volatile boolean running;

    public RaftCluster(NetworkSimulator network, SimulationEngine engine, EventLogService log) {
        this.network = network;
        this.engine = engine;
        this.log = log;
    }

    public void register(String nodeId) {
        ids.add(nodeId);
        states.put(nodeId, new RaftState());
    }

    public List<String> nodeIds()      { return List.copyOf(ids); }
    public long term()                 { return term; }
    public String leaderId()           { return leaderId; }
    public boolean isRunning()         { return running; }

    public RaftState stateOf(String id) { return states.get(id); }

    public int quorum() { return ids.size() / 2 + 1; }

    /* ---------------------------------------------------------------------- */

    public void start() {
        if (running) return;
        running = true;
        log.algorithm("RAFT", "Cluster started (" + ids.size() + " nodes, quorum=" + quorum() + ")");
        for (String id : ids) scheduleElection(id, randomTimeout());
    }

    public void stop() {
        running = false;
        log.algorithm("RAFT", "Cluster paused");
    }

    public void reset() {
        stop();
        leaderId = null;
        term = 0;
        states.values().forEach(RaftState::reset);
    }

    public void failLeader() {
        if (leaderId == null) return;
        Node n = network.node(leaderId);
        if (n != null) {
            n.fail();
            log.warn("RAFT", "Leader " + leaderId + " failed");
        }
        states.get(leaderId).role = RaftRole.FOLLOWER;
        states.get(leaderId).votesReceived = 0;
        leaderId = null;
        for (String id : ids) scheduleElection(id, randomTimeout());
    }

    public void recover(String id) {
        Node n = network.node(id);
        if (n != null) {
            n.recover();
            log.info("RAFT", "Node " + id + " recovered");
            scheduleElection(id, randomTimeout());
        }
    }

    public void append(String command) {
        if (leaderId == null) {
            log.warn("RAFT", "No leader — cannot append");
            return;
        }
        RaftState leader = states.get(leaderId);
        long index = leader.log.size() + 1L;
        LogEntry entry = new LogEntry(index, leader.currentTerm, command);
        leader.log.add(entry);
        log.algorithm("RAFT", "Leader " + leaderId + " appended [" + index + "] " + command);

        int acks = 1;
        for (String peer : ids) {
            if (peer.equals(leaderId)) continue;
            network.send(leaderId, peer, MessageType.APPEND_ENTRIES,
                    "index=" + index + ";term=" + entry.term() + ";cmd=" + command);
        }

        // simulate delayed commit — in a real Raft this awaits responses
        engine.schedule(500, "commit check", e -> {
            if (acks >= quorum() - 1 || ids.size() <= 1) {
                leader.commitIndex = index;
                log.algorithm("RAFT", "Committed [" + index + "] at term " + leader.currentTerm);
            }
        });
    }

    /* ---------------------------------------------------------------------- */

    public void onMessage(Message m) {
        RaftState target = states.get(m.destinationId());
        if (target == null) return;

        switch (m.type()) {
            case VOTE_REQUEST   -> handleVoteRequest(m, target);
            case VOTE_RESPONSE  -> handleVoteResponse(m, target);
            case APPEND_ENTRIES -> handleAppend(m, target);
            case HEARTBEAT      -> handleHeartbeat(m, target);
            default             -> { /* ignore */ }
        }
    }

    private void handleVoteRequest(Message m, RaftState target) {
        long candidateTerm = parseTerm(m.payload());
        if (candidateTerm > target.currentTerm) {
            target.currentTerm = candidateTerm;
            target.role = RaftRole.FOLLOWER;
            target.votedFor = null;
        }
        boolean grant = candidateTerm >= target.currentTerm
                && (target.votedFor == null || target.votedFor.equals(m.sourceId()));
        if (grant) target.votedFor = m.sourceId();
        network.send(m.destinationId(), m.sourceId(), MessageType.VOTE_RESPONSE,
                "term=" + target.currentTerm + ";grant=" + grant);
        log.algorithm("RAFT", m.destinationId() + (grant ? " voted for " : " denied ") + m.sourceId());
    }

    private void handleVoteResponse(Message m, RaftState target) {
        if (target.role != RaftRole.CANDIDATE) return;
        boolean granted = m.payload().contains("grant=true");
        if (!granted) return;
        target.votesReceived++;
        if (target.votesReceived >= quorum()) becomeLeader(m.destinationId(), target);
    }

    private void handleAppend(Message m, RaftState target) {
        long srcTerm = parseTerm(m.payload());
        if (srcTerm >= target.currentTerm) {
            target.currentTerm = srcTerm;
            target.role = RaftRole.FOLLOWER;
            target.votedFor = null;
            String cmd = extract(m.payload(), "cmd=");
            long index = parseLong(extract(m.payload(), "index="));
            if (index == target.log.size() + 1L) {
                target.log.add(new LogEntry(index, srcTerm, cmd));
                target.commitIndex = index;
            }
            leaderId = m.sourceId();
            scheduleElection(m.destinationId(), randomTimeout());
            // ack by heartbeat
            network.send(m.destinationId(), m.sourceId(), MessageType.HEARTBEAT, "term=" + srcTerm);
        }
    }

    private void handleHeartbeat(Message m, RaftState target) {
        long srcTerm = parseTerm(m.payload());
        if (srcTerm >= target.currentTerm) {
            target.currentTerm = srcTerm;
            target.role = RaftRole.FOLLOWER;
            leaderId = m.sourceId();
            scheduleElection(m.destinationId(), randomTimeout());
        }
    }

    /* ---------------------------------------------------------------------- */

    private void scheduleElection(String nodeId, long delayMs) {
        engine.schedule(delayMs, nodeId + " election timeout", e -> {
            if (!running) return;
            Node n = network.node(nodeId);
            if (n == null || !n.isOperational()) return;
            RaftState st = states.get(nodeId);
            if (st == null || st.role == RaftRole.LEADER) return;
            startElection(nodeId, st);
        });
    }

    private void startElection(String nodeId, RaftState st) {
        st.role = RaftRole.CANDIDATE;
        st.currentTerm++;
        st.votedFor = nodeId;
        st.votesReceived = 1;
        term = st.currentTerm;

        log.algorithm("RAFT", nodeId + " started election (term " + st.currentTerm + ")");
        for (String peer : ids) {
            if (peer.equals(nodeId)) continue;
            network.send(nodeId, peer, MessageType.VOTE_REQUEST, "term=" + st.currentTerm);
        }
        // if majority is single node (or already won by self)
        if (st.votesReceived >= quorum()) becomeLeader(nodeId, st);
        scheduleElection(nodeId, randomTimeout());
    }

    private void becomeLeader(String nodeId, RaftState st) {
        if (st.role == RaftRole.LEADER) return;
        st.role = RaftRole.LEADER;
        leaderId = nodeId;
        log.algorithm("RAFT", "★ " + nodeId + " became LEADER (term " + st.currentTerm + ")");

        // heartbeat loop
        engine.schedule(200, "heartbeats", e -> heartbeatLoop(nodeId, st));
    }

    private void heartbeatLoop(String nodeId, RaftState st) {
        if (!running || st.role != RaftRole.LEADER) return;
        for (String peer : ids) {
            if (peer.equals(nodeId)) continue;
            network.send(nodeId, peer, MessageType.HEARTBEAT, "term=" + st.currentTerm);
        }
        engine.schedule(400, "heartbeats", e -> heartbeatLoop(nodeId, st));
    }

    /* ---------------------------------------------------------------------- */

    private long randomTimeout() {
        return ThreadLocalRandom.current().nextLong(900, 1800);
    }

    private static long parseTerm(String payload) {
        return parseLong(extract(payload, "term="));
    }

    private static long parseLong(String s) {
        try { return Long.parseLong(s.trim()); } catch (Exception e) { return 0; }
    }

    private static String extract(String payload, String key) {
        int i = payload.indexOf(key);
        if (i < 0) return "";
        int start = i + key.length();
        int end = payload.indexOf(';', start);
        return end < 0 ? payload.substring(start) : payload.substring(start, end);
    }
}