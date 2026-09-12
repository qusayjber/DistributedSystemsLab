package dsl.model;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Node {

    private final String id;
    private final String address;

    private volatile NodeState state = NodeState.ONLINE;
    private volatile NodeRole role  = NodeRole.NONE;

    private final Set<String> neighbors = ConcurrentHashMap.newKeySet();
    private final Deque<Message> inbox  = new ArrayDeque<>();

    private final LamportClock lamportClock = new LamportClock();
    private final VectorClock  vectorClock  = new VectorClock();

    private final AtomicLong sent     = new AtomicLong();
    private final AtomicLong received = new AtomicLong();

    private long onlineSince = System.currentTimeMillis();
    private long accumulatedUptime;
    private long restartCount;

    /* canvas position */
    private double x;
    private double y;

    public Node(String id, String address) {
        this.id = id;
        this.address = address;
    }

    public String id()      { return id; }
    public String address() { return address; }

    public NodeState getState()          { return state; }
    public void setState(NodeState s)    { this.state = s; }

    public NodeRole getRole()            { return role; }
    public void setRole(NodeRole r)      { this.role = r; }

    public Set<String> neighbors()       { return Collections.unmodifiableSet(neighbors); }
    public void addNeighbor(String n)    { neighbors.add(n); }
    public void removeNeighbor(String n) { neighbors.remove(n); }
    public void clearNeighbors()         { neighbors.clear(); }

    public LamportClock getLamportClock() { return lamportClock; }
    public VectorClock  getVectorClock()  { return vectorClock; }

    public long sentCount()     { return sent.get(); }
    public long receivedCount() { return received.get(); }

    public void incrementSent()     { sent.incrementAndGet(); }
    public void incrementReceived() { received.incrementAndGet(); }

    public void enqueue(Message m) {
        synchronized (inbox) {
            inbox.addLast(m);
            while (inbox.size() > 200) inbox.pollFirst();
        }
    }

    public Deque<Message> inboxSnapshot() {
        synchronized (inbox) {
            return new ArrayDeque<>(inbox);
        }
    }

    public boolean isOperational() {
        return state.isOperational();
    }

    public void start() {
        state = NodeState.ONLINE;
        onlineSince = System.currentTimeMillis();
    }

    public void stop() {
        if (state == NodeState.ONLINE) {
            accumulatedUptime += System.currentTimeMillis() - onlineSince;
        }
        state = NodeState.OFFLINE;
    }

    public void fail() {
        if (state == NodeState.ONLINE) {
            accumulatedUptime += System.currentTimeMillis() - onlineSince;
        }
        state = NodeState.FAILED;
        role = NodeRole.NONE;
        neighbors.clear();
    }

    public void recover() {
        restartCount++;
        role = NodeRole.NONE;
        onlineSince = System.currentTimeMillis();
        state = NodeState.ONLINE;
    }

    public long uptimeMillis() {
        long total = accumulatedUptime;
        if (state == NodeState.ONLINE) {
            total += System.currentTimeMillis() - onlineSince;
        }
        return total;
    }

    public long restartCount() { return restartCount; }

    public double x()          { return x; }
    public double y()          { return y; }
    public void setPosition(double x, double y) { this.x = x; this.y = y; }

    @Override
    public String toString() { return id; }
}