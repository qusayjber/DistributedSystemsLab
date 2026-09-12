package dsl.model;

public enum NodeState {
    ONLINE, OFFLINE, FAILED, RECOVERING;

    public String label() {
        return switch (this) {
            case ONLINE     -> "Online";
            case OFFLINE    -> "Offline";
            case FAILED     -> "Failed";
            case RECOVERING -> "Recovering";
        };
    }

    public boolean isOperational() {
        return this == ONLINE;
    }
}