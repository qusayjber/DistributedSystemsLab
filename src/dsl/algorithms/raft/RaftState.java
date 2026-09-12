package dsl.algorithms.raft;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RaftState {
    public volatile RaftRole role = RaftRole.FOLLOWER;
    public volatile long currentTerm = 0;
    public volatile String votedFor = null;
    public final List<LogEntry> log = new CopyOnWriteArrayList<>();
    public volatile long commitIndex = 0;
    public volatile int votesReceived = 0;

    public synchronized void reset() {
        role = RaftRole.FOLLOWER;
        currentTerm = 0;
        votedFor = null;
        log.clear();
        commitIndex = 0;
        votesReceived = 0;
    }
}