package dsl.simulation;

@FunctionalInterface
public interface SimulationTask {
    void execute(SimulationEngine engine);
}