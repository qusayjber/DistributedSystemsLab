package dsl.algorithms.loadbalancing;

import java.util.List;

public interface LoadBalancingStrategy {
    String name();
    int pick(List<String> servers, List<Integer> connections, List<Integer> weights);
}