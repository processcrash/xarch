<template>
  <div class="server-monitor">
    <div class="toolbar">
      <span class="page-title">{{ t('monitor.server.title') }}</span>
      <el-button type="primary" @click="loadData" :loading="loading">
        <el-icon><Refresh /></el-icon>
        {{ t('common.refresh') }}
      </el-button>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#409eff"><Cpu /></el-icon>
            <span class="stat-label">{{ t('monitor.server.cpuUsage') }}</span>
          </div>
          <el-progress
            :percentage="Number(((serverInfo?.cpu?.usage || 0)).toFixed(2))"
            :color="getProgressColor(((serverInfo?.cpu?.usage || 0)))"
            :stroke-width="14"
          />
          <div class="stat-meta">
            {{ t('monitor.server.cores') }}: {{ serverInfo?.cpu?.cpuNum ?? '-' }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#67c23a"><Histogram /></el-icon>
            <span class="stat-label">{{ t('monitor.server.memoryUsage') }}</span>
          </div>
          <el-progress
            :percentage="Number(((serverInfo?.mem?.usage || 0)).toFixed(2))"
            :color="getProgressColor((serverInfo?.mem?.usage || 0))"
            :stroke-width="14"
          />
          <div class="stat-meta">
            {{ t('monitor.server.used') }}: {{ formatBytes(serverInfo?.mem?.used) }} / {{ formatBytes(serverInfo?.mem?.total) }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#e6a23c"><Coin /></el-icon>
            <span class="stat-label">{{ t('monitor.server.jvmMemory') }}</span>
          </div>
          <el-progress
            :percentage="Number(((serverInfo?.jvm?.usage || 0)).toFixed(2))"
            :color="getProgressColor((serverInfo?.jvm?.usage || 0))"
            :stroke-width="14"
          />
          <div class="stat-meta">
            {{ t('monitor.server.used') }}: {{ formatBytes(serverInfo?.jvm?.used) }} / Max: {{ formatBytes(serverInfo?.jvm?.max) }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#f56c6c"><Files /></el-icon>
            <span class="stat-label">{{ t('monitor.server.diskUsage') }}</span>
          </div>
          <el-progress
            :percentage="Number(((diskUsage || 0)).toFixed(2))"
            :color="getProgressColor(diskUsage || 0)"
            :stroke-width="14"
          />
          <div class="stat-meta">
            {{ t('monitor.server.used') }}: {{ formatBytes(diskUsed) }} / {{ formatBytes(diskTotal) }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>{{ t('monitor.server.cpuInfo') }}</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item :label="t('monitor.server.cpuCores')">{{ serverInfo?.cpu?.cpuNum ?? '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.total')">{{ formatBytes(serverInfo?.cpu?.total) }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.system')">{{ formatBytes(serverInfo?.cpu?.sys) }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.used')">{{ formatBytes(serverInfo?.cpu?.used) }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.wait')">{{ formatBytes(serverInfo?.cpu?.wait) }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.free')">{{ formatBytes(serverInfo?.cpu?.free) }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.usage')">{{ (serverInfo?.cpu?.usage ?? 0).toFixed(2) }}%</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>{{ t('monitor.server.memoryInfo') }}</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item :label="t('monitor.server.total')">{{ formatBytes(serverInfo?.mem?.total) }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.used')">{{ formatBytes(serverInfo?.mem?.used) }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.free')">{{ formatBytes(serverInfo?.mem?.free) }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.usage')">{{ (serverInfo?.mem?.usage ?? 0).toFixed(2) }}%</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.jvmTotal')">{{ formatBytes(serverInfo?.jvm?.total) }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.jvmMax')">{{ formatBytes(serverInfo?.jvm?.max) }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.jvmFree')">{{ formatBytes(serverInfo?.jvm?.free) }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.jvmVersion')">{{ serverInfo?.jvm?.version || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.jvmHome')">{{ serverInfo?.jvm?.home || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.jvmName')">{{ serverInfo?.jvm?.name || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.startTime')">{{ serverInfo?.jvm?.startTime || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.runTime')">{{ serverInfo?.jvm?.runTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>{{ t('monitor.server.systemInfo') }}</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item :label="t('monitor.server.computerName')">{{ serverInfo?.sys?.computerName || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.computerIp')">{{ serverInfo?.sys?.computerIp || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.osName')">{{ serverInfo?.sys?.osName || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.osArch')">{{ serverInfo?.sys?.osArch || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.userDir')">{{ serverInfo?.sys?.userDir || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.server.userName')">{{ serverInfo?.sys?.userName || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>{{ t('monitor.server.diskFilesystems') }}</span>
      </template>
      <el-table :data="serverInfo?.sysFile || []" stripe size="small">
        <el-table-column prop="dirName" :label="t('monitor.server.directory')" min-width="200" />
        <el-table-column prop="sysTypeName" :label="t('monitor.server.systemType')" width="140" />
        <el-table-column prop="typeName" :label="t('monitor.server.type')" width="120" />
        <el-table-column prop="total" :label="t('monitor.server.total')" width="140" />
        <el-table-column prop="free" :label="t('monitor.server.free')" width="140" />
        <el-table-column prop="used" :label="t('monitor.server.used')" width="140" />
        <el-table-column prop="usage" :label="t('monitor.server.usage')" width="240">
          <template #default="{ row }">
            <el-progress :percentage="Number((row.usage ?? 0).toFixed(2))" :color="getProgressColor(row.usage || 0)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Cpu, Histogram, Coin, Files } from '@element-plus/icons-vue'
import { serverApi } from '@/api/server'
import type { Server, SysFileInfo } from '@/api/server'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()
const loading = ref(false)
const serverInfo = ref<Server | null>(null)

const diskUsage = computed(() => {
  const files: SysFileInfo[] = serverInfo.value?.sysFile || []
  if (files.length === 0) return 0
  const totalUsed = files.reduce((acc, f) => acc + parseBytes(f.used), 0)
  const totalAll = files.reduce((acc, f) => acc + parseBytes(f.total), 0)
  if (totalAll <= 0) return 0
  return (totalUsed / totalAll) * 100
})

const diskUsed = computed(() => {
  const files: SysFileInfo[] = serverInfo.value?.sysFile || []
  return files.reduce((acc, f) => acc + parseBytes(f.used), 0)
})

const diskTotal = computed(() => {
  const files: SysFileInfo[] = serverInfo.value?.sysFile || []
  return files.reduce((acc, f) => acc + parseBytes(f.total), 0)
})

const loadData = async () => {
  loading.value = true
  try {
    serverInfo.value = await serverApi.getInfo()
  } catch {
    ElMessage.error(t('monitor.server.loadFailed'))
  } finally {
    loading.value = false
  }
}

const formatBytes = (bytes?: number) => {
  if (bytes === undefined || bytes === null) return '-'
  const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
  let i = 0
  let value = bytes
  while (value >= 1024 && i < units.length - 1) {
    value /= 1024
    i++
  }
  return `${value.toFixed(2)} ${units[i]}`
}

const parseBytes = (input?: string | number): number => {
  if (input === undefined || input === null) return 0
  if (typeof input === 'number') return input
  const num = parseFloat(input)
  return isNaN(num) ? 0 : num
}

const getProgressColor = (percentage: number) => {
  if (percentage > 80) return '#f56c6c'
  if (percentage > 60) return '#e6a23c'
  return '#67c23a'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.server-monitor {
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
  height: 140px;
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

.stat-meta {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
