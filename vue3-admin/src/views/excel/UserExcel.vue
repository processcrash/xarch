<template>
  <div class="user-excel">
    <div class="toolbar">
      <span class="page-title">{{ t('excel.title') }}</span>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" class="action-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#409eff"><Download /></el-icon>
            <div class="card-info">
              <div class="card-title">{{ t('excel.exportUsers') }}</div>
              <div class="card-desc">
                {{ t('excel.exportDesc') }}
              </div>
              <el-button
                type="primary"
                size="large"
                :loading="exporting"
                @click="handleExport"
                style="margin-top: 16px"
              >
                <el-icon><Download /></el-icon>
                {{ t('excel.exportUsers') }}
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="action-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#67c23a"><Upload /></el-icon>
            <div class="card-info">
              <div class="card-title">{{ t('excel.importUsers') }}</div>
              <div class="card-desc">
                {{ t('excel.importDesc') }}
              </div>
              <el-upload
                class="upload-area"
                :show-file-list="false"
                :http-request="customImport"
                :before-upload="beforeImport"
                :on-success="handleImportSuccess"
                :on-error="handleImportError"
              >
                <el-button type="success" size="large" :loading="importing">
                  <el-icon><Upload /></el-icon>
                  {{ t('excel.importUsers') }}
                </el-button>
              </el-upload>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>{{ t('excel.importInstructions') }}</span>
      </template>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        <p v-html="renderedInstruction1"></p>
        <p>{{ t('excel.instruction2') }}</p>
        <p>{{ t('excel.instruction3') }}</p>
        <p>{{ t('excel.instruction4') }}</p>
      </el-alert>

      <div class="history-section">
        <h4>{{ t('excel.recentHistory') }}</h4>
        <el-table :data="historyList" stripe size="small">
          <el-table-column prop="fileName" :label="t('excel.fileName')" min-width="240" />
          <el-table-column prop="total" :label="t('excel.totalRows')" width="120" />
          <el-table-column prop="imported" :label="t('excel.imported')" width="120">
            <template #default="{ row }">
              <el-tag type="success" size="small">{{ row.imported }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="skipped" :label="t('excel.skipped')" width="120">
            <template #default="{ row }">
              <el-tag type="info" size="small">{{ row.skipped }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="user" :label="t('excel.operator')" width="140" />
          <el-table-column prop="time" :label="t('excel.time')" width="180" />
          <el-table-column :label="t('excel.status')" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Upload } from '@element-plus/icons-vue'
import { excelApi } from '@/api/excel'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()
const exporting = ref(false)
const importing = ref(false)

interface ImportHistory {
  fileName: string
  total: number
  imported: number
  skipped: number
  user: string
  time: string
  status: 'SUCCESS' | 'FAILED'
}

const historyList = ref<ImportHistory[]>([])

const renderedInstruction1 = computed(() => t('excel.instruction1'))

const loadHistory = () => {
  // Placeholder: actual import history endpoint can be wired in later.
  historyList.value = [
    {
      fileName: 'users-2025-12-01.xlsx',
      total: 120,
      imported: 115,
      skipped: 5,
      user: 'admin',
      time: '2025-12-01 10:32:18',
      status: 'SUCCESS'
    },
    {
      fileName: 'users-2025-11-28.xlsx',
      total: 88,
      imported: 88,
      skipped: 0,
      user: 'admin',
      time: '2025-11-28 16:14:02',
      status: 'SUCCESS'
    }
  ]
}

const handleExport = async () => {
  exporting.value = true
  try {
    const blob = await excelApi.exportUsers()
    const url = window.URL.createObjectURL(blob as unknown as Blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `users-${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success(t('excel.exportStarted'))
  } catch {
    ElMessage.error(t('excel.exportFailed'))
  } finally {
    exporting.value = false
  }
}

const beforeImport = (file: File) => {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error(t('excel.fileSizeLimit'))
    return false
  }
  const isExcel = /\.(xlsx|xls)$/i.test(file.name)
  if (!isExcel) {
    ElMessage.error(t('excel.excelOnly'))
    return false
  }
  return true
}

const customImport = async (options: any) => {
  importing.value = true
  const file: File = options.file
  const formData = new FormData()
  formData.append('file', file)
  try {
    const result = await excelApi.importUsers(formData)
    options.onSuccess?.(result, file)
  } catch (e: any) {
    options.onError?.(e)
  } finally {
    importing.value = false
  }
}

const handleImportSuccess = (response: any) => {
  ElMessage.success(t('excel.importedSuccess', { count: response?.data ?? 0 }))
  loadHistory()
}

const handleImportError = () => {
  ElMessage.error(t('excel.importFailed'))
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.user-excel {
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

.action-card {
  height: 220px;
}

.card-content {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 10px;
}

.card-icon {
  font-size: 56px;
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
}

.card-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.upload-area {
  margin-top: 16px;
}

.history-section h4 {
  margin-bottom: 12px;
}
</style>
