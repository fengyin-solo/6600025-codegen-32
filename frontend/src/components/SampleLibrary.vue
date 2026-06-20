<script setup lang="ts">
import { ref, computed } from 'vue';
import { useCanBusStore } from '../store/canbus';
import { useSampleLibraryStore } from '../store/sampleLibrary';
import type { SaveSampleOptions } from '../types';

const canbusStore = useCanBusStore();
const sampleStore = useSampleLibraryStore();

const showSaveDialog = ref(false);
const saveForm = ref<SaveSampleOptions>({
  name: '',
  description: '',
  tags: [],
  faultType: 'normal'
});
const newTag = ref('');
const activeTab = ref<'list' | 'save'>('list');

const faultTypeLabels: Record<string, string> = {
  normal: '正常工况',
  overheat: '过热故障',
  high_load: '高负载故障',
  erratic_rpm: '转速波动',
  communication_error: '通信错误',
  custom: '自定义'
};

const faultTypeColors: Record<string, string> = {
  normal: 'bg-green-500',
  overheat: 'bg-red-500',
  high_load: 'bg-orange-500',
  erratic_rpm: 'bg-yellow-500',
  communication_error: 'bg-purple-500',
  custom: 'bg-gray-500'
};

const canSave = computed(() => {
  return canbusStore.frames.length > 0 && saveForm.value.name.trim().length > 0;
});

function openSaveDialog() {
  saveForm.value = {
    name: `样本_${new Date().toLocaleString('zh-CN')}`,
    description: '',
    tags: [],
    faultType: 'normal'
  };
  showSaveDialog.value = true;
  activeTab.value = 'save';
}

function addTag() {
  const tag = newTag.value.trim();
  if (tag && !saveForm.value.tags.includes(tag)) {
    saveForm.value.tags.push(tag);
    newTag.value = '';
  }
}

function removeTag(tag: string) {
  const index = saveForm.value.tags.indexOf(tag);
  if (index > -1) {
    saveForm.value.tags.splice(index, 1);
  }
}

function handleSaveSample() {
  if (!canSave.value) return;
  sampleStore.saveCurrentSession(
    canbusStore.frames,
    canbusStore.busStats,
    { ...saveForm.value }
  );
  showSaveDialog.value = false;
  activeTab.value = 'list';
}

function handleDeleteSample(sampleId: string) {
  if (confirm('确定要删除这个样本吗？')) {
    sampleStore.deleteSample(sampleId);
  }
}

function handleSelectSample(sampleId: string) {
  if (sampleStore.selectedSampleId === sampleId) {
    sampleStore.selectSample(null);
  } else {
    sampleStore.selectSample(sampleId);
    if (canbusStore.frames.length > 0) {
      const sample = sampleStore.samples.find(s => s.id === sampleId);
      if (sample) {
        sampleStore.compareWithSample(sample, canbusStore.frames, canbusStore.busStats);
      }
    }
  }
}

function handleAutoMatch() {
  if (canbusStore.frames.length === 0) {
    alert('请先开始捕获数据');
    return;
  }
  const match = sampleStore.findMostSimilarSample(canbusStore.frames, canbusStore.busStats);
  if (match) {
    sampleStore.selectSample(match.sample.id);
    alert(`最佳匹配: ${match.sample.name}\n相似度: ${match.similarity}%`);
  }
}

function handleExportSample(sampleId: string) {
  const sample = sampleStore.samples.find(s => s.id === sampleId);
  if (!sample) return;
  const json = sampleStore.exportSample(sample);
  const blob = new Blob([json], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `sample_${sample.name.replace(/\s+/g, '_')}_${Date.now()}.json`;
  a.click();
  URL.revokeObjectURL(url);
}

function handleImportSample() {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = '.json';
  input.onchange = (e) => {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (ev) => {
      const result = sampleStore.importSample(ev.target?.result as string);
      if (result) {
        alert(`成功导入样本: ${result.name}`);
      } else {
        alert('导入失败，请检查文件格式');
      }
    };
    reader.readAsText(file);
  };
  input.click();
}

function formatDate(timestamp: number): string {
  return new Date(timestamp).toLocaleString('zh-CN');
}

function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms}ms`;
  return `${(ms / 1000).toFixed(1)}s`;
}
</script>

<template>
  <div class="h-full flex flex-col bg-gray-800 text-gray-100">
    <!-- Header -->
    <div class="px-4 py-3 border-b border-gray-700 flex items-center justify-between shrink-0">
      <h2 class="text-sm font-semibold text-gray-200">离线样本库</h2>
      <div class="flex items-center gap-1">
        <button
          @click="handleAutoMatch"
          class="px-2 py-1 text-xs bg-cyan-600 hover:bg-cyan-500 text-white rounded transition-colors"
          :disabled="canbusStore.frames.length === 0"
          :class="{ 'opacity-50 cursor-not-allowed': canbusStore.frames.length === 0 }"
        >
          智能匹配
        </button>
        <button
          @click="openSaveDialog"
          class="px-2 py-1 text-xs bg-green-600 hover:bg-green-500 text-white rounded transition-colors"
          :disabled="canbusStore.frames.length === 0"
          :class="{ 'opacity-50 cursor-not-allowed': canbusStore.frames.length === 0 }"
        >
          保存当前
        </button>
        <button
          @click="handleImportSample"
          class="px-2 py-1 text-xs bg-gray-600 hover:bg-gray-500 text-white rounded transition-colors"
        >
          导入
        </button>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex border-b border-gray-700 shrink-0">
      <button
        @click="activeTab = 'list'"
        class="flex-1 px-4 py-2 text-xs font-medium transition-colors"
        :class="activeTab === 'list' ? 'text-cyan-400 border-b-2 border-cyan-400' : 'text-gray-400 hover:text-gray-200'"
      >
        样本列表 ({{ sampleStore.samples.length }})
      </button>
      <button
        @click="activeTab = 'save'"
        class="flex-1 px-4 py-2 text-xs font-medium transition-colors"
        :class="activeTab === 'save' ? 'text-cyan-400 border-b-2 border-cyan-400' : 'text-gray-400 hover:text-gray-200'"
      >
        保存样本
      </button>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto">
      <!-- Sample List Tab -->
      <div v-if="activeTab === 'list'" class="p-2 space-y-2">
        <template v-for="(typeSamples, type) in sampleStore.samplesByType" :key="type">
          <div class="mb-2">
            <div class="flex items-center gap-2 px-2 py-1 bg-gray-750 rounded-t">
              <span :class="['w-2 h-2 rounded-full', faultTypeColors[type]]"></span>
              <span class="text-xs font-medium text-gray-300">{{ faultTypeLabels[type] }}</span>
              <span class="text-xs text-gray-500">({{ typeSamples.length }})</span>
            </div>
            <div class="space-y-1">
              <div
                v-for="sample in typeSamples"
                :key="sample.id"
                class="p-2 bg-gray-700 rounded border transition-colors cursor-pointer"
                :class="{
                  'border-cyan-500 bg-gray-650': sampleStore.selectedSampleId === sample.id,
                  'border-transparent hover:border-gray-600': sampleStore.selectedSampleId !== sample.id
                }"
                @click="handleSelectSample(sample.id)"
              >
                <div class="flex items-start justify-between gap-2">
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-medium text-gray-100 truncate">{{ sample.name }}</span>
                      <span v-if="sample.id.startsWith('builtin-')" class="text-xs text-cyan-400 shrink-0">内置</span>
                    </div>
                    <p class="text-xs text-gray-400 mt-0.5 line-clamp-2">{{ sample.description }}</p>
                    <div class="flex items-center gap-3 mt-1 text-xs text-gray-500">
                      <span>{{ sample.frameCount }}帧</span>
                      <span>{{ formatDuration(sample.durationMs) }}</span>
                    </div>
                    <div class="flex items-center gap-1 mt-1 flex-wrap">
                      <span
                        v-for="tag in sample.tags"
                        :key="tag"
                        class="px-1.5 py-0.5 bg-gray-600 text-gray-300 text-xs rounded"
                      >
                        {{ tag }}
                      </span>
                    </div>
                  </div>
                  <div class="flex flex-col gap-1 shrink-0">
                    <button
                      @click.stop="handleExportSample(sample.id)"
                      class="p-1 text-gray-400 hover:text-white hover:bg-gray-600 rounded transition-colors"
                      title="导出样本"
                    >
                      <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                      </svg>
                    </button>
                    <button
                      v-if="!sample.id.startsWith('builtin-')"
                      @click.stop="handleDeleteSample(sample.id)"
                      class="p-1 text-gray-400 hover:text-red-400 hover:bg-gray-600 rounded transition-colors"
                      title="删除样本"
                    >
                      <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </div>
                <div class="text-xs text-gray-500 mt-1 pt-1 border-t border-gray-600">
                  {{ formatDate(sample.createdAt) }}
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- Save Sample Tab -->
      <div v-if="activeTab === 'save'" class="p-4 space-y-4">
        <div v-if="canbusStore.frames.length === 0" class="text-center py-8 text-gray-500">
          <svg class="w-12 h-12 mx-auto mb-3 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <p class="text-sm">暂无数据可保存</p>
          <p class="text-xs mt-1">请先开始捕获CAN总线数据</p>
        </div>

        <template v-else>
          <div class="bg-gray-700 rounded-lg p-3 mb-4">
            <div class="text-xs text-gray-400 mb-2">当前会话摘要</div>
            <div class="grid grid-cols-2 gap-2 text-sm">
              <div>
                <span class="text-gray-500">帧数:</span>
                <span class="text-gray-200 ml-1">{{ canbusStore.frames.length }}</span>
              </div>
              <div>
                <span class="text-gray-500">时长:</span>
                <span class="text-gray-200 ml-1">
                  {{ canbusStore.frames.length > 1 
                    ? formatDuration(canbusStore.frames[canbusStore.frames.length - 1].timestamp - canbusStore.frames[0].timestamp)
                    : '-' }}
                </span>
              </div>
              <div>
                <span class="text-gray-500">RX:</span>
                <span class="text-green-400 ml-1">{{ canbusStore.busStats.rxCount }}</span>
              </div>
              <div>
                <span class="text-gray-500">TX:</span>
                <span class="text-blue-400 ml-1">{{ canbusStore.busStats.txCount }}</span>
              </div>
            </div>
          </div>

          <div>
            <label class="block text-xs font-medium text-gray-400 mb-1">样本名称 *</label>
            <input
              v-model="saveForm.name"
              type="text"
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-sm text-gray-100 focus:outline-none focus:border-cyan-500 transition-colors"
              placeholder="请输入样本名称"
            />
          </div>

          <div>
            <label class="block text-xs font-medium text-gray-400 mb-1">故障类型</label>
            <select
              v-model="saveForm.faultType"
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-sm text-gray-100 focus:outline-none focus:border-cyan-500 transition-colors"
            >
              <option v-for="(label, key) in faultTypeLabels" :key="key" :value="key">
                {{ label }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-xs font-medium text-gray-400 mb-1">描述</label>
            <textarea
              v-model="saveForm.description"
              rows="3"
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded text-sm text-gray-100 focus:outline-none focus:border-cyan-500 transition-colors resize-none"
              placeholder="请输入样本描述，如故障现象、采集条件等"
            ></textarea>
          </div>

          <div>
            <label class="block text-xs font-medium text-gray-400 mb-1">标签</label>
            <div class="flex items-center gap-2">
              <input
                v-model="newTag"
                type="text"
                class="flex-1 px-3 py-2 bg-gray-700 border border-gray-600 rounded text-sm text-gray-100 focus:outline-none focus:border-cyan-500 transition-colors"
                placeholder="输入标签后按回车添加"
                @keyup.enter="addTag"
              />
              <button
                @click="addTag"
                class="px-3 py-2 bg-gray-600 hover:bg-gray-500 text-sm rounded transition-colors"
              >
                添加
              </button>
            </div>
            <div class="flex flex-wrap gap-1 mt-2">
              <span
                v-for="tag in saveForm.tags"
                :key="tag"
                class="inline-flex items-center gap-1 px-2 py-1 bg-cyan-600 text-white text-xs rounded"
              >
                {{ tag }}
                <button @click="removeTag(tag)" class="hover:text-cyan-200">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </span>
            </div>
          </div>

          <button
            @click="handleSaveSample"
            class="w-full py-2 bg-green-600 hover:bg-green-500 disabled:bg-gray-600 disabled:cursor-not-allowed text-white text-sm font-medium rounded transition-colors"
            :disabled="!canSave"
          >
            保存到样本库
          </button>
        </template>
      </div>
    </div>
  </div>
</template>
