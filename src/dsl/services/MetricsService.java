package dsl.services;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

public final class MetricsService {

    private final AtomicInteger totalNodes = new AtomicInteger();
    private final AtomicLong messagesSent = new AtomicLong();
    private final AtomicLong messagesReceived = new AtomicLong();
    private final AtomicLong messagesLost = new AtomicLong();

    private final DoubleAdder latencySum = new DoubleAdder();
    private final AtomicLong latencySamples = new AtomicLong();

    private final AtomicLong simulationSteps = new AtomicLong();

    public void setTotalNodes(int n)         { totalNodes.set(n); }
    public int totalNodes()                  { return totalNodes.get(); }

    public void incrementMessagesSent()      { messagesSent.incrementAndGet(); }
    public long messagesSent()               { return messagesSent.get(); }

    public void incrementMessagesReceived()  { messagesReceived.incrementAndGet(); }
    public long messagesReceived()           { return messagesReceived.get(); }

    public void incrementMessagesLost()      { messagesLost.incrementAndGet(); }
    public long messagesLost()               { return messagesLost.get(); }

    public void recordLatency(long ms) {
        latencySum.add(ms);
        latencySamples.incrementAndGet();
    }

    public double averageLatency() {
        long n = latencySamples.get();
        return n == 0 ? 0 : latencySum.sum() / n;
    }

    public void incrementSteps() { simulationSteps.incrementAndGet(); }
    public long steps()          { return simulationSteps.get(); }

    public void reset() {
        messagesSent.set(0);
        messagesReceived.set(0);
        messagesLost.set(0);
        latencySum.reset();
        latencySamples.set(0);
        simulationSteps.set(0);
    }
}