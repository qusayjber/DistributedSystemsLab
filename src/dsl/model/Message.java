package dsl.model;

import java.util.Collections;
import java.util.Map;

public final class Message {

    private final String id;
    private final String sourceId;
    private final String destinationId;
    private final MessageType type;
    private final String payload;
    private final long createdAt = System.currentTimeMillis();
    private final long logicalTimestamp;
    private final Map<String, Integer> vectorSnapshot;

    private long departureTime;   // simulation time
    private long arrivalTime;     // simulation time
    private MessageStatus status = MessageStatus.CREATED;
    private int retryCount;
    private boolean duplicate;

    public Message(String id,
                   String sourceId,
                   String destinationId,
                   MessageType type,
                   String payload,
                   long logicalTimestamp,
                   Map<String, Integer> vectorSnapshot) {
        this.id = id;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.type = type;
        this.payload = payload == null ? "" : payload;
        this.logicalTimestamp = logicalTimestamp;
        this.vectorSnapshot = vectorSnapshot == null ? Collections.emptyMap() : vectorSnapshot;
    }

    public String id()                      { return id; }
    public String sourceId()                { return sourceId; }
    public String destinationId()           { return destinationId; }
    public MessageType type()               { return type; }
    public String payload()                 { return payload; }
    public long createdAt()                 { return createdAt; }
    public long logicalTimestamp()          { return logicalTimestamp; }
    public Map<String, Integer> vector()    { return vectorSnapshot; }

    public long departureTime()             { return departureTime; }
    public void setDepartureTime(long t)    { this.departureTime = t; }

    public long arrivalTime()               { return arrivalTime; }
    public void setArrivalTime(long t)      { this.arrivalTime = t; }

    public long latency()                   { return Math.max(0, arrivalTime - departureTime); }

    public MessageStatus status()           { return status; }
    public void setStatus(MessageStatus s)  { this.status = s; }

    public int retryCount()                 { return retryCount; }
    public void incrementRetry()            { retryCount++; }

    public boolean duplicate()              { return duplicate; }
    public void markDuplicate()             { this.duplicate = true; }

    @Override
    public String toString() {
        return sourceId + " → " + destinationId + " : " + type;
    }
}