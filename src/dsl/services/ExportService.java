package dsl.services;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ExportService {

    private ExportService() { }

    public static void exportEventLog(Path file, List<EventLogService.Entry> entries) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            w.write("timestamp,level,category,message");
            w.newLine();
            for (EventLogService.Entry e : entries) {
                w.write(csv(String.valueOf(e.timestamp())));
                w.write(',');
                w.write(csv(e.level().name()));
                w.write(',');
                w.write(csv(e.category()));
                w.write(',');
                w.write(csv(e.message()));
                w.newLine();
            }
        }
    }

    public static void exportText(Path file, String content) throws IOException {
        Files.writeString(file, content);
    }

    private static String csv(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        return s.contains(",") || s.contains("\"") || s.contains("\n") ? "\"" + s + "\"" : s;
    }
}