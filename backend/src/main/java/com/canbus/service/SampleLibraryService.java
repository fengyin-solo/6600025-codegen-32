package com.canbus.service;

import com.canbus.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SampleLibraryService {

    private final Map<String, Sample> sampleStorage = new ConcurrentHashMap<>();

    public SampleLibraryService() {
        initBuiltinSamples();
    }

    private void initBuiltinSamples() {
        Sample normalSample = createBuiltinSample(
            "builtin-normal-001",
            "正常工况样本",
            "发动机怠速运行，水温正常，无故障码",
            Arrays.asList("正常", "怠速", "基准"),
            "normal",
            800, 750, 850,
            0, 0, 5,
            90, 85, 95,
            2, 0, 5,
            20, 15, 25,
            25.5, 50, 35, 15
        );

        Sample overheatSample = createBuiltinSample(
            "builtin-overheat-001",
            "发动机过热故障",
            "冷却液温度超过110°C，发动机负载偏高，可能由冷却系统故障引起",
            Arrays.asList("故障", "过热", "冷却系统"),
            "overheat",
            2800, 2000, 3500,
            60, 40, 80,
            112, 105, 120,
            35, 20, 50,
            72, 60, 85,
            32.8, 75, 52, 23
        );

        Sample highLoadSample = createBuiltinSample(
            "builtin-highload-001",
            "高负载工况故障",
            "发动机长时间处于高负载状态，可能导致动力不足或燃油消耗异常",
            Arrays.asList("故障", "高负载", "燃油系统"),
            "high_load",
            4200, 3000, 5500,
            85, 60, 110,
            98, 90, 105,
            80, 60, 100,
            88, 75, 98,
            38.2, 60, 42, 18
        );

        Sample erraticSample = createBuiltinSample(
            "builtin-erratic-001",
            "转速波动异常",
            "发动机转速波动剧烈，怠速不稳定，可能由点火系统或进气系统故障引起",
            Arrays.asList("故障", "转速波动", "点火系统"),
            "erratic_rpm",
            950, 500, 1500,
            3, 0, 10,
            88, 80, 95,
            5, 0, 15,
            25, 10, 40,
            28.5, 40, 28, 12
        );

        sampleStorage.put(normalSample.getId(), normalSample);
        sampleStorage.put(overheatSample.getId(), overheatSample);
        sampleStorage.put(highLoadSample.getId(), highLoadSample);
        sampleStorage.put(erraticSample.getId(), erraticSample);
    }

    private Sample createBuiltinSample(String id, String name, String description,
                                       List<String> tags, String faultType,
                                       double rpmAvg, double rpmMin, double rpmMax,
                                       double speedAvg, double speedMin, double speedMax,
                                       double tempAvg, double tempMin, double tempMax,
                                       double throttleAvg, double throttleMin, double throttleMax,
                                       double loadAvg, double loadMin, double loadMax,
                                       double busLoad, int frameCount, int rxCount, int txCount) {
        Sample sample = new Sample();
        sample.setId(id);
        sample.setName(name);
        sample.setDescription(description);
        sample.setTags(tags);
        sample.setFaultType(faultType);
        sample.setCreatedAt(System.currentTimeMillis() - 86400000L * 30);
        sample.setDurationMs(10000);
        sample.setFrameCount(frameCount);
        sample.setFrames(new ArrayList<>());

        BusStats busStats = new BusStats();
        busStats.setTotalFrames(frameCount);
        busStats.setRxCount(rxCount);
        busStats.setTxCount(txCount);
        busStats.setErrorCount(0);
        busStats.setBusLoad(busLoad);
        busStats.setLastUpdate(System.currentTimeMillis());
        sample.setBusStats(busStats);

        Map<String, SignalStats> signalStats = new LinkedHashMap<>();
        signalStats.put("EngineRPM", createSignalStats("EngineRPM", rpmMin, rpmMax, rpmAvg, rpmAvg, 15, frameCount));
        signalStats.put("VehicleSpeed", createSignalStats("VehicleSpeed", speedMin, speedMax, speedAvg, speedAvg, 0.5, frameCount));
        signalStats.put("CoolantTemp", createSignalStats("CoolantTemp", tempMin, tempMax, tempAvg, tempAvg, 2, frameCount));
        signalStats.put("ThrottlePosition", createSignalStats("ThrottlePosition", throttleMin, throttleMax, throttleAvg, throttleAvg, 1, frameCount));
        signalStats.put("EngineLoad", createSignalStats("EngineLoad", loadMin, loadMax, loadAvg, loadAvg, 3, frameCount));
        sample.setSignalStats(signalStats);

        return sample;
    }

    private SignalStats createSignalStats(String name, double min, double max, double avg,
                                          double median, double stdDev, int sampleCount) {
        return new SignalStats(name, min, max, avg, median, stdDev, sampleCount);
    }

    public List<Sample> getAllSamples() {
        return new ArrayList<>(sampleStorage.values());
    }

    public Sample getSample(String id) {
        return sampleStorage.get(id);
    }

    public Sample saveSample(Sample sample) {
        if (sample.getId() == null || sample.getId().isEmpty()) {
            sample.setId("sample-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8));
        }
        sample.setCreatedAt(System.currentTimeMillis());
        sampleStorage.put(sample.getId(), sample);
        return sample;
    }

    public boolean deleteSample(String id) {
        if (id.startsWith("builtin-")) {
            return false;
        }
        return sampleStorage.remove(id) != null;
    }

    public ComparisonResult compareWithSample(String sampleId, List<CanFrame> currentFrames, BusStats currentStats) {
        Sample sample = sampleStorage.get(sampleId);
        if (sample == null) {
            return null;
        }

        Map<String, SignalStats> currentSignalStats = calculateSignalStats(currentFrames);
        List<SignalComparison> signalComparisons = new ArrayList<>();
        List<String> abnormalSignals = new ArrayList<>();

        for (Map.Entry<String, SignalStats> entry : sample.getSignalStats().entrySet()) {
            String signalName = entry.getKey();
            SignalStats sampleStat = entry.getValue();
            SignalStats currentStat = currentSignalStats.get(signalName);

            if (currentStat == null) continue;

            double currentValue = currentStat.getAvg();
            double sampleValue = sampleStat.getAvg();
            double deviation = Math.abs(currentValue - sampleValue);
            double deviationPercent = sampleValue != 0 ? (deviation / sampleValue) * 100 : deviation * 100;

            double threshold = Math.max(sampleStat.getStdDev() * 2, sampleValue * 0.15);
            boolean isAbnormal = deviation > threshold;

            if (isAbnormal) {
                abnormalSignals.add(signalName);
            }

            SignalComparison sc = new SignalComparison();
            sc.setSignalName(signalName);
            sc.setCurrentValue(currentValue);
            sc.setSampleValue(sampleValue);
            sc.setDeviation(deviation);
            sc.setDeviationPercent(deviationPercent);
            sc.setAbnormal(isAbnormal);
            sc.setThreshold(threshold);
            signalComparisons.add(sc);
        }

        double busLoadDeviation = Math.abs(currentStats.getBusLoad() - sample.getBusStats().getBusLoad());
        double busLoadSimilarity = Math.max(0, 100 - busLoadDeviation * 2);

        double signalSimilaritySum = 0;
        int signalSimilarityCount = 0;
        for (SignalComparison sc : signalComparisons) {
            double maxDeviation = Math.max(sc.getSampleValue() * 0.5, sc.getSampleValue() + sc.getThreshold() * 2);
            double similarity = sc.getDeviation() == 0 ? 100 : Math.max(0, 100 - (sc.getDeviation() / maxDeviation) * 100);
            signalSimilaritySum += similarity;
            signalSimilarityCount++;
        }
        double signalSimilarity = signalSimilarityCount > 0 ? signalSimilaritySum / signalSimilarityCount : 0;

        int overallSimilarity = (int) Math.round((busLoadSimilarity * 0.3 + signalSimilarity * 0.7));

        List<String> matchedPatterns = matchFaultPatterns(currentSignalStats, sample);
        String diagnosis = generateDiagnosis(abnormalSignals, sample, overallSimilarity);

        ComparisonResult result = new ComparisonResult();
        result.setSampleId(sample.getId());
        result.setSampleName(sample.getName());
        result.setOverallSimilarity(overallSimilarity);
        result.setSignalComparisons(signalComparisons);
        result.setAbnormalSignals(abnormalSignals);
        result.setMatchedFaultPatterns(matchedPatterns);
        result.setDiagnosisSuggestion(diagnosis);

        return result;
    }

    private Map<String, SignalStats> calculateSignalStats(List<CanFrame> frames) {
        Map<String, List<Double>> signalValues = new HashMap<>();

        for (CanFrame frame : frames) {
            Map<String, Double> decoded = frame.getDecoded();
            if (decoded != null) {
                for (Map.Entry<String, Double> entry : decoded.entrySet()) {
                    signalValues.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                               .add(entry.getValue());
                }
            }
        }

        Map<String, SignalStats> stats = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> entry : signalValues.entrySet()) {
            List<Double> values = entry.getValue();
            if (values.isEmpty()) continue;

            Collections.sort(values);
            double sum = values.stream().mapToDouble(Double::doubleValue).sum();
            double avg = sum / values.size();
            double median = values.size() % 2 == 0
                ? (values.get(values.size() / 2 - 1) + values.get(values.size() / 2)) / 2
                : values.get(values.size() / 2);

            double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - avg, 2))
                .average()
                .orElse(0);
            double stdDev = Math.sqrt(variance);

            SignalStats s = new SignalStats();
            s.setName(entry.getKey());
            s.setMin(Collections.min(values));
            s.setMax(Collections.max(values));
            s.setAvg(avg);
            s.setMedian(median);
            s.setStdDev(stdDev);
            s.setSampleCount(values.size());
            stats.put(entry.getKey(), s);
        }

        return stats;
    }

    private List<String> matchFaultPatterns(Map<String, SignalStats> currentStats, Sample sample) {
        List<String> patterns = new ArrayList<>();

        SignalStats coolant = currentStats.get("CoolantTemp");
        if (coolant != null && coolant.getAvg() > 105) {
            patterns.add("发动机过热");
        }

        SignalStats load = currentStats.get("EngineLoad");
        if (load != null && load.getAvg() > 80) {
            patterns.add("高负载运行");
        }

        SignalStats rpm = currentStats.get("EngineRPM");
        if (rpm != null && rpm.getStdDev() > 150) {
            patterns.add("转速波动异常");
        }

        SignalStats throttle = currentStats.get("ThrottlePosition");
        if (throttle != null && throttle.getAvg() > 70 && load != null && load.getAvg() > 80) {
            patterns.add("急加速工况");
        }

        if ("overheat".equals(sample.getFaultType()) && coolant != null && coolant.getAvg() > 100) {
            patterns.add("冷却系统故障嫌疑");
        }
        if ("erratic_rpm".equals(sample.getFaultType()) && rpm != null && rpm.getStdDev() > 100) {
            patterns.add("点火系统故障嫌疑");
        }

        return patterns;
    }

    private String generateDiagnosis(List<String> abnormalSignals, Sample sample, int similarity) {
        if (similarity >= 85) {
            if ("normal".equals(sample.getFaultType())) {
                return "当前工况与正常样本高度匹配，系统运行正常。";
            } else {
                return "高度匹配【" + sample.getName() + "】故障模式！请立即检查相关系统。建议：" + getFaultAdvice(sample.getFaultType());
            }
        }

        if (similarity >= 60) {
            if (!abnormalSignals.isEmpty()) {
                return "检测到" + abnormalSignals.size() + "项异常信号（" + String.join("、", abnormalSignals) + "），与【" + sample.getName() + "】有" + similarity + "%相似性，建议密切关注。";
            }
            return "与【" + sample.getName() + "】有" + similarity + "%相似性，建议持续观察。";
        }

        if (!abnormalSignals.isEmpty()) {
            return "检测到异常信号：" + String.join("、", abnormalSignals) + "，但与已知故障模式匹配度较低，建议进一步诊断。";
        }

        return "当前工况与所选样本差异较大，未发现明显匹配的故障模式。";
    }

    private String getFaultAdvice(String faultType) {
        switch (faultType) {
            case "overheat":
                return "检查冷却液液位、水泵、节温器和散热风扇";
            case "high_load":
                return "检查空气滤清器、燃油系统和排气系统是否堵塞";
            case "erratic_rpm":
                return "检查火花塞、点火线圈、节气门和怠速控制阀";
            case "communication_error":
                return "检查CAN总线连接、终端电阻和ECU通信模块";
            default:
                return "建议使用专业诊断设备读取详细故障码";
        }
    }

    public Map<String, Object> findMostSimilarSample(List<CanFrame> currentFrames, BusStats currentStats) {
        Sample bestMatch = null;
        int bestSimilarity = -1;
        ComparisonResult bestResult = null;

        for (Sample sample : sampleStorage.values()) {
            ComparisonResult result = compareWithSample(sample.getId(), currentFrames, currentStats);
            if (result != null && (bestMatch == null || result.getOverallSimilarity() > bestSimilarity)) {
                bestMatch = sample;
                bestSimilarity = result.getOverallSimilarity();
                bestResult = result;
            }
        }

        if (bestMatch == null) {
            return null;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sample", bestMatch);
        result.put("similarity", bestSimilarity);
        result.put("comparisonResult", bestResult);
        return result;
    }
}
