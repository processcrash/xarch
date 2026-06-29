<!-- TODO: i18n - migrate all hardcoded labels/buttons to t('config.*') keys -->
<template>
  <div class="config-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Config Key">
          <el-input v-model="queryParams.configKey" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">Add Config</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="configKey" label="Config Key" />
      <el-table-column prop="configValue" label="Value" />
      <el-table-column prop="configType" label="Type" width="100" />
      <el-table-column prop="description" label="Description" />
      <el-table-column prop="status" label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? 'Active' : 'Disabled' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">Edit</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="Config Key">
          <el-input v-model="formData.configKey" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="Config Value">
          <el-input v-model="formData.configValue" />
        </el-form-item>
        <el-form-item label="Type">
          <el-select v-model="formData.configType" style="width: 100%">
            <el-option label="String" value="string" />
            <el-option label="Number" value="number" />
            <el-option label="Boolean" value="boolean" />
          </el-select>
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="formData.description" type="textarea" />
        </el-form-item>
        <el-form-item label="Status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">Active</el-radio>
            <el-radio :label="0">Disabled</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="handleSubmit">Submit</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { configApi, type Config } from '@/api/config'

const loading = ref(false)
const tableData = ref<Config[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryParams = reactive({
  configKey: '',
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<Config>({
  configKey: '',
  configValue: '',
  configType: 'string',
  description: '',
  status: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const result = await configApi.page(queryParams)
    tableData.value = result.list || []
    total.value = result.total || 0
  } catch {
    ElMessage.error('Failed to load data')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.configKey = ''
  queryParams.pageNum = 1
  loadData()
}

const handleAdd = () => {
  Object.keys(formData).forEach(key => {
    (formData as any)[key] = key === 'configType' || key === 'status' ? (key === 'configType' ? 'string' : 1) : undefined
  })
  dialogTitle.value = 'Add Config'
  dialogVisible.value = true
}

const handleEdit = (row: Config) => {
  Object.assign(formData, row)
  dialogTitle.value = 'Edit Config'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await configApi.update(formData.id!, formData)
      ElMessage.success('Updated successfully')
    } else {
      await configApi.create(formData)
      ElMessage.success('Created successfully')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('Operation failed')
  }
}

const handleDelete = async (row: Config) => {
  try {
    await ElMessageBox.confirm(`Delete config ${row.configKey}?`, 'Confirm', { type: 'warning' })
    await configApi.delete(row.id!)
    ElMessage.success('Deleted successfully')
    loadData()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.config-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>