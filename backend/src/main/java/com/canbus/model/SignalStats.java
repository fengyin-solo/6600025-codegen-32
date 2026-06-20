package com.canbus.model;

public class SignalStats {
    private String name;
    private double min;
    private double max;
    private double avg;
    private double median;
    private double stdDev;
    private int sampleCount;

    public SignalStats() {}

    public SignalStats(String name, double min, double max, double avg, double median, double stdDev, int sampleCount) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.avg = avg;
        this.median = median;
        this.stdDev = stdDev;
        this.sampleCount = sampleCount;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }

    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }

    public double getAvg() { return avg; }
    public void setAvg(double avg) { this.avg = avg; }

    public double getMedian() { return median; }
    public void setMedian(double median) { this.median = median; }

    public double getStdDev() { return stdDev; }
    public void setStdDev(double stdDev) { this.stdDev = stdDev; }

    public int getSampleCount() { return sampleCount; }
    public void setSampleCount(int sampleCount) { this.sampleCount = sampleCount; }
}
