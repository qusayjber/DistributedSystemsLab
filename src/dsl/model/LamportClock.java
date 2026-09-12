package dsl.model;

/**
 * Lamport logical clock.
 *
 * <p>Rule on send:    L = L + 1, attach L.
 * <p>Rule on receive: L = max(L, remote) + 1.
 */
public final class LamportClock {

    private long value;

    public synchronized long tick() {
        return ++value;
    }

    public synchronized long send() {
        return ++value;
    }

    public synchronized long receive(long remote) {
        value = Math.max(value, remote) + 1;
        return value;
    }

    public synchronized long value() {
        return value;
    }

    public synchronized void reset() {
        value = 0;
    }
}