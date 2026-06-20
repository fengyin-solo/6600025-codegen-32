<script setup lang="ts">
import { ref } from 'vue';
import { useCanBusStore } from './store/canbus';
import FrameTable from './components/FrameTable.vue';
import SignalChart from './components/SignalChart.vue';
import SampleLibrary from './components/SampleLibrary.vue';
import SampleComparison from './components/SampleComparison.vue';
import { useSampleLibraryStore } from './store/sampleLibrary';

const canbusStore = useCanBusStore();
const sampleStore = useSampleLibraryStore();

const rightPanelTab = ref<'chart' | 'comparison'>('chart');
const showSampleLibrary = ref(true);

function handleLoadDbc() {
  canbusStore.loadMockDbc();
  alert(`已加载 DBC 定义: ${canbusStore.dbcMessages.size} 条消息`);
}

function handleExport() {
  const csv = canbusStore.exportFrames();
  const blob = new Blob([csv], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `can_frames_${Date.now()}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}

function handleExportAllSamples() {
  const json = sampleStore.exportAllSamples();
  const blob = new Blob([json], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `canbus_samples_${Date.now()}.json`;
  a.click();
  URL.revokeObjectURL(url);
}

function handleImportAllSamples() {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = '.json';
  input.onchange = (e) => {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (ev) => {
      const imported = sampleStore.importAllSamples(ev.target?.result as string);
      alert(`成功导入 ${imported.length} 个样本`);
    };
    reader.readAsText(file);
  };
  input.click();
}
</script>

<template>
  <div class="h-screen flex flex-col bg-gray-900 text-gray-100 overflow-hidden">
    <!-- Header -->
    <header class="flex items-center justify-between px-6 py-3 bg-gray-800 border-b border-gray-700 shrink-0">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 bg-cyan-600 rounded-lg flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
          </svg>
        </div>
        <h1 class="text-lg font-bold text-gray-100">CAN 总线数据帧解析与诊断仪</h1>
      </div>

      <div class="flex items-center gap-2">
        <button
          @click="handleLoadDbc"
          class="px-3 py-1.5 bg-gray-700 hover:bg-gray-600 text-gray-200 text-sm rounded transition-colors border border-gray-600"
        >
          加载DBC
        </button>
        <button
          @click="canbusStore.isCapturing ? canbusStore.stopCapture() : canbusStore.startCapture()"
          class="px-3 py-1.5 text-sm rounded transition-colors font-medium"
          :class="canbusStore.isCapturing
            ? 'bg-red-600 hover:bg-red-700 text-white'
            : 'bg-green-600 hover:bg-green-700 text-white'"
        >
          {{ canbusStore.isCapturing ? '停止捕获' : '开始捕获' }}
        </button>
        <button
          @click="canbusStore.clearFrames()"
          class="px-3 py-1.5 bg-gray-700 hover:bg-gray-600 text-gray-200 text-sm rounded transition-colors border border-gray-600"
        >
          清除
        </button>
        <button
          @click="handleExport"
          class="px-3 py-1.5 bg-gray-700 hover:bg-gray-600 text-gray-200 text-sm rounded transition-colors border border-gray-600"
        >
          导出CSV
        </button>
        <div class="w-px h-6 bg-gray-600 mx-1"></div>
        <button
          @click="showSampleLibrary = !showSampleLibrary"
          class="px-3 py-1.5 text-sm rounded transition-colors border"
          :class="showSampleLibrary
            ? 'bg-cyan-600 hover:bg-cyan-700 text-white border-cyan-500'
            : 'bg-gray-700 hover:bg-gray-600 text-gray-200 border-gray-600'"
        >
          样本库
        </button>
        <button
          @click="handleImportAllSamples"
          class="px-3 py-1.5 bg-gray-700 hover:bg-gray-600 text-gray-200 text-sm rounded transition-colors border border-gray-600"
          title="导入全部样本"
        >
          导入样本
        </button>
        <button
          @click="handleExportAllSamples"
          class="px-3 py-1.5 bg-gray-700 hover:bg-gray-600 text-gray-200 text-sm rounded transition-colors border border-gray-600"
          title="导出全部样本"
        >
          导出样本
        </button>
      </div>
    </header>

    <!-- Main Area -->
    <main class="flex-1 flex overflow-hidden">
      <!-- Left Panel: Sample Library (25%) -->
      <div
        v-if="showSampleLibrary"
        class="w-1/4 border-r border-gray-700 flex flex-col overflow-hidden transition-all duration-300"
      >
        <SampleLibrary />
      </div>

      <!-- Middle Panel: Frame Table (45%) -->
      <div
        class="border-r border-gray-700 flex flex-col overflow-hidden"
        :class="showSampleLibrary ? 'w-[45%]' : 'w-[60%]'"
      >
        <FrameTable />
      </div>

      <!-- Right Panel: Signal Chart / Comparison (30%) -->
      <div
        class="flex flex-col overflow-hidden"
        :class="showSampleLibrary ? 'w-[30%]' : 'w-[40%]'"
      >
        <!-- Tabs -->
        <div class="flex border-b border-gray-700 shrink-0">
          <button
            @click="rightPanelTab = 'chart'"
            class="flex-1 px-4 py-2 text-xs font-medium transition-colors"
            :class="rightPanelTab === 'chart' ? 'text-cyan-400 border-b-2 border-cyan-400' : 'text-gray-400 hover:text-gray-200'"
          >
            信号波形
          </button>
          <button
            @click="rightPanelTab = 'comparison'"
            class="flex-1 px-4 py-2 text-xs font-medium transition-colors relative"
            :class="rightPanelTab === 'comparison' ? 'text-cyan-400 border-b-2 border-cyan-400' : 'text-gray-400 hover:text-gray-200'"
          >
            样本对比
            <span
              v-if="sampleStore.comparisonResult && sampleStore.comparisonResult.abnormalSignals.length > 0"
              class="absolute top-1 right-4 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center"
            >
              {{ sampleStore.comparisonResult.abnormalSignals.length }}
            </span>
          </button>
        </div>

        <!-- Tab Content -->
        <div class="flex-1 overflow-hidden">
          <div v-show="rightPanelTab === 'chart'" class="h-full">
            <SignalChart />
          </div>
          <div v-show="rightPanelTab === 'comparison'" class="h-full">
            <SampleComparison />
          </div>
        </div>
      </div>
    </main>

    <!-- Status Bar -->
    <footer class="flex items-center justify-between px-6 py-1.5 bg-gray-800 border-t border-gray-700 text-xs shrink-0">
      <div class="flex items-center gap-4 text-gray-500">
        <span>
          <span :class="canbusStore.isCapturing ? 'text-green-400' : 'text-gray-500'">
            ● {{ canbusStore.isCapturing ? '捕获中' : '已停止' }}
          </span>
        </span>
        <span>DBC消息: {{ canbusStore.dbcMessages.size }}</span>
        <span>样本库: {{ sampleStore.samples.length }} 个样本</span>
        <span v-if="sampleStore.selectedSample" class="text-cyan-400">
          已选: {{ sampleStore.selectedSample.name }}
        </span>
      </div>
      <div class="flex items-center gap-4 text-gray-500">
        <span>帧数: {{ canbusStore.busStats.totalFrames }}</span>
        <span>RX: {{ canbusStore.busStats.rxCount }}</span>
        <span>TX: {{ canbusStore.busStats.txCount }}</span>
        <span>负载: {{ canbusStore.busLoadPercent }}%</span>
      </div>
    </footer>
  </div>
</template>
