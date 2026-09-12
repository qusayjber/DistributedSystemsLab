package dsl.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class TimeFormat {

    private static final DateTimeFormatter HMS =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private TimeFormat() { }

    public static String stamp(long epochMillis) {
        return HMS.format(Instant.ofEpochMilli(epochMillis));
    }

    public static String duration(long millis) {
        if (millis < 1_000) return millis + " ms";
        long s = millis / 1_000;
        if (s < 60) return s + " s";
        long m = s / 60;
        if (m < 60) return m + " m " + (s % 60) + " s";
        return (m / 60) + " h " + (m % 60) + " m";
    }
}