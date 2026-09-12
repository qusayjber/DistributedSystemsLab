package dsl.model;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Vector clock: {@code Map<NodeId, Integer>}.
 */
public final class VectorClock {

    private final Map<String, Integer> v = new TreeMap<>();

    public synchronized void increment(String nodeId) {
        v.merge(nodeId, 1, Integer::sum);
    }

    public synchronized int get(String nodeId) {
        return v.getOrDefault(nodeId, 0);
    }

    public synchronized Map<String, Integer> snapshot() {
        return Collections.unmodifiableMap(new TreeMap<>(v));
    }

    /** Receive rule: merge element‑wise maximum, then increment own component. */
    public synchronized void merge(String nodeId, Map<String, Integer> remote) {
        if (remote != null) {
            remote.forEach((k, val) -> v.merge(k, val, Math::max));
        }
        increment(nodeId);
    }

    public synchronized void reset() {
        v.clear();
    }

    public static CausalRelation compare(Map<String, Integer> a, Map<String, Integer> b) {
        boolean aLess = false, bLess = false;
        for (String k : union(a, b)) {
            int va = a.getOrDefault(k, 0);
            int vb = b.getOrDefault(k, 0);
            if (va < vb) aLess = true;
            if (vb < va) bLess = true;
        }
        if (aLess && bLess) return CausalRelation.CONCURRENT;
        if (aLess)          return CausalRelation.BEFORE;
        if (bLess)          return CausalRelation.AFTER;
        return CausalRelation.EQUAL;
    }

    private static java.util.Set<String> union(Map<String, Integer> a, Map<String, Integer> b) {
        java.util.Set<String> s = new java.util.TreeSet<>(a.keySet());
        s.addAll(b.keySet());
        return s;
    }
}