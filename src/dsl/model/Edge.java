package dsl.model;

public record Edge(String a, String b) {

    public static Edge of(String a, String b) {
        return a.compareTo(b) <= 0 ? new Edge(a, b) : new Edge(b, a);
    }

    public String other(String node) {
        return node.equals(a) ? b : a;
    }

    public boolean touches(String node) {
        return node.equals(a) || node.equals(b);
    }
}