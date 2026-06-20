package com.canbus.model;

public class BusStats {
    private int totalFrames;
    private int rxCount;
    private int txCount;
    private int errorCount;
    private double busLoad;
    private long lastUpdate;

    public BusStats() {}

    public BusStats(int totalFrames, int rxCount, int txCount, int errorCount, double busLoad, long lastUpdate) {
        this.totalFrames = totalFrames;
        this.rxCount = rxCount;
        this.txCount = txCount;
        this.errorCount = errorCount;
        this.busLoad = busLoad;
        this.lastUpdate = lastUpdate;
    }

    public int getTotalFrames() { return totalFrames; }
    public void setTotalFrames(int totalFrames) { this.totalFrames = totalFrames; }

    public int getRxCount() { return rxCount; }
    public void setRxCount(int rxCount) { this.rxCount = rxCount; }

    public int getTxCount() { return txCount; }
    public void setTxCount(int txCount) { this.txCount = txCount; }

    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }

    public double getBusLoad() { return busLoad; }
    public void setBusLoad(double busLoad) { this.busLoad = busLoad; }

    public long getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(long lastUpdate) { this.lastUpdate = lastUpdate; }
}
