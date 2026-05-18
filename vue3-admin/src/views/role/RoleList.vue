<template>
  <div class="role-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Role Name">
          <el-input v-model="queryParams.roleName" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">Add Role</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleName" label="Role Name" />
      <el-table-column prop="roleCode" label="Role Code" />
      <el-table-column prop="roleType" label="Type" width="100">
        <template #default="{ row }">
          {{ row.roleType === 1 ? 'System' : 'Business' }}
        </template>
      </el-table-column>
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
        <el-form-item label="Role Name">
          <el-input v-model="formData.roleName" />
        </el-form-item>
        <el-form-item label="Role Code">
          <el-input v-model="formData.roleCode" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="Type">
          <el-radio-group v-model="formData.roleType">
            <el-radio :label="1">System</el-radio>
            <el-radio :label="2">Business</el-radio>
          </el-radio-group>
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
import { roleApi } from '@/api/role'
import type { Role } from '@/api/role'

const loading = ref(false)
const tableData = ref<Role[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')

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
  queryParams.roleName = ''
  queryParams.pageNum = 1
  loadData()
}

const handleAdd = () => {
  Object.keys(formData).forEach(key => {
    (formData as any)[key] = key === 'roleType' || key === 'status' ? (key === 'roleType' ? 2 : 1) : undefined
  })
  dialogTitle.value = 'Add Role'
  dialogVisible.value = true
}

const handleEdit = (row: Role) => {
  Object.assign(formData, row)
  dialogTitle.value = 'Edit Role'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await roleApi.update(formData.id!, formData)
      ElMessage.success('Updated successfully')
    } else {
      await roleApi.create(formData)
      ElMessage.success('Created successfully')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('Operation failed')
  }
}

const handleDelete = async (row: Role) => {
  try {
    await ElMessageBox.confirm(`Delete role ${row.roleName}?`, 'Confirm', { type: 'warning' })
    await roleApi.delete(row.id!)
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
.role-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>