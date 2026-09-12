package dsl.network;

import java.util.concurrent.ThreadLocalRandom;

public final class NetworkConditions {

    private volatile double latencyMs      = 120;
    private volatile double jitterMs       = 40;
    private volatile double packetLoss     = 0.0;
    private volatile double duplicateRate  = 0.0;
    private volatile double failureRate    = 0.0;
    private volatile long   timeoutMs      = 1_500;
    private volatile int    maxRetries     = 3;

    public double latencyMs()                    { return latencyMs; }
    public void setLatencyMs(double v)           { latencyMs = Math.max(0, v); }

    public double jitterMs()                     { return jitterMs; }
    public void setJitterMs(double v)            { jitterMs = Math.max(0, v); }

    public double packetLoss()                   { return packetLoss; }
    public void setPacketLoss(double v)          { packetLoss = clamp01(v); }

    public double duplicateRate()                { return duplicateRate; }
    public void setDuplicateRate(double v)       { duplicateRate = clamp01(v); }

    public double failureRate()                  { return failureRate; }
    public void setFailureRate(double v)         { failureRate = clamp01(v); }

    public long timeoutMs()                      { return timeoutMs; }
    public void setTimeoutMs(long v)             { timeoutMs = Math.max(50, v); }

    public int maxRetries()                      { return maxRetries; }
    public void setMaxRetries(int v)             { maxRetries = Math.max(0, v); }

    public long sampleLatency() {
        double jitter = jitterMs <= 0 ? 0 : ThreadLocalRandom.current().nextDouble(-jitterMs, jitterMs);
        return Math.max(1, Math.round(latencyMs + jitter));
    }

    public boolean shouldDrop() {
        return ThreadLocalRandom.current().nextDouble() < packetLoss;
    }

    public boolean shouldDuplicate() {
        return ThreadLocalRandom.current().nextDouble() < duplicateRate;
    }

    public boolean shouldFail() {
        return ThreadLocalRandom.current().nextDouble() < failureRate;
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }
}