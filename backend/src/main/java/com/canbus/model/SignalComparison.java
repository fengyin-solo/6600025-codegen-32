package com.canbus.model;

public class SignalComparison {
    private String signalName;
    private double currentValue;
    private double sampleValue;
    private double deviation;
    private double deviationPercent;
    private boolean isAbnormal;
    private double threshold;

    public SignalComparison() {}

    public String getSignalName() { return signalName; }
    public void setSignalName(String signalName) { this.signalName = signalName; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public double getSampleValue() { return sampleValue; }
    public void setSampleValue(double sampleValue) { this.sampleValue = sampleValue; }

    public double getDeviation() { return deviation; }
    public void setDeviation(double deviation) { this.deviation = deviation; }

    public double getDeviationPercent() { return deviationPercent; }
    public void setDeviationPercent(double deviationPercent) { this.deviationPercent = deviationPercent; }

    public boolean isAbnormal() { return isAbnormal; }
    public void setAbnormal(boolean abnormal) { isAbnormal = abnormal; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
}
