<template>
  <div class="dict-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Dictionary Name">
          <el-input v-model="queryParams.dictName" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">Add Dictionary</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="dictName" label="Dictionary Name" />
      <el-table-column prop="dictCode" label="Code" />
      <el-table-column prop="description" label="Description" />
      <el-table-column prop="status" label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? 'Active' : 'Disabled' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="250" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">Edit</el-button>
          <el-button size="small" @click="handleViewData(row)">Data</el-button>
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
        <el-form-item label="Dictionary Name">
          <el-input v-model="formData.dictName" />
        </el-form-item>
        <el-form-item label="Code">
          <el-input v-model="formData.dictCode" :disabled="!!formData.id" />
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

    <el-dialog v-model="dataDialogVisible" title="Dictionary Data" width="600px">
      <el-table :data="dictDataList" stripe>
        <el-table-column prop="dictLabel" label="Label" />
        <el-table-column prop="dictValue" label="Value" />
        <el-table-column prop="sortOrder" label="Sort" width="80" />
        <el-table-column prop="status" label="Status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? 'Active' : 'Disabled' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dictApi, type Dict, type DictData } from '@/api/dict'

const loading = ref(false)
const tableData = ref<Dict[]>([])
const dictDataList = ref<DictData[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dataDialogVisible = ref(false)
const dialogTitle = ref('')

const queryParams = reactive({
  dictName: '',
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<Dict>({
  dictName: '',
  dictCode: '',
  description: '',
  status: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const result = await dictApi.page(queryParams)
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
  queryParams.dictName = ''
  queryParams.pageNum = 1
  loadData()
}

const handleAdd = () => {
  Object.keys(formData).forEach(key => {
    (formData as any)[key] = key === 'status' ? 1 : undefined
  })
  dialogTitle.value = 'Add Dictionary'
  dialogVisible.value = true
}

const handleEdit = (row: Dict) => {
  Object.assign(formData, row)
  dialogTitle.value = 'Edit Dictionary'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await dictApi.update(formData.id!, formData)
      ElMessage.success('Updated successfully')
    } else {
      await dictApi.create(formData)
      ElMessage.success('Created successfully')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('Operation failed')
  }
}

const handleDelete = async (row: Dict) => {
  try {
    await ElMessageBox.confirm(`Delete dictionary ${row.dictName}?`, 'Confirm', { type: 'warning' })
    await dictApi.delete(row.id!)
    ElMessage.success('Deleted successfully')
    loadData()
  } catch {
    // cancelled
  }
}

const handleViewData = async (row: Dict) => {
  const result = await dictApi.getDataById(row.id!)
  dictDataList.value = result
  dataDialogVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dict-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>