<template>
  <div class="server-monitor">
    <div class="toolbar">
      <span class="page-title">Server Monitor</span>
      <el-button type="primary" @click="loadData" :loading="loading">
        <el-icon><Refresh /></el-icon>
        Refresh
      </el-button>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#409eff"><Cpu /></el-icon>
            <span class="stat-label">CPU Usage</span>
          </div>
          <el-progress
            :percentage="Number(((serverInfo?.cpu?.usage || 0)).toFixed(2))"
            :color="getProgressColor(((serverInfo?.cpu?.usage || 0)))"
            :stroke-width="14"
          />
          <div class="stat-meta">
            Cores: {{ serverInfo?.cpu?.cpuNum ?? '-' }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#67c23a"><Histogram /></el-icon>
            <span class="stat-label">Memory Usage</span>
          </div>
          <el-progress
            :percentage="Number(((serverInfo?.mem?.usage || 0)).toFixed(2))"
            :color="getProgressColor((serverInfo?.mem?.usage || 0))"
            :stroke-width="14"
          />
          <div class="stat-meta">
            Used: {{ formatBytes(serverInfo?.mem?.used) }} / {{ formatBytes(serverInfo?.mem?.total) }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#e6a23c"><Coin /></el-icon>
            <span class="stat-label">JVM Memory</span>
          </div>
          <el-progress
            :percentage="Number(((serverInfo?.jvm?.usage || 0)).toFixed(2))"
            :color="getProgressColor((serverInfo?.jvm?.usage || 0))"
            :stroke-width="14"
          />
          <div class="stat-meta">
            Used: {{ formatBytes(serverInfo?.jvm?.used) }} / Max: {{ formatBytes(serverInfo?.jvm?.max) }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#f56c6c"><Files /></el-icon>
            <span class="stat-label">Disk Usage</span>
          </div>
          <el-progress
            :percentage="Number(((diskUsage || 0)).toFixed(2))"
            :color="getProgressColor(diskUsage || 0)"
            :stroke-width="14"
          />
          <div class="stat-meta">
            Used: {{ formatBytes(diskUsed) }} / {{ formatBytes(diskTotal) }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>CPU Info</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="CPU Cores">{{ serverInfo?.cpu?.cpuNum ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="Total">{{ formatBytes(serverInfo?.cpu?.total) }}</el-descriptions-item>
            <el-descriptions-item label="System">{{ formatBytes(serverInfo?.cpu?.sys) }}</el-descriptions-item>
            <el-descriptions-item label="Used">{{ formatBytes(serverInfo?.cpu?.used) }}</el-descriptions-item>
            <el-descriptions-item label="Wait">{{ formatBytes(serverInfo?.cpu?.wait) }}</el-descriptions-item>
            <el-descriptions-item label="Free">{{ formatBytes(serverInfo?.cpu?.free) }}</el-descriptions-item>
            <el-descriptions-item label="Usage">{{ (serverInfo?.cpu?.usage ?? 0).toFixed(2) }}%</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>Memory Info</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="Total">{{ formatBytes(serverInfo?.mem?.total) }}</el-descriptions-item>
            <el-descriptions-item label="Used">{{ formatBytes(serverInfo?.mem?.used) }}</el-descriptions-item>
            <el-descriptions-item label="Free">{{ formatBytes(serverInfo?.mem?.free) }}</el-descriptions-item>
            <el-descriptions-item label="Usage">{{ (serverInfo?.mem?.usage ?? 0).toFixed(2) }}%</el-descriptions-item>
            <el-descriptions-item label="JVM Total">{{ formatBytes(serverInfo?.jvm?.total) }}</el-descriptions-item>
            <el-descriptions-item label="JVM Max">{{ formatBytes(serverInfo?.jvm?.max) }}</el-descriptions-item>
            <el-descriptions-item label="JVM Free">{{ formatBytes(serverInfo?.jvm?.free) }}</el-descriptions-item>
            <el-descriptions-item label="JVM Version">{{ serverInfo?.jvm?.version || '-' }}</el-descriptions-item>
            <el-descriptions-item label="JVM Home">{{ serverInfo?.jvm?.home || '-' }}</el-descriptions-item>
            <el-descriptions-item label="JVM Name">{{ serverInfo?.jvm?.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="Start Time">{{ serverInfo?.jvm?.startTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="Run Time">{{ serverInfo?.jvm?.runTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>System Info</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="Computer Name">{{ serverInfo?.sys?.computerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="Computer IP">{{ serverInfo?.sys?.computerIp || '-' }}</el-descriptions-item>
            <el-descriptions-item label="OS Name">{{ serverInfo?.sys?.osName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="OS Arch">{{ serverInfo?.sys?.osArch || '-' }}</el-descriptions-item>
            <el-descriptions-item label="User Dir">{{ serverInfo?.sys?.userDir || '-' }}</el-descriptions-item>
            <el-descriptions-item label="User Name">{{ serverInfo?.sys?.userName || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>Disk Filesystems</span>
      </template>
      <el-table :data="serverInfo?.sysFile || []" stripe size="small">
        <el-table-column prop="dirName" label="Directory" min-width="200" />
        <el-table-column prop="sysTypeName" label="System Type" width="140" />
        <el-table-column prop="typeName" label="Type" width="120" />
        <el-table-column prop="total" label="Total" width="140" />
        <el-table-column prop="free" label="Free" width="140" />
        <el-table-column prop="used" label="Used" width="140" />
        <el-table-column prop="usage" label="Usage" width="240">
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
    ElMessage.error('Failed to load server info')
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