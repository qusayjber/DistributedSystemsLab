package dsl.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class SettingsService {

    private static final Path DIR  = Paths.get(System.getProperty("user.home"), ".dsl-lab");
    private static final Path FILE = DIR.resolve("settings.properties");

    private final Properties properties = new Properties();

    /* ---------------------------------------------------------------------- */

    /** Private ctor — use the static {@link #load()} factory. */
    private SettingsService() {
        applyDefaults();
        readFromDisk();          // ← renamed; NO recursion any more
    }

    /** Static factory. */
    public static SettingsService load() {
        return new SettingsService();
    }

    /* ---------------------------------------------------------------------- */

    private void applyDefaults() {
        properties.setProperty("theme",            "dark");
        properties.setProperty("simulationSpeed",  "1.0");
        properties.setProperty("animationSpeed",   "1.0");
        properties.setProperty("defaultNodeCount", "5");
        properties.setProperty("latency",          "120");
        properties.setProperty("packetLoss",       "0.0");
        properties.setProperty("logLevel",         "DEBUG");
    }

    private void readFromDisk() {
        if (!Files.exists(FILE)) return;
        try (InputStream in = Files.newInputStream(FILE)) {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("Could not read settings: " + e.getMessage());
        }
    }

    /* ---------------------------------------------------------------------- */

    public void save() {
        try {
            Files.createDirectories(DIR);
            try (OutputStream out = Files.newOutputStream(FILE)) {
                properties.store(out, "Distributed Systems Laboratory");
            }
        } catch (IOException e) {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }

    public String get(String key)             { return properties.getProperty(key); }
    public void   set(String key, String v)   { properties.setProperty(key, v); }

    public int getInt(String key, int fallback) {
        try { return Integer.parseInt(properties.getProperty(key)); }
        catch (Exception e) { return fallback; }
    }

    public double getDouble(String key, double fallback) {
        try { return Double.parseDouble(properties.getProperty(key)); }
        catch (Exception e) { return fallback; }
    }
}