package com.canbus.controller;

import com.canbus.model.*;
import com.canbus.service.SampleLibraryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/samples")
@CrossOrigin(origins = "*")
public class SampleLibraryController {

    private final SampleLibraryService sampleLibraryService;

    public SampleLibraryController(SampleLibraryService sampleLibraryService) {
        this.sampleLibraryService = sampleLibraryService;
    }

    @GetMapping
    public List<Sample> getAllSamples() {
        return sampleLibraryService.getAllSamples();
    }

    @GetMapping("/{id}")
    public Sample getSample(@PathVariable String id) {
        return sampleLibraryService.getSample(id);
    }

    @PostMapping
    public Sample saveSample(@RequestBody Sample sample) {
        return sampleLibraryService.saveSample(sample);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteSample(@PathVariable String id) {
        boolean deleted = sampleLibraryService.deleteSample(id);
        return Map.of(
            "success", deleted,
            "message", deleted ? "样本删除成功" : "无法删除内置样本或样本不存在"
        );
    }

    @PostMapping("/{id}/compare")
    public ComparisonResult compareWithSample(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<CanFrame> frames = (List<CanFrame>) request.get("frames");
        @SuppressWarnings("unchecked")
        Map<String, Object> statsMap = (Map<String, Object>) request.get("busStats");

        BusStats busStats = new BusStats();
        busStats.setTotalFrames(((Number) statsMap.get("totalFrames")).intValue());
        busStats.setRxCount(((Number) statsMap.get("rxCount")).intValue());
        busStats.setTxCount(((Number) statsMap.get("txCount")).intValue());
        busStats.setErrorCount(((Number) statsMap.get("errorCount")).intValue());
        busStats.setBusLoad(((Number) statsMap.get("busLoad")).doubleValue());
        busStats.setLastUpdate(((Number) statsMap.get("lastUpdate")).longValue());

        return sampleLibraryService.compareWithSample(id, frames, busStats);
    }

    @PostMapping("/match")
    public Map<String, Object> findMostSimilarSample(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<CanFrame> frames = (List<CanFrame>) request.get("frames");
        @SuppressWarnings("unchecked")
        Map<String, Object> statsMap = (Map<String, Object>) request.get("busStats");

        BusStats busStats = new BusStats();
        busStats.setTotalFrames(((Number) statsMap.get("totalFrames")).intValue());
        busStats.setRxCount(((Number) statsMap.get("rxCount")).intValue());
        busStats.setTxCount(((Number) statsMap.get("txCount")).intValue());
        busStats.setErrorCount(((Number) statsMap.get("errorCount")).intValue());
        busStats.setBusLoad(((Number) statsMap.get("busLoad")).doubleValue());
        busStats.setLastUpdate(((Number) statsMap.get("lastUpdate")).longValue());

        return sampleLibraryService.findMostSimilarSample(frames, busStats);
    }
}
