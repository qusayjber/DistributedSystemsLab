package dsl.services;

import dsl.utils.TimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventLogService {

    public enum Level { DEBUG, INFO, WARN, ERROR, ALGORITHM }

    public record Entry(long timestamp, Level level, String category, String message) {
        public String formatted() {
            return "[" + TimeFormat.stamp(timestamp) + "] [" + level + "] " + category + " — " + message;
        }
    }

    private final List<Entry> entries = new CopyOnWriteArrayList<>();
    private final List<Consumer<Entry>> listeners = new CopyOnWriteArrayList<>();
    private volatile Level threshold = Level.DEBUG;

    public void setThreshold(Level level) { this.threshold = level; }

    public void add(Level level, String category, String message) {
        if (level.ordinal() < threshold.ordinal()) return;
        Entry e = new Entry(System.currentTimeMillis(), level, category, message);
        entries.add(e);
        if (entries.size() > 20_000) entries.remove(0);
        listeners.forEach(l -> l.accept(e));
    }

    public void debug(String c, String m)     { add(Level.DEBUG, c, m); }
    public void info(String c, String m)      { add(Level.INFO, c, m); }
    public void warn(String c, String m)      { add(Level.WARN, c, m); }
    public void error(String c, String m)     { add(Level.ERROR, c, m); }
    public void algorithm(String c, String m) { add(Level.ALGORITHM, c, m); }

    public List<Entry> entries() { return new ArrayList<>(entries); }

    public void clear() { entries.clear(); }

    public void onEntry(Consumer<Entry> listener) { listeners.add(listener); }
}