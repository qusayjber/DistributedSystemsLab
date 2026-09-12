package dsl.algorithms.loadbalancing;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class Strategies {

    private Strategies() { }

    public static final class RoundRobin implements LoadBalancingStrategy {
        private final AtomicInteger i = new AtomicInteger();
        public String name() { return "Round Robin"; }
        public int pick(List<String> s, List<Integer> c, List<Integer> w) {
            return Math.floorMod(i.getAndIncrement(), s.size());
        }
    }

    public static final class WeightedRoundRobin implements LoadBalancingStrategy {
        private final AtomicInteger i = new AtomicInteger();
        public String name() { return "Weighted Round Robin"; }
        public int pick(List<String> s, List<Integer> c, List<Integer> w) {
            // expand the weighted list into a schedule
            int total = 0;
            for (int v : w) total += Math.max(1, v);
            int slot = Math.floorMod(i.getAndIncrement(), total);
            int acc = 0;
            for (int k = 0; k < s.size(); k++) {
                acc += Math.max(1, w.get(k));
                if (slot < acc) return k;
            }
            return 0;
        }
    }

    public static final class LeastConnections implements LoadBalancingStrategy {
        public String name() { return "Least Connections"; }
        public int pick(List<String> s, List<Integer> c, List<Integer> w) {
            int best = 0;
            for (int i = 1; i < s.size(); i++) if (c.get(i) < c.get(best)) best = i;
            return best;
        }
    }

    public static final class RandomStrategy implements LoadBalancingStrategy {
        public String name() { return "Random"; }
        public int pick(List<String> s, List<Integer> c, List<Integer> w) {
            return ThreadLocalRandom.current().nextInt(s.size());
        }
    }

    public static final class LeastLoad implements LoadBalancingStrategy {
        public String name() { return "Least Load"; }
        public int pick(List<String> s, List<Integer> c, List<Integer> w) {
            // "load" = connections / weight; lower is better
            double best = Double.MAX_VALUE;
            int bestIdx = 0;
            for (int i = 0; i < s.size(); i++) {
                double load = c.get(i) / (double) Math.max(1, w.get(i));
                if (load < best) { best = load; bestIdx = i; }
            }
            return bestIdx;
        }
    }
}