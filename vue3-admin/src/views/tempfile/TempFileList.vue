<template>
  <div class="temp-file-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="File Name">
          <el-input v-model="queryParams.fileName" placeholder="Please enter file name" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-upload
          :show-file-list="false"
          :http-request="customUpload"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
        >
          <el-button type="primary" :loading="uploading">
            <el-icon><Upload /></el-icon>
            Upload
          </el-button>
        </el-upload>
        <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
          Batch Delete
        </el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="fileName" label="File Name" min-width="200" show-overflow-tooltip />
      <el-table-column prop="filePath" label="File Path" min-width="240" show-overflow-tooltip />
      <el-table-column prop="fileSize" label="Size" width="120">
        <template #default="{ row }">
          {{ formatSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="fileType" label="Type" width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="Create Time" width="180" />
      <el-table-column label="Actions" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleDownload(row)">Download</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
      style="margin-top: 20px"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { tempFileApi } from '@/api/tempFile'
import type { TempFile } from '@/api/tempFile'

const loading = ref(false)
const uploading = ref(false)
const tableData = ref<TempFile[]>([])
const total = ref(0)
const selectedRows = ref<TempFile[]>([])

const queryParams = reactive({
  fileName: '',
  pageNum: 1,
  pageSize: 10
})

const loadData = async () => {
  loading.value = true
  try {
    const result = await tempFileApi.page(queryParams)
    tableData.value = result.list || []
    total.value = result.total || 0
  } catch {
    ElMessage.error('Failed to load temp files')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.fileName = ''
  queryParams.pageNum = 1
  loadData()
}

const handleSelectionChange = (rows: TempFile[]) => {
  selectedRows.value = rows
}

const beforeUpload = (file: File) => {
  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('File size cannot exceed 100MB')
    return false
  }
  return true
}

const customUpload = async (options: any) => {
  const file: File = options.file
  uploading.value = true
  const formData = new FormData()
  formData.append('file', file)
  try {
    const result = await tempFileApi.upload(formData)
    options.onSuccess?.(result, file)
  } catch (e: any) {
    options.onError?.(e)
  } finally {
    uploading.value = false
  }
}

const handleUploadSuccess = () => {
  ElMessage.success('Uploaded successfully')
  loadData()
}

const handleUploadError = () => {
  ElMessage.error('Upload failed')
}

const handleDelete = async (row: TempFile) => {
  try {
    await ElMessageBox.confirm(`Delete temp file "${row.fileName}"?`, 'Confirm', { type: 'warning' })
    await tempFileApi.delete({ ids: [row.id!] })
    ElMessage.success('Deleted successfully')
    loadData()
  } catch {
    // cancelled
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('Please select files to delete')
    return
  }
  try {
    await ElMessageBox.confirm(
      `Delete ${selectedRows.value.length} temp files?`,
      'Confirm',
      { type: 'warning' }
    )
    const ids = selectedRows.value.map(r => r.id!)
    await tempFileApi.delete({ ids })
    ElMessage.success(`Deleted ${ids.length} temp files`)
    selectedRows.value = []
    loadData()
  } catch {
    // cancelled
  }
}

const handleDownload = async (row: TempFile) => {
  try {
    const blob = await tempFileApi.download(row.id!)
    const url = window.URL.createObjectURL(blob as unknown as Blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('Download failed')
  }
}

const formatSize = (bytes?: number) => {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  let value = bytes
  while (value >= 1024 && i < units.length - 1) {
    value /= 1024
    i++
  }
  return `${value.toFixed(2)} ${units[i]}`
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.temp-file-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.actions {
  text-align: right;
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>