<template>
  <div class="cache-monitor">
    <div class="toolbar">
      <span class="page-title">Cache Monitor</span>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#409eff"><Coin /></el-icon>
            <span class="stat-label">Cache Count</span>
          </div>
          <div class="stat-value">{{ cacheNames.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#67c23a"><Refresh /></el-icon>
            <span class="stat-label">Actions</span>
          </div>
          <div class="action-row">
            <el-button type="primary" @click="loadNames" :loading="loading">
              <el-icon><Refresh /></el-icon>
              Refresh
            </el-button>
            <el-button type="danger" @click="handleClearAll">
              <el-icon><Delete /></el-icon>
              Clear All
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>Cache Names</span>
          </template>
          <div class="name-list" v-loading="loading">
            <div
              v-for="item in cacheNames"
              :key="item.cacheName"
              :class="['name-item', { active: selectedCache?.cacheName === item.cacheName }]"
              @click="selectCache(item)"
            >
              <div class="name-info">
                <el-icon><Coin /></el-icon>
                <span class="name-text">{{ item.cacheName }}</span>
              </div>
              <el-tag v-if="item.remark" size="small" type="info">{{ item.remark }}</el-tag>
            </div>
            <el-empty v-if="!loading && cacheNames.length === 0" description="No caches" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>
                <template v-if="selectedCache">
                  Keys in "{{ selectedCache.cacheName }}"
                </template>
                <template v-else>
                  Select a cache name to view keys
                </template>
              </span>
              <div v-if="selectedCache">
                <el-button size="small" type="danger" @click="handleClearCacheName">
                  <el-icon><Delete /></el-icon>
                  Clear Cache
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="!selectedCache" class="placeholder">
            <el-empty description="Select a cache on the left" />
          </div>
          <div v-else v-loading="keysLoading">
            <el-table :data="keys" stripe @row-click="selectKey">
              <el-table-column type="index" label="#" width="60" />
              <el-table-column prop="name" label="Key" min-width="280">
                <template #default="{ row }">
                  <el-link type="primary" :underline="false">{{ row }}</el-link>
                </template>
              </el-table-column>
              <el-table-column label="Actions" width="200">
                <template #default="{ row }">
                  <el-button size="small" @click.stop="selectKey(row)">View</el-button>
                  <el-button size="small" type="danger" @click.stop="handleClearKey(row)">Clear</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div v-if="currentValue" class="value-panel">
              <div class="value-title">
                <span>Value for: {{ currentValue.cacheKey }}</span>
                <el-button size="small" type="danger" @click="handleClearKey(currentValue.cacheKey)">Clear Key</el-button>
              </div>
              <pre class="value-body">{{ currentValue.cacheValue }}</pre>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coin, Refresh, Delete } from '@element-plus/icons-vue'
import { cacheApi } from '@/api/cache'
import type { SysCache } from '@/api/cache'

const loading = ref(false)
const keysLoading = ref(false)
const cacheNames = ref<SysCache[]>([])
const selectedCache = ref<SysCache | null>(null)
const keys = ref<string[]>([])
const currentValue = ref<SysCache | null>(null)

const loadNames = async () => {
  loading.value = true
  try {
    cacheNames.value = await cacheApi.getNames()
  } catch {
    ElMessage.error('Failed to load cache names')
  } finally {
    loading.value = false
  }
}

const selectCache = async (item: SysCache) => {
  selectedCache.value = item
  currentValue.value = null
  keysLoading.value = true
  try {
    keys.value = await cacheApi.getKeys(item.cacheName)
  } catch {
    ElMessage.error('Failed to load cache keys')
  } finally {
    keysLoading.value = false
  }
}

const selectKey = async (key: string) => {
  if (!selectedCache.value) return
  try {
    currentValue.value = await cacheApi.getValue(selectedCache.value.cacheName, key)
  } catch {
    ElMessage.error('Failed to load cache value')
  }
}

const handleClearKey = async (key: string) => {
  try {
    await ElMessageBox.confirm(`Clear cache key "${key}"?`, 'Confirm', { type: 'warning' })
    await cacheApi.clearCacheKey(key)
    ElMessage.success('Cache key cleared')
    if (selectedCache.value) {
      keys.value = await cacheApi.getKeys(selectedCache.value.cacheName)
    }
    currentValue.value = null
  } catch {
    // cancelled
  }
}

const handleClearCacheName = async () => {
  if (!selectedCache.value) return
  try {
    await ElMessageBox.confirm(
      `Clear entire cache "${selectedCache.value.cacheName}"?`,
      'Confirm',
      { type: 'warning' }
    )
    await cacheApi.clearCacheName(selectedCache.value.cacheName)
    ElMessage.success('Cache cleared')
    keys.value = []
    currentValue.value = null
  } catch {
    // cancelled
  }
}

const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('Clear ALL caches?', 'Confirm', { type: 'warning' })
    await cacheApi.clearCacheAll()
    ElMessage.success('All caches cleared')
    cacheNames.value = []
    selectedCache.value = null
    keys.value = []
    currentValue.value = null
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadNames()
})
</script>

<style scoped>
.cache-monitor {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.stat-card {
  height: 110px;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.stat-icon {
  font-size: 20px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.action-row {
  display: flex;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name-list {
  max-height: 600px;
  overflow-y: auto;
}

.name-item {
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
}

.name-item:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.name-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.name-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-size: 14px;
  font-weight: 500;
}

.placeholder {
  padding: 60px 0;
}

.value-panel {
  margin-top: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.value-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 500;
}

.value-body {
  padding: 14px;
  background: #fafafa;
  margin: 0;
  max-height: 320px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}
</style>