<template>
  <div class="client-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Client ID">
          <el-input v-model="queryParams.clientId" placeholder="Please enter client ID" clearable />
        </el-form-item>
        <el-form-item label="Client Name">
          <el-input v-model="queryParams.clientName" placeholder="Please enter client name" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">Batch Delete</el-button>
        <el-button type="primary" @click="handleAdd">Add Client</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="clientId" label="Client ID" width="180" />
      <el-table-column prop="clientKey" label="Client Key" width="180" />
      <el-table-column prop="clientName" label="Client Name" min-width="160" />
      <el-table-column prop="grantTypes" label="Grant Types" min-width="180" show-overflow-tooltip />
      <el-table-column prop="scope" label="Scope" width="120" />
      <el-table-column prop="status" label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? 'Active' : 'Disabled' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Create Time" width="180" />
      <el-table-column label="Actions" width="160" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="formData" label-width="120px" :rules="formRules" ref="formRef">
        <el-form-item label="Client ID" prop="clientId">
          <el-input v-model="formData.clientId" placeholder="Unique client identifier" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="Client Key" prop="clientKey">
          <el-input v-model="formData.clientKey" placeholder="Client key" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="Client Secret" prop="clientSecret" v-if="!formData.id">
          <el-input v-model="formData.clientSecret" type="password" show-password placeholder="Client secret" />
        </el-form-item>
        <el-form-item label="Client Name" prop="clientName">
          <el-input v-model="formData.clientName" placeholder="Please enter client name" />
        </el-form-item>
        <el-form-item label="Grant Types" prop="grantTypes">
          <el-input v-model="formData.grantTypes" placeholder="e.g., password,authorization_code,client_credentials" />
        </el-form-item>
        <el-form-item label="Scope" prop="scope">
          <el-input v-model="formData.scope" placeholder="e.g., read,write" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
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
import { clientApi } from '@/api/client'
import type { Client } from '@/api/client'

const formRef = ref()

const loading = ref(false)
const tableData = ref<Client[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const selectedRows = ref<Client[]>([])

const queryParams = reactive({
  clientId: '',
  clientName: '',
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<Client>({
  clientId: '',
  clientKey: '',
  clientSecret: '',
  clientName: '',
  grantTypes: '',
  scope: '',
  status: 1
})

const formRules = {
  clientId: [
    { required: true, message: 'Client ID is required', trigger: 'blur' },
    { min: 2, max: 50, message: 'Length 2-50 characters', trigger: 'blur' }
  ],
  clientKey: [
    { required: true, message: 'Client key is required', trigger: 'blur' },
    { min: 2, max: 50, message: 'Length 2-50 characters', trigger: 'blur' }
  ],
  clientName: [
    { required: true, message: 'Client name is required', trigger: 'blur' },
    { min: 2, max: 50, message: 'Length 2-50 characters', trigger: 'blur' }
  ],
  clientSecret: [
    { required: true, message: 'Client secret is required', trigger: 'blur' },
    { min: 6, max: 100, message: 'Length 6-100 characters', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const result = await clientApi.page(queryParams)
    tableData.value = result.list || []
    total.value = result.total || 0
  } catch {
    ElMessage.error('Failed to load clients')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.clientId = ''
  queryParams.clientName = ''
  queryParams.pageNum = 1
  loadData()
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    clientId: '',
    clientKey: '',
    clientSecret: '',
    clientName: '',
    grantTypes: '',
    scope: '',
    status: 1
  })
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = 'Add Client'
  dialogVisible.value = true
}

const handleEdit = (row: Client) => {
  Object.assign(formData, row)
  formData.clientSecret = ''
  dialogTitle.value = 'Edit Client'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (formData.id) {
      await clientApi.update(formData.id, formData)
      ElMessage.success('Updated successfully')
    } else {
      await clientApi.create(formData)
      ElMessage.success('Created successfully')
    }
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    if (e?.message) {
      ElMessage.error('Operation failed: ' + e.message)
    }
  }
}

const handleDelete = async (row: Client) => {
  try {
    await ElMessageBox.confirm(`Delete client ${row.clientName}?`, 'Confirm', { type: 'warning' })
    await clientApi.delete({ ids: [row.id!] })
    ElMessage.success('Deleted successfully')
    loadData()
  } catch {
    // cancelled
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('Please select clients to delete')
    return
  }
  try {
    await ElMessageBox.confirm(`Delete ${selectedRows.value.length} clients?`, 'Confirm', { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id!)
    await clientApi.delete({ ids })
    ElMessage.success(`Deleted ${ids.length} clients successfully`)
    selectedRows.value = []
    loadData()
  } catch {
    // cancelled
  }
}

const handleSelectionChange = (rows: Client[]) => {
  selectedRows.value = rows
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.client-list {
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
