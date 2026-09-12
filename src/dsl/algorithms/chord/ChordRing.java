package dsl.algorithms.chord;

import dsl.services.EventLogService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Simplified Chord DHT over a 256-key space. Keys and node ids are hashed
 * into [0,255]. Lookup traverses the ring via successor pointers, optionally
 * using the finger table for O(log N) hops.
 */
public final class ChordRing {

    public static final int M = 8;                // 2^8 = 256
    public static final int SPACE = 1 << M;

    private final List<ChordNode> nodes = new ArrayList<>();
    private final EventLogService log;

    public ChordRing(EventLogService log) {
        this.log = log;
    }

    public List<ChordNode> nodes() { return List.copyOf(nodes); }

    public synchronized ChordNode join(int id) {
        for (ChordNode n : nodes) if (n.id == id) return n;
        ChordNode n = new ChordNode(id);
        nodes.add(n);
        nodes.sort(Comparator.comparingInt(a -> a.id));
        stabilize();
        log.algorithm("CHORD", "Node " + id + " joined (ring size " + nodes.size() + ")");
        return n;
    }

    public synchronized void leave(int id) {
        nodes.removeIf(n -> n.id == id);
        stabilize();
        log.algorithm("CHORD", "Node " + id + " left");
    }

    public synchronized void stabilize() {
        if (nodes.isEmpty()) return;
        for (int i = 0; i < nodes.size(); i++) {
            ChordNode n = nodes.get(i);
            n.successor   = nodes.get((i + 1) % nodes.size()).id;
            n.predecessor = nodes.get((i - 1 + nodes.size()) % nodes.size()).id;
            for (int k = 0; k < M; k++) {
                int start = (n.id + (1 << k)) % SPACE;
                n.finger[k] = findSuccessor(start).id;
            }
        }
    }

    /** Returns the node responsible for {@code key}. */
    public synchronized ChordNode findSuccessor(int key) {
        if (nodes.isEmpty()) return null;
        for (ChordNode n : nodes) if (n.id >= key) return n;
        return nodes.get(0);
    }

    /** Simple successor-based lookup path (for visualisation). */
    public synchronized List<Integer> lookupPath(int key) {
        List<Integer> path = new ArrayList<>();
        if (nodes.isEmpty()) return path;
        ChordNode start = nodes.get(0);
        ChordNode current = start;
        int safety = nodes.size() * 2 + 4;
        while (safety-- > 0) {
            path.add(current.id);
            if (current.successor == current.id) break;
            // is key in (current, successor] ?
            int succ = current.successor;
            boolean wrap = succ < current.id;
            boolean hit = wrap
                    ? (key > current.id || key <= succ)
                    : (key > current.id && key <= succ);
            if (hit) break;
            ChordNode next = nodes.stream().filter(n -> n.id == succ).findFirst().orElse(null);
            if (next == null) break;
            current = next;
        }
        path.add(findSuccessor(key).id);
        return path;
    }

    /** Stable 8-bit hash of a key string. */
    public static int hash(String key) {
        int h = 0;
        for (int i = 0; i < key.length(); i++) h = (h * 31 + key.charAt(i)) & 0xFF;
        return h;
    }
}