<template>
  <div class="user-excel">
    <div class="toolbar">
      <span class="page-title">User Excel Import/Export</span>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" class="action-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#409eff"><Download /></el-icon>
            <div class="card-info">
              <div class="card-title">Export Users</div>
              <div class="card-desc">
                Download all users as an Excel file (.xlsx). Use this to back up user data or perform
                offline analysis.
              </div>
              <el-button
                type="primary"
                size="large"
                :loading="exporting"
                @click="handleExport"
                style="margin-top: 16px"
              >
                <el-icon><Download /></el-icon>
                Export Users
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
              <div class="card-title">Import Users</div>
              <div class="card-desc">
                Upload an Excel file (.xlsx) containing user data. Make sure the columns match the
                template format.
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
                  Import Users
                </el-button>
              </el-upload>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>Import Instructions</span>
      </template>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        <p>1. The Excel template must contain the following columns: <b>username</b>, <b>nickname</b>,
          <b>email</b>, <b>mobile</b>, <b>status</b>.</p>
        <p>2. Status values: <b>1</b> = Active, <b>0</b> = Disabled.</p>
        <p>3. Duplicate usernames will be skipped automatically.</p>
        <p>4. Maximum file size: 10MB.</p>
      </el-alert>

      <div class="history-section">
        <h4>Recent Import History</h4>
        <el-table :data="historyList" stripe size="small">
          <el-table-column prop="fileName" label="File Name" min-width="240" />
          <el-table-column prop="total" label="Total Rows" width="120" />
          <el-table-column prop="imported" label="Imported" width="120">
            <template #default="{ row }">
              <el-tag type="success" size="small">{{ row.imported }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="skipped" label="Skipped" width="120">
            <template #default="{ row }">
              <el-tag type="info" size="small">{{ row.skipped }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="user" label="Operator" width="140" />
          <el-table-column prop="time" label="Time" width="180" />
          <el-table-column prop="status" label="Status" width="100">
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Upload } from '@element-plus/icons-vue'
import { excelApi } from '@/api/excel'

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
    ElMessage.success('Export started')
  } catch {
    ElMessage.error('Export failed')
  } finally {
    exporting.value = false
  }
}

const beforeImport = (file: File) => {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('File size cannot exceed 10MB')
    return false
  }
  const isExcel = /\.(xlsx|xls)$/i.test(file.name)
  if (!isExcel) {
    ElMessage.error('Only Excel files are supported')
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
  ElMessage.success(`Imported ${response?.data ?? 0} users successfully`)
  loadHistory()
}

const handleImportError = () => {
  ElMessage.error('Import failed')
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