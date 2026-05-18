<template>
  <div class="user-list">
    <!-- Basic Search Toolbar -->
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Username">
          <el-input v-model="queryParams.username" placeholder="Please enter username" clearable />
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="queryParams.status" placeholder="Please select" clearable style="width: 150px">
            <el-option label="Active" value="1" />
            <el-option label="Disabled" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
          <el-button type="info" @click="toggleAdvanced">Advanced</el-button>
        </el-form-item>
        <el-form-item v-if="selectedRows.length > 0">
          <el-dropdown @command="handleBatchCommand">
            <el-button type="warning">
              Batch Actions ({{ selectedRows.length }})<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="enable">Enable Selected</el-dropdown-item>
                <el-dropdown-item command="disable">Disable Selected</el-dropdown-item>
                <el-dropdown-item command="delete" divided>Delete Selected</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">Add User</el-button>
      </div>
    </div>

    <!-- Advanced Search Panel -->
    <div v-if="showAdvanced" class="advanced-search">
      <el-form :model="queryParams" inline>
        <el-form-item label="Email">
          <el-input v-model="queryParams.email" placeholder="Please enter email" clearable />
        </el-form-item>
        <el-form-item label="Mobile">
          <el-input v-model="queryParams.mobile" placeholder="Please enter mobile" clearable />
        </el-form-item>
        <el-form-item label="Created Date">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="to"
            start-placeholder="Start date"
            end-placeholder="End date"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="Username">
        <template #default="{ row }">
          <el-link type="primary" @click="handleView(row)">{{ row.username }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="Nickname" />
      <el-table-column prop="email" label="Email" />
      <el-table-column prop="mobile" label="Mobile" />
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
      <el-form :model="formData" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="Username" prop="username">
          <el-input v-model="formData.username" placeholder="4-20 characters, alphanumeric and underscore" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="Password" prop="password" v-if="!formData.id">
          <el-input v-model="formData.password" type="password" show-password placeholder="At least 6 characters" />
        </el-form-item>
        <el-form-item label="Nickname" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="Please enter nickname" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="formData.email" placeholder="Please enter email" />
        </el-form-item>
        <el-form-item label="Mobile" prop="mobile">
          <el-input v-model="formData.mobile" placeholder="Please enter mobile number" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import type { User } from '@/api/user'
import { validateEmail, validateMobile, validateUsername, validatePassword } from '@/utils/validate'

const router = useRouter()
const formRef = ref()

const loading = ref(false)
const tableData = ref<User[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const selectedRows = ref<User[]>([])
const showAdvanced = ref(false)

const queryParams = reactive({
  username: '',
  status: '',
  email: '',
  mobile: '',
  dateRange: [],
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<User>({
  username: '',
  nickname: '',
  email: '',
  mobile: '',
  status: 1
})

const formRules = {
  username: [
    { required: true, message: 'Username is required', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  mobile: [
    { validator: validateMobile, trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const result = await userApi.page(queryParams)
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
  queryParams.username = ''
  queryParams.status = ''
  queryParams.email = ''
  queryParams.mobile = ''
  queryParams.dateRange = []
  queryParams.pageNum = 1
  loadData()
}

const toggleAdvanced = () => {
  showAdvanced.value = !showAdvanced.value
}

const handleAdd = () => {
  Object.keys(formData).forEach(key => {
    (formData as any)[key] = key === 'status' ? 1 : undefined
  })
  dialogTitle.value = 'Add User'
  dialogVisible.value = true
}

const handleEdit = (row: User) => {
  Object.assign(formData, row)
  dialogTitle.value = 'Edit User'
  dialogVisible.value = true
}

const handleView = (row: User) => {
  router.push(`/users/${row.id}`)
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await userApi.update(formData.id!, formData)
      ElMessage.success('Updated successfully')
    } else {
      await userApi.create(formData)
      ElMessage.success('Created successfully')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('Operation failed')
  }
}

const handleDelete = async (row: User) => {
  try {
    await ElMessageBox.confirm(`Delete user ${row.username}?`, 'Confirm', { type: 'warning' })
    await userApi.delete(row.id!)
    ElMessage.success('Deleted successfully')
    loadData()
  } catch {
    // cancelled
  }
}

const handleSelectionChange = (rows: User[]) => {
  selectedRows.value = rows
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('Please select users to delete')
    return
  }
  try {
    await ElMessageBox.confirm(`Delete ${selectedRows.value.length} users?`, 'Confirm', { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id!)
    await Promise.all(ids.map(id => userApi.delete(id)))
    ElMessage.success(`Deleted ${ids.length} users successfully`)
    selectedRows.value = []
    loadData()
  } catch {
    // cancelled
  }
}

const handleBatchCommand = async (command: string) => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('Please select users first')
    return
  }

  try {
    const ids = selectedRows.value.map(row => row.id!)
    if (command === 'enable') {
      await Promise.all(ids.map(id => userApi.update(id, { status: 1 } as User)))
      ElMessage.success(`Enabled ${ids.length} users successfully`)
    } else if (command === 'disable') {
      await Promise.all(ids.map(id => userApi.update(id, { status: 0 } as User)))
      ElMessage.success(`Disabled ${ids.length} users successfully`)
    } else if (command === 'delete') {
      await ElMessageBox.confirm(`Delete ${ids.length} users?`, 'Confirm', { type: 'warning' })
      await Promise.all(ids.map(id => userApi.delete(id)))
      ElMessage.success(`Deleted ${ids.length} users successfully`)
    }
    selectedRows.value = []
    loadData()
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-list {
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

.advanced-search {
  padding: 15px;
  margin-bottom: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>