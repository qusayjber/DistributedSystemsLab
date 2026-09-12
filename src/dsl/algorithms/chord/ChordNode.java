package dsl.algorithms.chord;

public final class ChordNode {
    public final int id;              // 0..255
    public volatile int successor;
    public volatile int predecessor;
    public final int[] finger = new int[8];   // 2^i

    public ChordNode(int id) {
        this.id = id;
        this.successor = id;
        this.predecessor = id;
        for (int i = 0; i < 8; i++) finger[i] = id;
    }
}