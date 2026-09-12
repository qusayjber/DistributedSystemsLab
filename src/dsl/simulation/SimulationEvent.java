package dsl.simulation;

public record SimulationEvent(long time,
                              long sequence,
                              String description,
                              SimulationTask task)
        implements Comparable<SimulationEvent> {

    @Override
    public int compareTo(SimulationEvent other) {
        int byTime = Long.compare(this.time, other.time);
        return byTime != 0 ? byTime : Long.compare(this.sequence, other.sequence);
    }

    @Override
    public String toString() {
        return "[" + time + "ms #" + sequence + "] " + description;
    }
}