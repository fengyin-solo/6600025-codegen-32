package com.canbus.model;

import java.util.List;
import java.util.Map;

public class Sample {
    private String id;
    private String name;
    private String description;
    private List<String> tags;
    private String faultType;
    private long createdAt;
    private long durationMs;
    private List<CanFrame> frames;
    private BusStats busStats;
    private Map<String, SignalStats> signalStats;
    private int frameCount;

    public Sample() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getFaultType() { return faultType; }
    public void setFaultType(String faultType) { this.faultType = faultType; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public List<CanFrame> getFrames() { return frames; }
    public void setFrames(List<CanFrame> frames) { this.frames = frames; }

    public BusStats getBusStats() { return busStats; }
    public void setBusStats(BusStats busStats) { this.busStats = busStats; }

    public Map<String, SignalStats> getSignalStats() { return signalStats; }
    public void setSignalStats(Map<String, SignalStats> signalStats) { this.signalStats = signalStats; }

    public int getFrameCount() { return frameCount; }
    public void setFrameCount(int frameCount) { this.frameCount = frameCount; }
}
