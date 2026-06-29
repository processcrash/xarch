<!-- TODO: i18n - migrate all hardcoded labels/buttons to t('post.*') keys (namespace not yet added to locale files) -->
<template>
  <div class="post-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Post Code">
          <el-input v-model="queryParams.postCode" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item label="Post Name">
          <el-input v-model="queryParams.postName" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="queryParams.status" placeholder="Please select" clearable style="width: 120px">
            <el-option label="Normal" value="0" />
            <el-option label="Disabled" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">Add Post</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="postId" label="ID" width="80" />
      <el-table-column prop="postCode" label="Post Code" />
      <el-table-column prop="postName" label="Post Name" />
      <el-table-column prop="postSort" label="Sort" width="80" />
      <el-table-column prop="status" label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'">
            {{ row.status === '0' ? 'Normal' : 'Disabled' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Create Time" width="180" />
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
        <el-form-item label="Post Code" required>
          <el-input v-model="formData.postCode" :disabled="!!formData.postId" />
        </el-form-item>
        <el-form-item label="Post Name" required>
          <el-input v-model="formData.postName" />
        </el-form-item>
        <el-form-item label="Sort" required>
          <el-input-number v-model="formData.postSort" :min="0" />
        </el-form-item>
        <el-form-item label="Status">
          <el-radio-group v-model="formData.status">
            <el-radio label="0">Normal</el-radio>
            <el-radio label="1">Disabled</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Remark">
          <el-input v-model="formData.remark" type="textarea" />
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
import { postApi, type SysPost } from '@/api/post'

const loading = ref(false)
const tableData = ref<SysPost[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryParams = reactive({
  postCode: '',
  postName: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<SysPost>({
  postCode: '',
  postName: '',
  postSort: 0,
  status: '0',
  remark: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const result = await postApi.list(queryParams)
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
  queryParams.postCode = ''
  queryParams.postName = ''
  queryParams.status = ''
  queryParams.pageNum = 1
  loadData()
}

const handleAdd = () => {
  Object.assign(formData, {
    postId: undefined,
    postCode: '',
    postName: '',
    postSort: 0,
    status: '0',
    remark: ''
  })
  dialogTitle.value = 'Add Post'
  dialogVisible.value = true
}

const handleEdit = (row: SysPost) => {
  Object.assign(formData, row)
  dialogTitle.value = 'Edit Post'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (formData.postId) {
      await postApi.update(formData.postId, formData)
      ElMessage.success('Updated successfully')
    } else {
      await postApi.create(formData)
      ElMessage.success('Created successfully')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('Operation failed')
  }
}

const handleDelete = async (row: SysPost) => {
  try {
    await ElMessageBox.confirm(`Delete post ${row.postName}?`, 'Confirm', { type: 'warning' })
    await postApi.delete(row.postId!)
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
.post-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>