package dsl.simulation;

import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
public final class SimulationEngine {

    /* ---------------------------------------------------------------------- */
    /* State                                                                  */
    /* ---------------------------------------------------------------------- */

    private final PriorityQueue<SimulationEvent> queue = new PriorityQueue<>();
    private final AtomicLong sequence = new AtomicLong();

    private final List<Consumer<SimulationEvent>> stepListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<Long>>            timeListeners = new CopyOnWriteArrayList<>();

    private volatile long    currentTime;
    private volatile double  speed = 1.0;
    private volatile boolean running;

    private ScheduledExecutorService ticker;
    private long lastRealNanos;

    /* ---------------------------------------------------------------------- */
    /* Scheduling                                                             */
    /* ---------------------------------------------------------------------- */

    /**
     * Schedules {@code task} to run {@code delayMs} virtual milliseconds from
     * now.
     */
    public synchronized void schedule(long delayMs, String description, SimulationTask task) {
        long time = currentTime + Math.max(0, delayMs);
        queue.add(new SimulationEvent(time, sequence.incrementAndGet(), description, task));
    }

    /**
     * Schedules {@code task} to run at the absolute virtual time
     * {@code timeMs}. If that time is already in the past it is executed as
     * soon as possible (i.e. at {@code currentTime}).
     */
    public synchronized void scheduleAt(long timeMs, String description, SimulationTask task) {
        queue.add(new SimulationEvent(Math.max(currentTime, timeMs),
                sequence.incrementAndGet(), description, task));
    }

    /* ---------------------------------------------------------------------- */
    /* Stepping                                                               */
    /* ---------------------------------------------------------------------- */

    /**
     * Executes exactly one event.
     *
     * @return {@code true} if an event was executed, {@code false} if the
     *         queue was empty
     */
    public synchronized boolean step() {
        SimulationEvent e = queue.poll();
        if (e == null) return false;

        currentTime = Math.max(currentTime, e.time());
        try {
            e.task().execute(this);
        } catch (RuntimeException ex) {
            System.err.println("Event failed: " + e.description() + " → " + ex);
            ex.printStackTrace(System.err);
        }
        stepListeners.forEach(l -> l.accept(e));
        return true;
    }

    /* ---------------------------------------------------------------------- */
    /* Playback control                                                       */
    /* ---------------------------------------------------------------------- */

    /** Starts the background ticker. No‑op if already running. */
    public synchronized void play() {
        if (running) return;
        running = true;
        lastRealNanos = System.nanoTime();

        ticker = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "dsl-simulation");
            t.setDaemon(true);
            return t;
        });
        ticker.scheduleAtFixedRate(this::tick, 0, 25, TimeUnit.MILLISECONDS);
    }

    /** Stops the background ticker. No‑op if already paused. */
    public synchronized void pause() {
        running = false;
        if (ticker != null) {
            ticker.shutdownNow();
            ticker = null;
        }
    }

    /** Pauses the engine, clears the queue and resets virtual time to zero. */
    public synchronized void reset() {
        pause();
        queue.clear();
        sequence.set(0);
        currentTime = 0;
        timeListeners.forEach(l -> l.accept(0L));
    }

    /* ---------------------------------------------------------------------- */
    /* Internal tick                                                          */
    /* ---------------------------------------------------------------------- */

    private void tick() {
        if (!running) return;

        long now = System.nanoTime();
        long elapsedMs = (now - lastRealNanos) / 1_000_000L;
        lastRealNanos = now;
        if (elapsedMs <= 0) return;

        currentTime += Math.max(1, (long) (elapsedMs * speed));

        // Safety valve: never block the ticker indefinitely if a buggy task
        // schedules work far into the past.
        int budget = 5_000;
        while (running && budget-- > 0) {
            SimulationEvent head;
            synchronized (this) {
                head = queue.peek();
                if (head == null || head.time() > currentTime) break;
                step();
            }
        }
        timeListeners.forEach(l -> l.accept(currentTime));
    }

    /* ---------------------------------------------------------------------- */
    /* Introspection                                                          */
    /* ---------------------------------------------------------------------- */

    public long currentTime()         { return currentTime; }
    public double speed()             { return speed; }
    public boolean isRunning()        { return running; }

    public boolean hasPendingEvents() { synchronized (this) { return !queue.isEmpty(); } }
    public int pendingEventCount()    { synchronized (this) { return queue.size(); } }

    /** Sets the real‑time → virtual‑time multiplier. */
    public void setSpeed(double s)    { this.speed = Math.max(0.01, s); }

    /* ---------------------------------------------------------------------- */
    /* Listeners                                                              */
    /* ---------------------------------------------------------------------- */

    /** Called on the simulation thread after each event is executed. */
    public void onStep(Consumer<SimulationEvent> listener) {
        if (listener != null) stepListeners.add(listener);
    }

    /** Called on the simulation thread whenever virtual time advances. */
    public void onTime(Consumer<Long> listener) {
        if (listener != null) timeListeners.add(listener);
    }
}