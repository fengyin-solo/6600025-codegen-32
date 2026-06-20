import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Sample, ComparisonResult, SignalComparison, SignalStats, SaveSampleOptions, CanFrame, BusStats } from '../types';

const STORAGE_KEY = 'canbus_sample_library';

const BUILTIN_SAMPLES: Sample[] = [
  {
    id: 'builtin-normal-001',
    name: '正常工况样本',
    description: '发动机怠速运行，水温正常，无故障码',
    tags: ['正常', '怠速', '基准'],
    faultType: 'normal',
    createdAt: Date.now() - 86400000 * 30,
    durationMs: 10000,
    frameCount: 50,
    frames: [],
    busStats: {
      totalFrames: 50,
      rxCount: 35,
      txCount: 15,
      errorCount: 0,
      busLoad: 25.5,
      lastUpdate: Date.now()
    },
    signalStats: {
      EngineRPM: { name: 'EngineRPM', min: 750, max: 850, avg: 800, median: 800, stdDev: 15, sampleCount: 50 },
      VehicleSpeed: { name: 'VehicleSpeed', min: 0, max: 5, avg: 0, median: 0, stdDev: 0.5, sampleCount: 50 },
      CoolantTemp: { name: 'CoolantTemp', min: 85, max: 95, avg: 90, median: 90, stdDev: 2, sampleCount: 50 },
      ThrottlePosition: { name: 'ThrottlePosition', min: 0, max: 5, avg: 2, median: 2, stdDev: 1, sampleCount: 50 },
      EngineLoad: { name: 'EngineLoad', min: 15, max: 25, avg: 20, median: 20, stdDev: 3, sampleCount: 50 }
    }
  },
  {
    id: 'builtin-overheat-001',
    name: '发动机过热故障',
    description: '冷却液温度超过110°C，发动机负载偏高，可能由冷却系统故障引起',
    tags: ['故障', '过热', '冷却系统'],
    faultType: 'overheat',
    createdAt: Date.now() - 86400000 * 20,
    durationMs: 15000,
    frameCount: 75,
    frames: [],
    busStats: {
      totalFrames: 75,
      rxCount: 52,
      txCount: 23,
      errorCount: 0,
      busLoad: 32.8,
      lastUpdate: Date.now()
    },
    signalStats: {
      EngineRPM: { name: 'EngineRPM', min: 2000, max: 3500, avg: 2800, median: 2800, stdDev: 200, sampleCount: 75 },
      VehicleSpeed: { name: 'VehicleSpeed', min: 40, max: 80, avg: 60, median: 60, stdDev: 15, sampleCount: 75 },
      CoolantTemp: { name: 'CoolantTemp', min: 105, max: 120, avg: 112, median: 112, stdDev: 4, sampleCount: 75 },
      ThrottlePosition: { name: 'ThrottlePosition', min: 20, max: 50, avg: 35, median: 35, stdDev: 8, sampleCount: 75 },
      EngineLoad: { name: 'EngineLoad', min: 60, max: 85, avg: 72, median: 72, stdDev: 10, sampleCount: 75 }
    }
  },
  {
    id: 'builtin-highload-001',
    name: '高负载工况故障',
    description: '发动机长时间处于高负载状态，可能导致动力不足或燃油消耗异常',
    tags: ['故障', '高负载', '燃油系统'],
    faultType: 'high_load',
    createdAt: Date.now() - 86400000 * 15,
    durationMs: 12000,
    frameCount: 60,
    frames: [],
    busStats: {
      totalFrames: 60,
      rxCount: 42,
      txCount: 18,
      errorCount: 0,
      busLoad: 38.2,
      lastUpdate: Date.now()
    },
    signalStats: {
      EngineRPM: { name: 'EngineRPM', min: 3000, max: 5500, avg: 4200, median: 4200, stdDev: 350, sampleCount: 60 },
      VehicleSpeed: { name: 'VehicleSpeed', min: 60, max: 110, avg: 85, median: 85, stdDev: 20, sampleCount: 60 },
      CoolantTemp: { name: 'CoolantTemp', min: 90, max: 105, avg: 98, median: 98, stdDev: 4, sampleCount: 60 },
      ThrottlePosition: { name: 'ThrottlePosition', min: 60, max: 100, avg: 80, median: 80, stdDev: 12, sampleCount: 60 },
      EngineLoad: { name: 'EngineLoad', min: 75, max: 98, avg: 88, median: 88, stdDev: 6, sampleCount: 60 }
    }
  },
  {
    id: 'builtin-erratic-001',
    name: '转速波动异常',
    description: '发动机转速波动剧烈，怠速不稳定，可能由点火系统或进气系统故障引起',
    tags: ['故障', '转速波动', '点火系统'],
    faultType: 'erratic_rpm',
    createdAt: Date.now() - 86400000 * 10,
    durationMs: 8000,
    frameCount: 40,
    frames: [],
    busStats: {
      totalFrames: 40,
      rxCount: 28,
      txCount: 12,
      errorCount: 2,
      busLoad: 28.5,
      lastUpdate: Date.now()
    },
    signalStats: {
      EngineRPM: { name: 'EngineRPM', min: 500, max: 1500, avg: 950, median: 900, stdDev: 250, sampleCount: 40 },
      VehicleSpeed: { name: 'VehicleSpeed', min: 0, max: 10, avg: 3, median: 0, stdDev: 4, sampleCount: 40 },
      CoolantTemp: { name: 'CoolantTemp', min: 80, max: 95, avg: 88, median: 88, stdDev: 3, sampleCount: 40 },
      ThrottlePosition: { name: 'ThrottlePosition', min: 0, max: 15, avg: 5, median: 3, stdDev: 5, sampleCount: 40 },
      EngineLoad: { name: 'EngineLoad', min: 10, max: 40, avg: 25, median: 22, stdDev: 12, sampleCount: 40 }
    }
  }
];

export const useSampleLibraryStore = defineStore('sampleLibrary', () => {
  const samples = ref<Sample[]>([]);
  const selectedSampleId = ref<string | null>(null);
  const comparisonResult = ref<ComparisonResult | null>(null);
  const autoMatchEnabled = ref(false);

  const selectedSample = computed(() => {
    if (!selectedSampleId.value) return null;
    return samples.value.find(s => s.id === selectedSampleId.value) || null;
  });

  const samplesByType = computed(() => {
    const grouped: Record<string, Sample[]> = {};
    for (const sample of samples.value) {
      if (!grouped[sample.faultType]) {
        grouped[sample.faultType] = [];
      }
      grouped[sample.faultType].push(sample);
    }
    return grouped;
  });

  function loadSamples() {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      let userSamples: Sample[] = [];
      if (stored) {
        userSamples = JSON.parse(stored);
      }
      samples.value = [...BUILTIN_SAMPLES, ...userSamples];
    } catch (e) {
      console.error('Failed to load samples:', e);
      samples.value = [...BUILTIN_SAMPLES];
    }
  }

  function saveSamplesToStorage() {
    try {
      const userSamples = samples.value.filter(s => !s.id.startsWith('builtin-'));
      localStorage.setItem(STORAGE_KEY, JSON.stringify(userSamples));
    } catch (e) {
      console.error('Failed to save samples:', e);
    }
  }

  function calculateSignalStats(frames: CanFrame[], signalName: string): SignalStats | null {
    const values: number[] = [];
    for (const frame of frames) {
      if (frame.decoded && frame.decoded[signalName] !== undefined) {
        values.push(frame.decoded[signalName]);
      }
    }
    if (values.length === 0) return null;

    values.sort((a, b) => a - b);
    const sum = values.reduce((a, b) => a + b, 0);
    const avg = sum / values.length;
    const median = values.length % 2 === 0
      ? (values[values.length / 2 - 1] + values[values.length / 2]) / 2
      : values[Math.floor(values.length / 2)];
    const variance = values.reduce((acc, v) => acc + Math.pow(v - avg, 2), 0) / values.length;
    const stdDev = Math.sqrt(variance);

    return {
      name: signalName,
      min: Math.min(...values),
      max: Math.max(...values),
      avg,
      median,
      stdDev,
      sampleCount: values.length
    };
  }

  function saveCurrentSession(
    frames: CanFrame[],
    busStats: BusStats,
    options: SaveSampleOptions
  ): Sample {
    const signalNames = new Set<string>();
    for (const frame of frames) {
      if (frame.decoded) {
        Object.keys(frame.decoded).forEach(k => signalNames.add(k));
      }
    }

    const signalStats: Record<string, SignalStats> = {};
    for (const name of signalNames) {
      const stats = calculateSignalStats(frames, name);
      if (stats) {
        signalStats[name] = stats;
      }
    }

    const durationMs = frames.length > 1
      ? frames[frames.length - 1].timestamp - frames[0].timestamp
      : 0;

    const sample: Sample = {
      id: `sample-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      name: options.name,
      description: options.description,
      tags: options.tags,
      faultType: options.faultType,
      createdAt: Date.now(),
      durationMs,
      frameCount: frames.length,
      frames: [...frames],
      busStats: { ...busStats },
      signalStats
    };

    samples.value.push(sample);
    saveSamplesToStorage();
    return sample;
  }

  function deleteSample(sampleId: string) {
    if (sampleId.startsWith('builtin-')) {
      console.warn('Cannot delete built-in samples');
      return;
    }
    const index = samples.value.findIndex(s => s.id === sampleId);
    if (index > -1) {
      samples.value.splice(index, 1);
      if (selectedSampleId.value === sampleId) {
        selectedSampleId.value = null;
        comparisonResult.value = null;
      }
      saveSamplesToStorage();
    }
  }

  function selectSample(sampleId: string | null) {
    selectedSampleId.value = sampleId;
    if (!sampleId) {
      comparisonResult.value = null;
    }
  }

  function getCurrentSignalStats(frames: CanFrame[]): Record<string, SignalStats> {
    const signalNames = new Set<string>();
    for (const frame of frames) {
      if (frame.decoded) {
        Object.keys(frame.decoded).forEach(k => signalNames.add(k));
      }
    }

    const stats: Record<string, SignalStats> = {};
    for (const name of signalNames) {
      const s = calculateSignalStats(frames, name);
      if (s) {
        stats[name] = s;
      }
    }
    return stats;
  }

  function compareWithSample(
    sample: Sample,
    currentFrames: CanFrame[],
    currentBusStats: BusStats
  ): ComparisonResult {
    const currentStats = getCurrentSignalStats(currentFrames);
    const signalComparisons: SignalComparison[] = [];
    const abnormalSignals: string[] = [];

    for (const [signalName, sampleStat] of Object.entries(sample.signalStats)) {
      const currentStat = currentStats[signalName];
      if (!currentStat) continue;

      const currentValue = currentStat.avg;
      const sampleValue = sampleStat.avg;
      const deviation = Math.abs(currentValue - sampleValue);
      const deviationPercent = sampleValue !== 0 ? (deviation / sampleValue) * 100 : deviation * 100;

      const threshold = Math.max(sampleStat.stdDev * 2, sampleValue * 0.15);
      const isAbnormal = deviation > threshold;

      if (isAbnormal) {
        abnormalSignals.push(signalName);
      }

      signalComparisons.push({
        signalName,
        currentValue,
        sampleValue,
        deviation,
        deviationPercent,
        isAbnormal,
        threshold
      });
    }

    const busLoadDeviation = Math.abs(currentBusStats.busLoad - sample.busStats.busLoad);
    const busLoadSimilarity = Math.max(0, 100 - busLoadDeviation * 2);

    let signalSimilaritySum = 0;
    let signalSimilarityCount = 0;
    for (const sc of signalComparisons) {
      const maxDeviation = Math.max(sc.sampleValue * 0.5, sc.sampleValue + sc.threshold * 2);
      const similarity = sc.deviation === 0 ? 100 : Math.max(0, 100 - (sc.deviation / maxDeviation) * 100);
      signalSimilaritySum += similarity;
      signalSimilarityCount++;
    }
    const signalSimilarity = signalSimilarityCount > 0
      ? signalSimilaritySum / signalSimilarityCount
      : 0;

    const overallSimilarity = Math.round((busLoadSimilarity * 0.3 + signalSimilarity * 0.7));

    const matchedFaultPatterns = matchFaultPatterns(currentStats, sample);
    const diagnosisSuggestion = generateDiagnosis(abnormalSignals, sample, overallSimilarity);

    const result: ComparisonResult = {
      sampleId: sample.id,
      sampleName: sample.name,
      overallSimilarity,
      signalComparisons,
      abnormalSignals,
      matchedFaultPatterns,
      diagnosisSuggestion
    };

    comparisonResult.value = result;
    return result;
  }

  function matchFaultPatterns(
    currentStats: Record<string, SignalStats>,
    sample: Sample
  ): string[] {
    const patterns: string[] = [];

    const coolant = currentStats['CoolantTemp'];
    if (coolant && coolant.avg > 105) {
      patterns.push('发动机过热');
    }

    const load = currentStats['EngineLoad'];
    if (load && load.avg > 80) {
      patterns.push('高负载运行');
    }

    const rpm = currentStats['EngineRPM'];
    if (rpm && rpm.stdDev > 150) {
      patterns.push('转速波动异常');
    }

    const throttle = currentStats['ThrottlePosition'];
    if (throttle && throttle.avg > 70 && load && load.avg > 80) {
      patterns.push('急加速工况');
    }

    if (sample.faultType === 'overheat' && coolant && coolant.avg > 100) {
      patterns.push('冷却系统故障嫌疑');
    }
    if (sample.faultType === 'erratic_rpm' && rpm && rpm.stdDev > 100) {
      patterns.push('点火系统故障嫌疑');
    }

    return patterns;
  }

  function generateDiagnosis(
    abnormalSignals: string[],
    sample: Sample,
    similarity: number
  ): string {
    if (similarity >= 85) {
      if (sample.faultType === 'normal') {
        return '当前工况与正常样本高度匹配，系统运行正常。';
      } else {
        return `高度匹配【${sample.name}】故障模式！请立即检查相关系统。建议：${getFaultAdvice(sample.faultType)}`;
      }
    }

    if (similarity >= 60) {
      if (abnormalSignals.length > 0) {
        return `检测到${abnormalSignals.length}项异常信号（${abnormalSignals.join('、')}），与【${sample.name}】有${similarity}%相似性，建议密切关注。`;
      }
      return `与【${sample.name}】有${similarity}%相似性，建议持续观察。`;
    }

    if (abnormalSignals.length > 0) {
      return `检测到异常信号：${abnormalSignals.join('、')}，但与已知故障模式匹配度较低，建议进一步诊断。`;
    }

    return '当前工况与所选样本差异较大，未发现明显匹配的故障模式。';
  }

  function getFaultAdvice(faultType: Sample['faultType']): string {
    switch (faultType) {
      case 'overheat':
        return '检查冷却液液位、水泵、节温器和散热风扇';
      case 'high_load':
        return '检查空气滤清器、燃油系统和排气系统是否堵塞';
      case 'erratic_rpm':
        return '检查火花塞、点火线圈、节气门和怠速控制阀';
      case 'communication_error':
        return '检查CAN总线连接、终端电阻和ECU通信模块';
      default:
        return '建议使用专业诊断设备读取详细故障码';
    }
  }

  function findMostSimilarSample(
    currentFrames: CanFrame[],
    currentBusStats: BusStats
  ): { sample: Sample; similarity: number; result: ComparisonResult } | null {
    let bestMatch: { sample: Sample; similarity: number; result: ComparisonResult } | null = null;

    for (const sample of samples.value) {
      const result = compareWithSample(sample, currentFrames, currentBusStats);
      if (!bestMatch || result.overallSimilarity > bestMatch.similarity) {
        bestMatch = { sample, similarity: result.overallSimilarity, result };
      }
    }

    return bestMatch;
  }

  function exportSample(sample: Sample): string {
    const exportData = {
      version: '1.0',
      exportedAt: Date.now(),
      sample: {
        ...sample,
        id: undefined,
        createdAt: sample.createdAt
      }
    };
    return JSON.stringify(exportData, null, 2);
  }

  function importSample(jsonData: string): Sample | null {
    try {
      const data = JSON.parse(jsonData);
      if (!data.sample || !data.sample.name) {
        throw new Error('Invalid sample format');
      }

      const imported: Sample = {
        ...data.sample,
        id: `imported-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
        createdAt: data.sample.createdAt || Date.now()
      };

      samples.value.push(imported);
      saveSamplesToStorage();
      return imported;
    } catch (e) {
      console.error('Failed to import sample:', e);
      return null;
    }
  }

  function exportAllSamples(): string {
    const userSamples = samples.value.filter(s => !s.id.startsWith('builtin-'));
    const exportData = {
      version: '1.0',
      exportedAt: Date.now(),
      samples: userSamples.map(s => ({ ...s, id: undefined }))
    };
    return JSON.stringify(exportData, null, 2);
  }

  function importAllSamples(jsonData: string): Sample[] {
    try {
      const data = JSON.parse(jsonData);
      if (!data.samples || !Array.isArray(data.samples)) {
        throw new Error('Invalid samples format');
      }

      const imported: Sample[] = [];
      for (const s of data.samples) {
        const sample: Sample = {
          ...s,
          id: `imported-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
          createdAt: s.createdAt || Date.now()
        };
        samples.value.push(sample);
        imported.push(sample);
      }
      saveSamplesToStorage();
      return imported;
    } catch (e) {
      console.error('Failed to import samples:', e);
      return [];
    }
  }

  loadSamples();

  return {
    samples,
    selectedSampleId,
    selectedSample,
    comparisonResult,
    autoMatchEnabled,
    samplesByType,
    loadSamples,
    saveCurrentSession,
    deleteSample,
    selectSample,
    compareWithSample,
    findMostSimilarSample,
    exportSample,
    importSample,
    exportAllSamples,
    importAllSamples,
    getCurrentSignalStats
  };
});
