<template>
  <div class="role-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item :label="t('role.roleName')">
          <el-input v-model="queryParams.roleName" :placeholder="t('role.roleName')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
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
        <el-button type="primary" @click="handleAdd">{{ t('role.addRole') }}</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleName" :label="t('role.roleName')">
        <template #default="{ row }">
          <el-link type="primary" @click="handleView(row)">{{ row.roleName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="roleCode" :label="t('role.roleCode')" />
      <el-table-column prop="roleType" :label="t('role.roleType')" width="100">
        <template #default="{ row }">
          {{ row.roleType === 1 ? t('role.typeSystem') : t('role.typeBusiness') }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" :label="t('role.description')" />
      <el-table-column :label="t('common.status.active')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? t('common.status.active') : t('common.status.disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('role.actions')" width="200" fixed="right">
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
      <el-form :model="formData" label-width="100px">
        <el-form-item :label="t('role.roleName')">
          <el-input v-model="formData.roleName" />
        </el-form-item>
        <el-form-item :label="t('role.roleCode')">
          <el-input v-model="formData.roleCode" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item :label="t('role.roleType')">
          <el-radio-group v-model="formData.roleType">
            <el-radio :label="1">{{ t('role.typeSystem') }}</el-radio>
            <el-radio :label="2">{{ t('role.typeBusiness') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('role.description')">
          <el-input v-model="formData.description" type="textarea" />
        </el-form-item>
        <el-form-item :label="t('common.status.active')">
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
import { roleApi } from '@/api/role'
import type { Role } from '@/api/role'
import { useI18n } from '@/composables/useI18n'

const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const tableData = ref<Role[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const selectedRows = ref<Role[]>([])

const queryParams = reactive({
  roleName: '',
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<Role>({
  roleName: '',
  roleCode: '',
  roleType: 2,
  description: '',
  status: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const result = await roleApi.page(queryParams)
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
  queryParams.roleName = ''
  queryParams.pageNum = 1
  loadData()
}

const handleAdd = () => {
  Object.keys(formData).forEach(key => {
    (formData as any)[key] = key === 'roleType' || key === 'status' ? (key === 'roleType' ? 2 : 1) : undefined
  })
  dialogTitle.value = t('role.addRole')
  dialogVisible.value = true
}

const handleEdit = (row: Role) => {
  Object.assign(formData, row)
  dialogTitle.value = t('role.editRole')
  dialogVisible.value = true
}

const handleView = (row: Role) => {
  router.push(`/roles/${row.id}`)
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await roleApi.update(formData.id!, formData)
      ElMessage.success(t('common.messages.updatedSuccess'))
    } else {
      await roleApi.create(formData)
      ElMessage.success(t('common.messages.createdSuccess'))
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error(t('common.operationFailed'))
  }
}

const handleDelete = async (row: Role) => {
  try {
    await ElMessageBox.confirm(t('role.confirmDelete', { name: row.roleName }), t('common.confirm'), { type: 'warning' })
    await roleApi.delete(row.id!)
    ElMessage.success(t('common.messages.deletedSuccess'))
    loadData()
  } catch {
    // cancelled
  }
}

const handleSelectionChange = (rows: Role[]) => {
  selectedRows.value = rows
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning(t('common.messages.pleaseSelectToDelete'))
    return
  }
  try {
    await ElMessageBox.confirm(t('role.confirmDelete', { name: `${selectedRows.value.length}` }), t('common.confirm'), { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id!)
    await Promise.all(ids.map(id => roleApi.delete(id)))
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
      await Promise.all(ids.map(id => roleApi.update(id, { status: 1 } as Role)))
      ElMessage.success(t('common.status.active'))
    } else if (command === 'disable') {
      await Promise.all(ids.map(id => roleApi.update(id, { status: 0 } as Role)))
      ElMessage.success(t('common.status.disabled'))
    } else if (command === 'delete') {
      await ElMessageBox.confirm(t('role.confirmDelete', { name: `${selectedRows.value.length}` }), t('common.confirm'), { type: 'warning' })
      await Promise.all(ids.map(id => roleApi.delete(id)))
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
.role-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>
