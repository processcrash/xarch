<template>
  <div class="dept-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Department Name">
          <el-input v-model="queryParams.deptName" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">Add Department</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe row-key="id" default-expand-all>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="deptName" label="Department Name" />
      <el-table-column prop="deptCode" label="Code" />
      <el-table-column prop="leader" label="Leader" />
      <el-table-column prop="phone" label="Phone" />
      <el-table-column prop="sortOrder" label="Sort" width="80" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="Parent Dept">
          <el-tree-select v-model="formData.parentId" :data="deptTree" :props="{ label: 'deptName', value: 'id' }" placeholder="Root department" clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="Department Name">
          <el-input v-model="formData.deptName" />
        </el-form-item>
        <el-form-item label="Code">
          <el-input v-model="formData.deptCode" />
        </el-form-item>
        <el-form-item label="Leader">
          <el-input v-model="formData.leader" />
        </el-form-item>
        <el-form-item label="Phone">
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="Sort">
          <el-input-number v-model="formData.sortOrder" :min="0" />
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
import { deptApi } from '@/api/dept'
import type { Dept } from '@/api/dept'

const loading = ref(false)
const tableData = ref<Dept[]>([])
const deptTree = ref<Dept[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryParams = reactive({
  deptName: '',
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<Dept>({
  deptName: '',
  deptCode: '',
  leader: '',
  phone: '',
  sortOrder: 0,
  status: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const result = await deptApi.page(queryParams)
    tableData.value = result.list || []
    total.value = result.total || 0
  } catch {
    ElMessage.error('Failed to load data')
  } finally {
    loading.value = false
  }
}

const loadTree = async () => {
  const result = await deptApi.tree()
  deptTree.value = [{ id: 0, deptName: 'Root', children: result }] as any
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.deptName = ''
  queryParams.pageNum = 1
  loadData()
}

const handleAdd = () => {
  Object.keys(formData).forEach(key => {
    (formData as any)[key] = key === 'sortOrder' || key === 'status' ? (key === 'sortOrder' ? 0 : 1) : undefined
  })
  dialogTitle.value = 'Add Department'
  dialogVisible.value = true
}

const handleEdit = (row: Dept) => {
  Object.assign(formData, row)
  dialogTitle.value = 'Edit Department'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await deptApi.update(formData.id!, formData)
      ElMessage.success('Updated successfully')
    } else {
      await deptApi.create(formData)
      ElMessage.success('Created successfully')
    }
    dialogVisible.value = false
    loadData()
    loadTree()
  } catch {
    ElMessage.error('Operation failed')
  }
}

const handleDelete = async (row: Dept) => {
  try {
    await ElMessageBox.confirm(`Delete department ${row.deptName}?`, 'Confirm', { type: 'warning' })
    await deptApi.delete(row.id!)
    ElMessage.success('Deleted successfully')
    loadData()
    loadTree()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadData()
  loadTree()
})
</script>

<style scoped>
.dept-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>