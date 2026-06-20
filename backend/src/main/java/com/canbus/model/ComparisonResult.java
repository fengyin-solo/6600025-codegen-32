package com.canbus.model;

import java.util.List;

public class ComparisonResult {
    private String sampleId;
    private String sampleName;
    private int overallSimilarity;
    private List<SignalComparison> signalComparisons;
    private List<String> abnormalSignals;
    private List<String> matchedFaultPatterns;
    private String diagnosisSuggestion;

    public ComparisonResult() {}

    public String getSampleId() { return sampleId; }
    public void setSampleId(String sampleId) { this.sampleId = sampleId; }

    public String getSampleName() { return sampleName; }
    public void setSampleName(String sampleName) { this.sampleName = sampleName; }

    public int getOverallSimilarity() { return overallSimilarity; }
    public void setOverallSimilarity(int overallSimilarity) { this.overallSimilarity = overallSimilarity; }

    public List<SignalComparison> getSignalComparisons() { return signalComparisons; }
    public void setSignalComparisons(List<SignalComparison> signalComparisons) { this.signalComparisons = signalComparisons; }

    public List<String> getAbnormalSignals() { return abnormalSignals; }
    public void setAbnormalSignals(List<String> abnormalSignals) { this.abnormalSignals = abnormalSignals; }

    public List<String> getMatchedFaultPatterns() { return matchedFaultPatterns; }
    public void setMatchedFaultPatterns(List<String> matchedFaultPatterns) { this.matchedFaultPatterns = matchedFaultPatterns; }

    public String getDiagnosisSuggestion() { return diagnosisSuggestion; }
    public void setDiagnosisSuggestion(String diagnosisSuggestion) { this.diagnosisSuggestion = diagnosisSuggestion; }
}
