<template>
  <div class="resource-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item :label="t('resource.sceneCode')">
          <el-input v-model="queryParams.sceneCode" :placeholder="t('resource.sceneCode')" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item :label="t('resource.storageType')">
          <el-select v-model="queryParams.storageType" :placeholder="t('common.all')" clearable style="width: 150px">
            <el-option :label="t('common.all')" value="" />
            <el-option :label="t('resource.storageLocal')" value="local" />
            <el-option :label="t('resource.storageMinio')" value="minio" />
            <el-option :label="t('resource.storageOss')" value="oss" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('resource.keyword')">
          <el-input v-model="queryParams.keyword" :placeholder="t('resource.fileName')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="showUploadDialog">
          <el-icon><Upload /></el-icon>
          {{ t('resource.uploadFile') }}
        </el-button>
        <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
          {{ t('common.batchDelete') }}
        </el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="resourceName" :label="t('resource.fileName')" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sceneCode" :label="t('resource.sceneCode')" width="140" />
      <el-table-column prop="fileType" :label="t('resource.type')" width="160" show-overflow-tooltip />
      <el-table-column prop="fileSize" :label="t('resource.size')" width="120">
        <template #default="{ row }">
          {{ formatSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="storageType" :label="t('resource.storage')" width="120">
        <template #default="{ row }">
          <el-tag :type="getStorageTypeColor(row.storageType)" size="small">
            {{ getStorageTypeLabel(row.storageType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createUserName" :label="t('resource.uploadBy')" width="120" />
      <el-table-column prop="createTime" :label="t('resource.uploadTime')" width="180" />
      <el-table-column :label="t('common.actions')" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleDownload(row)">{{ t('common.download') }}</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
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

    <el-dialog v-model="uploadDialogVisible" :title="t('resource.uploadResource')" width="500px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item :label="t('resource.sceneCode')" required>
          <el-input v-model="uploadForm.sceneCode" :placeholder="t('resource.sceneCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('resource.storageType')">
          <el-select v-model="uploadForm.storageType" style="width: 100%">
            <el-option :label="t('resource.storageLocal')" value="local" />
            <el-option :label="t('resource.storageMinio')" value="minio" />
            <el-option :label="t('resource.storageOss')" value="oss" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('resource.fileName')">
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
            <div>{{ t('resource.dropOrClick') }} <em>{{ t('resource.clickToUpload') }}</em></div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleUpload" :loading="uploading">{{ t('common.upload') }}</el-button>
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
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()
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
    ElMessage.error(t('resource.loadFailed'))
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
    await ElMessageBox.confirm(t('resource.confirmDelete', { name: row.resourceName }), t('common.confirm'), { type: 'warning' })
    await resourceApi.delete(row.id!)
    ElMessage.success(t('common.messages.deletedSuccess'))
    loadData()
  } catch {
    // cancelled
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning(t('common.messages.pleaseSelectToDelete'))
    return
  }
  try {
    await ElMessageBox.confirm(
      t('resource.confirmDelete', { name: `${selectedRows.value.length}` }),
      t('common.confirm'),
      { type: 'warning' }
    )
    const ids = selectedRows.value.map(r => r.id!)
    await Promise.all(ids.map(id => resourceApi.delete(id)))
    ElMessage.success(t('common.messages.deletedSuccess'))
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
    ElMessage.warning(t('resource.selectFile'))
    return
  }
  if (!uploadForm.sceneCode.trim()) {
    ElMessage.warning(t('resource.enterSceneCode'))
    return
  }

  const formData = new FormData()
  formData.append('sceneCode', uploadForm.sceneCode)
  formData.append('storageType', uploadForm.storageType)
  formData.append('file', fileList.value[0].raw)

  uploading.value = true
  try {
    await resourceApi.upload(formData)
    ElMessage.success(t('resource.uploadedSuccess'))
    uploadDialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error(t('resource.uploadFailed'))
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
    ElMessage.error(t('resource.downloadFailed'))
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
    local: t('resource.storageLocal'),
    minio: t('resource.storageMinio'),
    oss: t('resource.storageOss')
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
