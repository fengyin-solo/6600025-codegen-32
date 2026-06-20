export interface CanFrame {
  id: string;
  timestamp: number;
  arbitrationId: number;
  dlc: number;
  data: string;
  decoded: Record<string, number>;
  direction: 'RX' | 'TX';
}

export interface DbcSignal {
  name: string;
  startBit: number;
  bitLength: number;
  factor: number;
  offset: number;
  unit: string;
  minValue: number;
  maxValue: number;
  messageId: number;
}

export interface DbcMessage {
  id: number;
  name: string;
  dlc: number;
  sender: string;
  signals: DbcSignal[];
}

export interface BusStats {
  totalFrames: number;
  rxCount: number;
  txCount: number;
  errorCount: number;
  busLoad: number;
  lastUpdate: number;
}

export interface SignalStats {
  name: string;
  min: number;
  max: number;
  avg: number;
  median: number;
  stdDev: number;
  sampleCount: number;
}

export interface Sample {
  id: string;
  name: string;
  description: string;
  tags: string[];
  faultType: 'normal' | 'overheat' | 'high_load' | 'erratic_rpm' | 'communication_error' | 'custom';
  createdAt: number;
  durationMs: number;
  frames: CanFrame[];
  busStats: BusStats;
  signalStats: Record<string, SignalStats>;
  frameCount: number;
}

export interface SignalComparison {
  signalName: string;
  currentValue: number;
  sampleValue: number;
  deviation: number;
  deviationPercent: number;
  isAbnormal: boolean;
  threshold: number;
}

export interface ComparisonResult {
  sampleId: string;
  sampleName: string;
  overallSimilarity: number;
  signalComparisons: SignalComparison[];
  abnormalSignals: string[];
  matchedFaultPatterns: string[];
  diagnosisSuggestion: string;
}

export interface SaveSampleOptions {
  name: string;
  description: string;
  tags: string[];
  faultType: Sample['faultType'];
}
