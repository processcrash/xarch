<template>
  <div class="resource-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Scene Code">
          <el-input v-model="queryParams.sceneCode" placeholder="Scene code" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="Storage Type">
          <el-select v-model="queryParams.storageType" placeholder="All" clearable style="width: 150px">
            <el-option label="All" value="" />
            <el-option label="Local" value="local" />
            <el-option label="MinIO" value="minio" />
            <el-option label="OSS" value="oss" />
          </el-select>
        </el-form-item>
        <el-form-item label="Keyword">
          <el-input v-model="queryParams.keyword" placeholder="File name" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="showUploadDialog">
          <el-icon><Upload /></el-icon>
          Upload File
        </el-button>
        <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
          Batch Delete
        </el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="resourceName" label="File Name" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sceneCode" label="Scene" width="140" />
      <el-table-column prop="fileType" label="Type" width="160" show-overflow-tooltip />
      <el-table-column prop="fileSize" label="Size" width="120">
        <template #default="{ row }">
          {{ formatSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="storageType" label="Storage" width="120">
        <template #default="{ row }">
          <el-tag :type="getStorageTypeColor(row.storageType)" size="small">
            {{ getStorageTypeLabel(row.storageType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createUserName" label="Upload By" width="120" />
      <el-table-column prop="createTime" label="Upload Time" width="180" />
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

    <el-dialog v-model="uploadDialogVisible" title="Upload Resource" width="500px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="Scene Code" required>
          <el-input v-model="uploadForm.sceneCode" placeholder="e.g., avatar, document" />
        </el-form-item>
        <el-form-item label="Storage Type">
          <el-select v-model="uploadForm.storageType" style="width: 100%">
            <el-option label="Local" value="local" />
            <el-option label="MinIO" value="minio" />
            <el-option label="OSS" value="oss" />
          </el-select>
        </el-form-item>
        <el-form-item label="File">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :file-list="fileList"
            :http-request="customUpload"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div>Drop file here or <em>click to upload</em></div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="handleUpload" :loading="uploading">Upload</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, UploadFilled } from '@element-plus/icons-vue'
import { resourceApi } from '@/api/resource'
import type { Resource } from '@/api/resource'

const loading = ref(false)
const tableData = ref<Resource[]>([])
const total = ref(0)
const selectedRows = ref<Resource[]>([])

const queryParams = reactive({
  sceneCode: '',
  storageType: '',
  keyword: '',
  pageNum: 1,
  pageSize: 10
})

const uploadDialogVisible = ref(false)
const uploadForm = reactive({
  sceneCode: '',
  storageType: 'local'
})
const uploading = ref(false)
const fileList = ref<any[]>([])
const uploadRef = ref()

const loadData = async () => {
  loading.value = true
  try {
    const result = await resourceApi.page(queryParams)
    tableData.value = result.list || []
    total.value = result.total || 0
  } catch {
    ElMessage.error('Failed to load resources')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.sceneCode = ''
  queryParams.storageType = ''
  queryParams.keyword = ''
  queryParams.pageNum = 1
  loadData()
}

const handleSelectionChange = (rows: Resource[]) => {
  selectedRows.value = rows
}

const handleDelete = async (row: Resource) => {
  try {
    await ElMessageBox.confirm(`Delete resource "${row.resourceName}"?`, 'Confirm', { type: 'warning' })
    await resourceApi.delete(row.id!)
    ElMessage.success('Deleted successfully')
    loadData()
  } catch {
    // cancelled
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('Please select resources to delete')
    return
  }
  try {
    await ElMessageBox.confirm(
      `Delete ${selectedRows.value.length} resources?`,
      'Confirm',
      { type: 'warning' }
    )
    const ids = selectedRows.value.map(r => r.id!)
    await Promise.all(ids.map(id => resourceApi.delete(id)))
    ElMessage.success(`Deleted ${ids.length} resources`)
    selectedRows.value = []
    loadData()
  } catch {
    // cancelled
  }
}

const showUploadDialog = () => {
  uploadForm.sceneCode = ''
  uploadForm.storageType = 'local'
  fileList.value = []
  uploadDialogVisible.value = true
}

const handleFileChange = (file: any) => {
  fileList.value = [file]
}

const customUpload = async (options: any) => {
  // Used by el-upload as the http-request handler; actual submit handled by handleUpload
  options.onSuccess?.({}, options.file)
}

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('Please select a file')
    return
  }
  if (!uploadForm.sceneCode.trim()) {
    ElMessage.warning('Please enter a scene code')
    return
  }

  const formData = new FormData()
  formData.append('sceneCode', uploadForm.sceneCode)
  formData.append('storageType', uploadForm.storageType)
  formData.append('file', fileList.value[0].raw)

  uploading.value = true
  try {
    await resourceApi.upload(formData)
    ElMessage.success('Uploaded successfully')
    uploadDialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('Upload failed')
  } finally {
    uploading.value = false
  }
}

const handleDownload = async (row: Resource) => {
  try {
    const blob = await resourceApi.download(row.id!)
    const url = window.URL.createObjectURL(blob as unknown as Blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.resourceName
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

const getStorageTypeColor = (type: string) => {
  const map: Record<string, string> = {
    local: '',
    minio: 'primary',
    oss: 'success'
  }
  return map[type] || 'info'
}

const getStorageTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    local: 'Local',
    minio: 'MinIO',
    oss: 'OSS'
  }
  return map[type] || type
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.resource-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.actions {
  text-align: right;
}
</style>