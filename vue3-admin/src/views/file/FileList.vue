<template>
  <div class="file-management">
    <!-- Storage Stats -->
    <el-card class="stats-card" shadow="hover">
      <div class="stats-container">
        <div class="stat-item">
          <div class="stat-label">Local Storage</div>
          <div class="stat-value">{{ stats.localCount || 0 }} files</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">MinIO</div>
          <div class="stat-value">{{ stats.minioCount || 0 }} files</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">Aliyun OSS</div>
          <div class="stat-value">{{ stats.ossCount || 0 }} files</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">Total Size</div>
          <div class="stat-value">{{ formatSize(stats.totalSize || 0) }}</div>
        </div>
      </div>
    </el-card>

    <!-- Toolbar -->
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Scene">
          <el-input v-model="queryParams.sceneCode" placeholder="Scene code" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="Storage">
          <el-select v-model="queryParams.storageType" placeholder="All" clearable style="width: 150px">
            <el-option label="Local" value="local" />
            <el-option label="MinIO" value="minio" />
            <el-option label="Aliyun OSS" value="aliyun_oss" />
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
        <el-button type="primary" @click="showUploadDialog">Upload</el-button>
        <el-button @click="showStorageConfig">Storage Config</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedRows.length === 0">Batch Delete</el-button>
      </div>
    </div>

    <!-- File List -->
    <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="resourceName" label="File Name" min-width="200">
        <template #default="{ row }">
          <div class="file-name-cell">
            <el-icon v-if="isImage(row.fileType)" class="file-icon"><Picture /></el-icon>
            <el-icon v-else-if="isVideo(row.fileType)" class="file-icon"><VideoPlay /></el-icon>
            <el-icon v-else class="file-icon"><Document /></el-icon>
            <span>{{ row.resourceName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="sceneCode" label="Scene" width="120" />
      <el-table-column prop="storageType" label="Storage" width="100">
        <template #default="{ row }">
          <el-tag :type="getStorageTypeColor(row.storageType)" size="small">
            {{ getStorageTypeLabel(row.storageType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" label="Size" width="100">
        <template #default="{ row }">
          {{ formatSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="createUserName" label="Upload By" width="120" />
      <el-table-column prop="createTime" label="Upload Time" width="180" />
      <el-table-column label="Actions" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handlePreview(row)">Preview</el-button>
          <el-button size="small" @click="handleDownload(row)">Download</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
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

    <!-- Upload Dialog -->
    <el-dialog v-model="uploadDialogVisible" title="Upload File" width="500px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="Scene Code">
          <el-input v-model="uploadForm.sceneCode" placeholder="e.g., avatar, document" />
        </el-form-item>
        <el-form-item label="Storage Type">
          <el-select v-model="uploadForm.storageType" style="width: 100%">
            <el-option v-for="type in storageTypes" :key="type.code" :label="type.name" :value="type.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="File">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :file-list="fileList"
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

    <!-- Storage Config Dialog -->
    <el-dialog v-model="configDialogVisible" title="Storage Configuration" width="700px">
      <el-tabs v-model="configTab">
        <el-tab-pane label="Config List" name="list">
          <el-button type="primary" @click="showConfigForm(null)" style="margin-bottom: 15px">Add Config</el-button>
          <el-table :data="configList" border>
            <el-table-column prop="configName" label="Name" width="150" />
            <el-table-column prop="storageType" label="Type" width="120">
              <template #default="{ row }">
                <el-tag>{{ getStorageTypeLabel(row.storageType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="endpoint" label="Endpoint" min-width="150" show-overflow-tooltip />
            <el-table-column prop="bucketName" label="Bucket" width="120" />
            <el-table-column prop="isDefault" label="Default" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault === 1" type="success">Yes</el-tag>
                <el-tag v-else type="info">No</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="Status" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? 'Enabled' : 'Disabled' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="160">
              <template #default="{ row }">
                <el-button size="small" @click="showConfigForm(row)">Edit</el-button>
                <el-button size="small" type="danger" @click="handleConfigDelete(row.id)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="Add/Edit" name="form">
          <el-form :model="configForm" label-width="120px">
            <el-form-item label="Config Name">
              <el-input v-model="configForm.configName" />
            </el-form-item>
            <el-form-item label="Storage Type">
              <el-select v-model="configForm.storageType" style="width: 100%">
                <el-option v-for="type in storageTypes" :key="type.code" :label="type.name" :value="type.code" />
              </el-select>
            </el-form-item>
            <el-form-item label="Endpoint">
              <el-input v-model="configForm.endpoint" placeholder="http://localhost:9000 or oss-cn-hangzhou.aliyuncs.com" />
            </el-form-item>
            <el-form-item label="Access Key">
              <el-input v-model="configForm.accessKey" placeholder="Access key or Access ID" />
            </el-form-item>
            <el-form-item label="Secret Key">
              <el-input v-model="configForm.secretKey" type="password" show-password />
            </el-form-item>
            <el-form-item label="Bucket Name">
              <el-input v-model="configForm.bucketName" />
            </el-form-item>
            <el-form-item label="Region">
              <el-input v-model="configForm.region" placeholder="e.g., cn-hangzhou" />
            </el-form-item>
            <el-form-item label="Domain">
              <el-input v-model="configForm.domain" placeholder="Custom domain for access" />
            </el-form-item>
            <el-form-item label="Base Path">
              <el-input v-model="configForm.basePath" placeholder="e.g., files/" />
            </el-form-item>
            <el-form-item label="Default">
              <el-switch v-model="configForm.isDefault" :true-value="1" :false-value="0" />
            </el-form-item>
            <el-form-item label="Status">
              <el-switch v-model="configForm.status" :true-value="1" :false-value="0" />
            </el-form-item>
            <el-form-item label="Description">
              <el-input v-model="configForm.description" type="textarea" />
            </el-form-item>
          </el-form>
          <div style="text-align: right; margin-top: 20px">
            <el-button @click="configTab = 'list'">Cancel</el-button>
            <el-button type="primary" @click="handleConfigSave">Save</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- Preview Dialog -->
    <el-dialog v-model="previewDialogVisible" title="Preview" width="800px">
      <div class="preview-container">
        <img v-if="previewType === 'image'" :src="previewUrl" class="preview-image" />
        <video v-else-if="previewType === 'video'" :src="previewUrl" controls class="preview-video" />
        <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" class="preview-pdf" />
        <div v-else class="preview-unsupported">Preview not available for this file type</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, VideoPlay, Document, UploadFilled } from '@element-plus/icons-vue'
import {
  filePage,
  fileDelete,
  fileStats,
  fileUpload,
  storageConfigList,
  storageConfigCreate,
  storageConfigUpdate,
  storageConfigDelete,
  storageTypes
} from '@/api/file'
import type { Resource, StorageConfig, StorageStats } from '@/api/file'

const loading = ref(false)
const tableData = ref<Resource[]>([])
const total = ref(0)
const selectedRows = ref<Resource[]>([])
const stats = ref<StorageStats>({})

// Query params
const queryParams = reactive({
  sceneCode: '',
  storageType: '',
  keyword: '',
  pageNum: 1,
  pageSize: 10
})

// Upload
const uploadDialogVisible = ref(false)
const uploadForm = reactive({
  sceneCode: '',
  storageType: 'local'
})
const uploading = ref(false)
const fileList = ref<any[]>([])
const uploadRef = ref()

// Storage config
const configDialogVisible = ref(false)
const configTab = ref('list')
const configList = ref<StorageConfig[]>([])
const storageTypesList = ref<Array<{ code: string; name: string; description: string }>>([])
const configForm = reactive<StorageConfig>({
  id: undefined,
  storageType: 'local',
  configName: '',
  isDefault: 0,
  endpoint: '',
  accessKey: '',
  secretKey: '',
  bucketName: '',
  region: '',
  basePath: '',
  domain: '',
  status: 1,
  description: ''
})

// Preview
const previewDialogVisible = ref(false)
const previewUrl = ref('')
const previewType = ref('')

const loadData = async () => {
  loading.value = true
  try {
    const result = await filePage(queryParams)
    if (result.code === '0000') {
      tableData.value = result.data.list || []
      total.value = result.data.total || 0
    }
  } catch {
    ElMessage.error('Failed to load files')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const result = await fileStats()
    if (result.code === '0000') {
      stats.value = result.data
    }
  } catch {
    // ignore
  }
}

const loadStorageTypes = async () => {
  try {
    const result = await storageTypes()
    if (result.code === '0000') {
      storageTypesList.value = result.data
    }
  } catch {
    // ignore
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
    await ElMessageBox.confirm(`Delete file ${row.resourceName}?`, 'Confirm', { type: 'warning' })
    await fileDelete(row.id!)
    ElMessage.success('Deleted successfully')
    loadData()
    loadStats()
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
    await ElMessageBox.confirm(`Delete ${selectedRows.value.length} files?`, 'Confirm', { type: 'warning' })
    await Promise.all(selectedRows.value.map(row => fileDelete(row.id!)))
    ElMessage.success(`Deleted ${selectedRows.value.length} files`)
    loadData()
    loadStats()
  } catch {
    // cancelled
  }
}

// Upload handlers
const showUploadDialog = () => {
  uploadForm.sceneCode = ''
  uploadForm.storageType = 'local'
  fileList.value = []
  uploadDialogVisible.value = true
}

const handleFileChange = (file: any) => {
  fileList.value = [file]
}

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('Please select a file')
    return
  }

  const formData = new FormData()
  formData.append('sceneCode', uploadForm.sceneCode)
  formData.append('storageType', uploadForm.storageType)
  formData.append('file', fileList.value[0].raw)

  uploading.value = true
  try {
    const result = await fileUpload(formData)
    if (result.code === '0000') {
      ElMessage.success('Uploaded successfully')
      uploadDialogVisible.value = false
      loadData()
      loadStats()
    } else {
      ElMessage.error(result.msg)
    }
  } catch {
    ElMessage.error('Upload failed')
  } finally {
    uploading.value = false
  }
}

// Storage config handlers
const showStorageConfig = async () => {
  configTab.value = 'list'
  await loadStorageConfigs()
  configDialogVisible.value = true
}

const loadStorageConfigs = async () => {
  try {
    const result = await storageConfigList()
    if (result.code === '0000') {
      configList.value = result.data
    }
  } catch {
    // ignore
  }
}

const showConfigForm = (config: StorageConfig | null) => {
  if (config) {
    Object.assign(configForm, config)
  } else {
    Object.assign(configForm, {
      id: undefined,
      storageType: 'local',
      configName: '',
      isDefault: 0,
      endpoint: '',
      accessKey: '',
      secretKey: '',
      bucketName: '',
      region: '',
      basePath: '',
      domain: '',
      status: 1,
      description: ''
    })
  }
  configTab.value = 'form'
}

const handleConfigSave = async () => {
  try {
    if (configForm.id) {
      await storageConfigUpdate(configForm)
    } else {
      await storageConfigCreate(configForm)
    }
    ElMessage.success('Saved successfully')
    configTab.value = 'list'
    await loadStorageConfigs()
  } catch {
    ElMessage.error('Save failed')
  }
}

const handleConfigDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('Delete this configuration?', 'Confirm', { type: 'warning' })
    await storageConfigDelete(id)
    ElMessage.success('Deleted successfully')
    await loadStorageConfigs()
  } catch {
    // cancelled
  }
}

// Preview/Download
const handlePreview = (row: Resource) => {
  previewUrl.value = row.accessUrl
  if (row.fileType?.startsWith('image/')) {
    previewType.value = 'image'
  } else if (row.fileType?.startsWith('video/')) {
    previewType.value = 'video'
  } else if (row.fileType === 'application/pdf') {
    previewType.value = 'pdf'
  } else {
    previewType.value = 'unsupported'
  }
  previewDialogVisible.value = true
}

const handleDownload = (row: Resource) => {
  window.open(`/file/download/${row.id}`, '_blank')
}

// Helpers
const formatSize = (bytes: number) => {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  while (bytes >= 1024 && i < units.length - 1) {
    bytes /= 1024
    i++
  }
  return `${bytes.toFixed(1)} ${units[i]}`
}

const isImage = (fileType: string) => fileType?.startsWith('image/')
const isVideo = (fileType: string) => fileType?.startsWith('video/')

const getStorageTypeColor = (type: string) => {
  const map: Record<string, string> = {
    local: '',
    minio: 'primary',
    aliyun_oss: 'success'
  }
  return map[type] || ''
}

const getStorageTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    local: 'Local',
    minio: 'MinIO',
    aliyun_oss: 'Aliyun OSS'
  }
  return map[type] || type
}

onMounted(() => {
  loadData()
  loadStats()
  loadStorageTypes()
})
</script>

<style scoped>
.file-management {
  padding: 20px;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-container {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  font-size: 20px;
  color: #409eff;
}

.preview-container {
  width: 100%;
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
}

.preview-video {
  max-width: 100%;
  max-height: 100%;
}

.preview-pdf {
  width: 100%;
  height: 100%;
}

.preview-unsupported {
  color: #909399;
}
</style>