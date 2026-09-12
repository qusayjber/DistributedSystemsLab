package dsl.algorithms.raft;

public record LogEntry(long index, long term, String command) { }