<template>
  <div class="user-list">
    <!-- Basic Search Toolbar -->
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item :label="t('user.username')">
          <el-input v-model="queryParams.username" :placeholder="t('user.placeholders.username')" clearable />
        </el-form-item>
        <el-form-item :label="t('common.status.active')">
          <el-select v-model="queryParams.status" :placeholder="t('common.pleaseSelect')" clearable style="width: 150px">
            <el-option :label="t('common.status.active')" value="1" />
            <el-option :label="t('common.status.disabled')" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
          <el-button type="info" @click="toggleAdvanced">{{ t('common.advanced') }}</el-button>
        </el-form-item>
        <el-form-item v-if="selectedRows.length > 0">
          <el-dropdown @command="handleBatchCommand">
            <el-button type="warning">
              {{ t('common.batchActions') }} ({{ selectedRows.length }})<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="enable">{{ t('common.enableSelected') }}</el-dropdown-item>
                <el-dropdown-item command="disable">{{ t('common.disableSelected') }}</el-dropdown-item>
                <el-dropdown-item command="delete" divided>{{ t('common.deleteSelected') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">{{ t('user.addUser') }}</el-button>
      </div>
    </div>

    <!-- Advanced Search Panel -->
    <div v-if="showAdvanced" class="advanced-search">
      <el-form :model="queryParams" inline>
        <el-form-item :label="t('user.email')">
          <el-input v-model="queryParams.email" :placeholder="t('user.placeholders.email')" clearable />
        </el-form-item>
        <el-form-item :label="t('user.mobile')">
          <el-input v-model="queryParams.mobile" :placeholder="t('user.placeholders.mobile')" clearable />
        </el-form-item>
        <el-form-item :label="t('user.createTime')">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            :range-separator="t('common.to')"
            :start-placeholder="t('common.pleaseSelect')"
            :end-placeholder="t('common.pleaseSelect')"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" :label="t('user.username')">
        <template #default="{ row }">
          <el-link type="primary" @click="handleView(row)">{{ row.username }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" :label="t('user.nickname')" />
      <el-table-column prop="email" :label="t('user.email')" />
      <el-table-column prop="mobile" :label="t('user.mobile')" />
      <el-table-column :label="t('user.status')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? t('common.status.active') : t('common.status.disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('user.actions')" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">{{ t('common.edit') }}</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item :label="t('user.username')" prop="username">
          <el-input
            v-model="formData.username"
            :placeholder="t('user.hints.usernameRule')"
            :disabled="!!formData.id"
          />
        </el-form-item>
        <el-form-item :label="t('user.password')" prop="password" v-if="!formData.id">
          <el-input
            v-model="formData.password"
            type="password"
            show-password
            :placeholder="t('user.hints.passwordRule')"
          />
        </el-form-item>
        <el-form-item :label="t('user.nickname')" prop="nickname">
          <el-input v-model="formData.nickname" :placeholder="t('user.placeholders.nickname')" />
        </el-form-item>
        <el-form-item :label="t('user.email')" prop="email">
          <el-input v-model="formData.email" :placeholder="t('user.placeholders.email')" />
        </el-form-item>
        <el-form-item :label="t('user.mobile')" prop="mobile">
          <el-input v-model="formData.mobile" :placeholder="t('user.placeholders.mobile')" />
        </el-form-item>
        <el-form-item :label="t('user.status')" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">{{ t('common.status.active') }}</el-radio>
            <el-radio :label="0">{{ t('common.status.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ t('common.submit') }}</el-button>
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
import { useI18n } from '@/composables/useI18n'

const router = useRouter()
const formRef = ref()
const { t } = useI18n()

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
    { required: true, message: t('user.validation.usernameRequired'), trigger: 'blur' },
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
    ElMessage.error(t('common.messages.loadFailed'))
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
  dialogTitle.value = t('user.addUser')
  dialogVisible.value = true
}

const handleEdit = (row: User) => {
  Object.assign(formData, row)
  dialogTitle.value = t('user.editUser')
  dialogVisible.value = true
}

const handleView = (row: User) => {
  router.push(`/users/${row.id}`)
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await userApi.update(formData.id!, formData)
      ElMessage.success(t('common.messages.updatedSuccess'))
    } else {
      await userApi.create(formData)
      ElMessage.success(t('common.messages.createdSuccess'))
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error(t('common.operationFailed'))
  }
}

const handleDelete = async (row: User) => {
  try {
    await ElMessageBox.confirm(t('user.confirmDelete', { name: row.username }), t('common.confirm'), { type: 'warning' })
    await userApi.delete(row.id!)
    ElMessage.success(t('common.messages.deletedSuccess'))
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
    ElMessage.warning(t('common.messages.pleaseSelectToDelete'))
    return
  }
  try {
    await ElMessageBox.confirm(t('user.confirmDelete', { name: `${selectedRows.value.length}` }), t('common.confirm'), { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id!)
    await Promise.all(ids.map(id => userApi.delete(id)))
    ElMessage.success(t('common.messages.deletedSuccess'))
    selectedRows.value = []
    loadData()
  } catch {
    // cancelled
  }
}

const handleBatchCommand = async (command: string) => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning(t('common.messages.pleaseSelectItems'))
    return
  }

  try {
    const ids = selectedRows.value.map(row => row.id!)
    if (command === 'enable') {
      await Promise.all(ids.map(id => userApi.update(id, { status: 1 } as User)))
      ElMessage.success(t('common.status.active'))
    } else if (command === 'disable') {
      await Promise.all(ids.map(id => userApi.update(id, { status: 0 } as User)))
      ElMessage.success(t('common.status.disabled'))
    } else if (command === 'delete') {
      await ElMessageBox.confirm(t('user.confirmDelete', { name: `${selectedRows.value.length}` }), t('common.confirm'), { type: 'warning' })
      await Promise.all(ids.map(id => userApi.delete(id)))
      ElMessage.success(t('common.messages.deletedSuccess'))
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
