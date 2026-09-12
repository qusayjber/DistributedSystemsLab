package dsl.model;

public enum NodeRole {
    NONE, LEADER, FOLLOWER, CANDIDATE, COORDINATOR, PARTICIPANT, PRIMARY, REPLICA;

    public String glyph() {
        return switch (this) {
            case LEADER      -> "★";
            case FOLLOWER    -> "●";
            case CANDIDATE   -> "◆";
            case COORDINATOR -> "◈";
            case PARTICIPANT -> "◇";
            case PRIMARY     -> "⬤";
            case REPLICA     -> "◯";
            case NONE        -> "";
        };
    }

    public String label() {
        return switch (this) {
            case LEADER      -> "LEADER";
            case FOLLOWER    -> "FOLLOWER";
            case CANDIDATE   -> "CANDIDATE";
            case COORDINATOR -> "COORDINATOR";
            case PARTICIPANT -> "PARTICIPANT";
            case PRIMARY     -> "PRIMARY";
            case REPLICA     -> "REPLICA";
            case NONE        -> "—";
        };
    }
}