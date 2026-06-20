<script setup lang="ts">
import { computed, watch, ref } from 'vue';
import { useCanBusStore } from '../store/canbus';
import { useSampleLibraryStore } from '../store/sampleLibrary';

const canbusStore = useCanBusStore();
const sampleStore = useSampleLibraryStore();

const autoRefresh = ref(true);
let refreshTimer: number | null = null;

const faultTypeLabels: Record<string, string> = {
  normal: '正常工况',
  overheat: '过热故障',
  high_load: '高负载故障',
  erratic_rpm: '转速波动',
  communication_error: '通信错误',
  custom: '自定义'
};

const signalLabels: Record<string, string> = {
  EngineRPM: '发动机转速',
  VehicleSpeed: '车速',
  CoolantTemp: '冷却液温度',
  ThrottlePosition: '节气门位置',
  EngineLoad: '发动机负载'
};

const signalUnits: Record<string, string> = {
  EngineRPM: 'RPM',
  VehicleSpeed: 'km/h',
  CoolantTemp: '°C',
  ThrottlePosition: '%',
  EngineLoad: '%'
};

function getSignalLabel(name: string): string {
  return signalLabels[name] || name;
}

function getSignalUnit(name: string): string {
  return signalUnits[name] || '';
}

function formatValue(value: number, signalName: string): string {
  const unit = getSignalUnit(signalName);
  return `${value.toFixed(1)}${unit}`;
}

const similarityColor = computed(() => {
  if (!sampleStore.comparisonResult) return 'text-gray-400';
  const sim = sampleStore.comparisonResult.overallSimilarity;
  if (sim >= 80) return 'text-green-400';
  if (sim >= 60) return 'text-yellow-400';
  if (sim >= 40) return 'text-orange-400';
  return 'text-red-400';
});

const similarityBgColor = computed(() => {
  if (!sampleStore.comparisonResult) return 'from-gray-600 to-gray-700';
  const sim = sampleStore.comparisonResult.overallSimilarity;
  if (sim >= 80) return 'from-green-500 to-green-600';
  if (sim >= 60) return 'from-yellow-500 to-yellow-600';
  if (sim >= 40) return 'from-orange-500 to-orange-600';
  return 'from-red-500 to-red-600';
});

const similarityRingOffset = computed(() => {
  if (!sampleStore.comparisonResult) return 283;
  const sim = sampleStore.comparisonResult.overallSimilarity;
  const circumference = 2 * Math.PI * 45;
  return circumference * (1 - sim / 100);
});

function performComparison() {
  if (sampleStore.selectedSample && canbusStore.frames.length > 0) {
    sampleStore.compareWithSample(
      sampleStore.selectedSample,
      canbusStore.frames,
      canbusStore.busStats
    );
  }
}

watch(autoRefresh, (val) => {
  if (val) {
    performComparison();
    refreshTimer = window.setInterval(() => {
      if (sampleStore.selectedSample && canbusStore.frames.length > 0) {
        performComparison();
      }
    }, 1000);
  } else {
    if (refreshTimer) {
      clearInterval(refreshTimer);
      refreshTimer = null;
    }
  }
}, { immediate: true });

watch(() => sampleStore.selectedSampleId, () => {
  if (sampleStore.selectedSample && canbusStore.frames.length > 0) {
    performComparison();
  }
});
</script>

<template>
  <div class="h-full flex flex-col bg-gray-800 text-gray-100">
    <!-- Header -->
    <div class="px-4 py-3 border-b border-gray-700 flex items-center justify-between shrink-0">
      <h2 class="text-sm font-semibold text-gray-200">样本对比分析</h2>
      <div class="flex items-center gap-2">
        <label class="flex items-center gap-1 text-xs text-gray-400 cursor-pointer">
          <input
            type="checkbox"
            v-model="autoRefresh"
            class="rounded bg-gray-700 border-gray-600 text-cyan-500 focus:ring-cyan-500"
          />
          自动刷新
        </label>
        <button
          @click="performComparison"
          class="px-2 py-1 text-xs bg-cyan-600 hover:bg-cyan-500 text-white rounded transition-colors"
          :disabled="!sampleStore.selectedSample || canbusStore.frames.length === 0"
          :class="{ 'opacity-50 cursor-not-allowed': !sampleStore.selectedSample || canbusStore.frames.length === 0 }"
        >
          刷新对比
        </button>
      </div>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto">
      <!-- No selection -->
      <div v-if="!sampleStore.selectedSample" class="h-full flex flex-col items-center justify-center px-4 text-center">
        <svg class="w-16 h-16 text-gray-600 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        <p class="text-sm text-gray-400 mb-1">请从左侧样本库选择一个样本</p>
        <p class="text-xs text-gray-500">选择后将与当前会话数据进行对比分析</p>
      </div>

      <!-- No data -->
      <div v-else-if="canbusStore.frames.length === 0" class="h-full flex flex-col items-center justify-center px-4 text-center">
        <svg class="w-16 h-16 text-gray-600 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
        <p class="text-sm text-gray-400 mb-1">暂无当前会话数据</p>
        <p class="text-xs text-gray-500">请先开始捕获CAN总线数据</p>
      </div>

      <!-- Comparison Result -->
      <template v-else>
        <!-- Selected Sample Info -->
        <div class="p-3 border-b border-gray-700 bg-gray-750">
          <div class="flex items-start gap-3">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2">
                <h3 class="text-sm font-medium text-gray-100 truncate">{{ sampleStore.selectedSample.name }}</h3>
                <span class="px-1.5 py-0.5 text-xs bg-cyan-600 text-white rounded shrink-0">
                  {{ faultTypeLabels[sampleStore.selectedSample.faultType] }}
                </span>
              </div>
              <p class="text-xs text-gray-400 mt-1 line-clamp-2">{{ sampleStore.selectedSample.description }}</p>
            </div>
          </div>
        </div>

        <!-- Overall Similarity -->
        <div v-if="sampleStore.comparisonResult" class="p-4 border-b border-gray-700">
          <div class="flex items-center gap-6">
            <!-- Similarity Ring -->
            <div class="relative w-28 h-28 shrink-0">
              <svg class="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
                <circle
                  cx="50" cy="50" r="45"
                  fill="none"
                  stroke="#374151"
                  stroke-width="8"
                />
                <circle
                  cx="50" cy="50" r="45"
                  fill="none"
                  :class="`stroke-current ${similarityColor}`"
                  stroke-width="8"
                  stroke-linecap="round"
                  stroke-dasharray="283"
                  :stroke-dashoffset="similarityRingOffset"
                  class="transition-all duration-500"
                />
              </svg>
              <div class="absolute inset-0 flex flex-col items-center justify-center">
                <span :class="['text-2xl font-bold', similarityColor]">
                  {{ sampleStore.comparisonResult.overallSimilarity }}%
                </span>
                <span class="text-xs text-gray-400">相似度</span>
              </div>
            </div>

            <!-- Status Info -->
            <div class="flex-1 space-y-2">
              <div class="flex items-center gap-2">
                <div :class="['w-3 h-3 rounded-full bg-gradient-to-r', similarityBgColor]"></div>
                <span class="text-sm font-medium" :class="similarityColor">
                  {{ sampleStore.comparisonResult.overallSimilarity >= 85 ? '高度匹配' :
                     sampleStore.comparisonResult.overallSimilarity >= 60 ? '部分匹配' :
                     sampleStore.comparisonResult.overallSimilarity >= 40 ? '低匹配度' : '差异较大' }}
                </span>
              </div>
              <div v-if="sampleStore.comparisonResult.abnormalSignals.length > 0" class="text-xs text-red-400">
                检测到 {{ sampleStore.comparisonResult.abnormalSignals.length }} 项异常信号
              </div>
              <div v-else class="text-xs text-green-400">
                未检测到异常信号
              </div>
            </div>
          </div>
        </div>

        <!-- Diagnosis Suggestion -->
        <div v-if="sampleStore.comparisonResult" class="p-3 border-b border-gray-700">
          <div class="text-xs font-medium text-gray-400 mb-2">诊断建议</div>
          <div class="p-3 bg-gray-700 rounded-lg">
            <p class="text-sm text-gray-200">{{ sampleStore.comparisonResult.diagnosisSuggestion }}</p>
          </div>
        </div>

        <!-- Matched Patterns -->
        <div v-if="sampleStore.comparisonResult && sampleStore.comparisonResult.matchedFaultPatterns.length > 0" class="p-3 border-b border-gray-700">
          <div class="text-xs font-medium text-gray-400 mb-2">匹配的故障模式</div>
          <div class="flex flex-wrap gap-1.5">
            <span
              v-for="pattern in sampleStore.comparisonResult.matchedFaultPatterns"
              :key="pattern"
              class="px-2 py-1 bg-orange-600/20 text-orange-400 text-xs rounded border border-orange-500/30"
            >
              {{ pattern }}
            </span>
          </div>
        </div>

        <!-- Signal Comparison Table -->
        <div v-if="sampleStore.comparisonResult" class="p-3">
          <div class="text-xs font-medium text-gray-400 mb-2">信号详细对比</div>
          <div class="overflow-x-auto">
            <table class="w-full text-xs">
              <thead>
                <tr class="text-gray-500 border-b border-gray-700">
                  <th class="text-left py-2 px-2 font-medium">信号名称</th>
                  <th class="text-right py-2 px-2 font-medium">当前值</th>
                  <th class="text-right py-2 px-2 font-medium">样本值</th>
                  <th class="text-right py-2 px-2 font-medium">偏差</th>
                  <th class="text-right py-2 px-2 font-medium">偏差%</th>
                  <th class="text-center py-2 px-2 font-medium">状态</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="sc in sampleStore.comparisonResult.signalComparisons"
                  :key="sc.signalName"
                  class="border-b border-gray-700/50 transition-colors"
                  :class="{ 'bg-red-900/20': sc.isAbnormal }"
                >
                  <td class="py-2 px-2">
                    <span class="text-gray-200">{{ getSignalLabel(sc.signalName) }}</span>
                  </td>
                  <td class="py-2 px-2 text-right text-gray-300">
                    {{ formatValue(sc.currentValue, sc.signalName) }}
                  </td>
                  <td class="py-2 px-2 text-right text-gray-400">
                    {{ formatValue(sc.sampleValue, sc.signalName) }}
                  </td>
                  <td class="py-2 px-2 text-right" :class="sc.isAbnormal ? 'text-red-400 font-medium' : 'text-gray-400'">
                    {{ sc.deviation.toFixed(2) }}{{ getSignalUnit(sc.signalName) }}
                  </td>
                  <td class="py-2 px-2 text-right" :class="sc.isAbnormal ? 'text-red-400 font-medium' : 'text-gray-400'">
                    {{ sc.deviationPercent.toFixed(1) }}%
                  </td>
                  <td class="py-2 px-2 text-center">
                    <span v-if="sc.isAbnormal" class="inline-flex items-center justify-center w-5 h-5 bg-red-500 rounded-full">
                      <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </span>
                    <span v-else class="inline-flex items-center justify-center w-5 h-5 bg-green-500 rounded-full">
                      <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                      </svg>
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Bus Stats Comparison -->
        <div v-if="sampleStore.selectedSample" class="p-3 border-t border-gray-700">
          <div class="text-xs font-medium text-gray-400 mb-2">总线统计对比</div>
          <div class="grid grid-cols-2 gap-3 text-xs">
            <div class="p-2 bg-gray-700 rounded">
              <div class="text-gray-500 mb-1">总线负载</div>
              <div class="flex items-baseline gap-2">
                <span class="text-lg font-medium text-gray-200">{{ canbusStore.busStats.busLoad.toFixed(1) }}%</span>
                <span class="text-gray-500">/ {{ sampleStore.selectedSample.busStats.busLoad.toFixed(1) }}%</span>
              </div>
            </div>
            <div class="p-2 bg-gray-700 rounded">
              <div class="text-gray-500 mb-1">帧数对比</div>
              <div class="flex items-baseline gap-2">
                <span class="text-lg font-medium text-gray-200">{{ canbusStore.frames.length }}</span>
                <span class="text-gray-500">/ {{ sampleStore.selectedSample.frameCount }}</span>
              </div>
            </div>
            <div class="p-2 bg-gray-700 rounded">
              <div class="text-gray-500 mb-1">RX/TX</div>
              <div class="flex items-baseline gap-2">
                <span class="text-green-400">{{ canbusStore.busStats.rxCount }}</span>
                <span class="text-gray-500">/</span>
                <span class="text-blue-400">{{ canbusStore.busStats.txCount }}</span>
              </div>
            </div>
            <div class="p-2 bg-gray-700 rounded">
              <div class="text-gray-500 mb-1">错误帧</div>
              <div class="flex items-baseline gap-2">
                <span :class="canbusStore.busStats.errorCount > 0 ? 'text-red-400' : 'text-green-400'" class="text-lg font-medium">
                  {{ canbusStore.busStats.errorCount }}
                </span>
                <span class="text-gray-500">/ {{ sampleStore.selectedSample.busStats.errorCount }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Sample Signal Stats -->
        <div v-if="sampleStore.selectedSample && Object.keys(sampleStore.selectedSample.signalStats).length > 0" class="p-3 border-t border-gray-700">
          <div class="text-xs font-medium text-gray-400 mb-2">样本信号统计</div>
          <div class="space-y-2">
            <div
              v-for="(stats, name) in sampleStore.selectedSample.signalStats"
              :key="name"
              class="p-2 bg-gray-700 rounded"
            >
              <div class="flex items-center justify-between mb-1">
                <span class="text-xs font-medium text-gray-300">{{ getSignalLabel(name) }}</span>
                <span class="text-xs text-gray-500">n={{ stats.sampleCount }}</span>
              </div>
              <div class="grid grid-cols-4 gap-2 text-xs">
                <div>
                  <span class="text-gray-500">最小</span>
                  <span class="text-gray-300 ml-1">{{ stats.min.toFixed(1) }}</span>
                </div>
                <div>
                  <span class="text-gray-500">最大</span>
                  <span class="text-gray-300 ml-1">{{ stats.max.toFixed(1) }}</span>
                </div>
                <div>
                  <span class="text-gray-500">均值</span>
                  <span class="text-gray-300 ml-1">{{ stats.avg.toFixed(1) }}</span>
                </div>
                <div>
                  <span class="text-gray-500">标准差</span>
                  <span class="text-gray-300 ml-1">{{ stats.stdDev.toFixed(1) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
