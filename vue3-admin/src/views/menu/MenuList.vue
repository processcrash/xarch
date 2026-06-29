<!-- TODO: i18n - migrate all hardcoded labels/buttons to t('menu.*') keys -->
<template>
  <div class="menu-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Menu Name">
          <el-input v-model="queryParams.menuName" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">Add Menu</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe row-key="id" default-expand-all>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="menuName" label="Menu Name">
        <template #default="{ row }">
          {{ row.menuName }}
        </template>
      </el-table-column>
      <el-table-column prop="menuCode" label="Code" />
      <el-table-column prop="menuType" label="Type" width="100">
        <template #default="{ row }">
          {{ row.menuType === 1 ? 'Menu' : 'Button' }}
        </template>
      </el-table-column>
      <el-table-column prop="path" label="Path" />
      <el-table-column prop="icon" label="Icon" width="100" />
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
        <el-form-item label="Parent Menu">
          <el-tree-select v-model="formData.parentId" :data="menuTree" :props="{ label: 'menuName', value: 'id' }" placeholder="Root menu" clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="Menu Name">
          <el-input v-model="formData.menuName" />
        </el-form-item>
        <el-form-item label="Menu Code">
          <el-input v-model="formData.menuCode" />
        </el-form-item>
        <el-form-item label="Type">
          <el-radio-group v-model="formData.menuType">
            <el-radio :label="1">Menu</el-radio>
            <el-radio :label="2">Button</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Path">
          <el-input v-model="formData.path" />
        </el-form-item>
        <el-form-item label="Icon">
          <el-input v-model="formData.icon" />
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
import { menuApi } from '@/api/menu'
import type { Menu } from '@/api/menu'

const loading = ref(false)
const tableData = ref<Menu[]>([])
const menuTree = ref<Menu[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryParams = reactive({
  menuName: '',
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<Menu>({
  menuName: '',
  menuCode: '',
  menuType: 1,
  path: '',
  icon: '',
  sortOrder: 0,
  status: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const result = await menuApi.page(queryParams)
    tableData.value = result.list || []
    total.value = result.total || 0
  } catch {
    ElMessage.error('Failed to load data')
  } finally {
    loading.value = false
  }
}

const loadTree = async () => {
  const result = await menuApi.tree()
  menuTree.value = [{ id: 0, menuName: 'Root', children: result }] as any
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.menuName = ''
  queryParams.pageNum = 1
  loadData()
}

const handleAdd = () => {
  Object.keys(formData).forEach(key => {
    (formData as any)[key] = key === 'menuType' || key === 'sortOrder' || key === 'status' ? (key === 'menuType' ? 1 : key === 'sortOrder' ? 0 : 1) : undefined
  })
  dialogTitle.value = 'Add Menu'
  dialogVisible.value = true
}

const handleEdit = (row: Menu) => {
  Object.assign(formData, row)
  dialogTitle.value = 'Edit Menu'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (formData.id) {
      await menuApi.update(formData.id!, formData)
      ElMessage.success('Updated successfully')
    } else {
      await menuApi.create(formData)
      ElMessage.success('Created successfully')
    }
    dialogVisible.value = false
    loadData()
    loadTree()
  } catch {
    ElMessage.error('Operation failed')
  }
}

const handleDelete = async (row: Menu) => {
  try {
    await ElMessageBox.confirm(`Delete menu ${row.menuName}?`, 'Confirm', { type: 'warning' })
    await menuApi.delete(row.id!)
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
.menu-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>