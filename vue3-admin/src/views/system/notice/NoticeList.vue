<!-- TODO: i18n - migrate all hardcoded labels/buttons to t('notice.*') keys (namespace not yet added to locale files) -->
<template>
  <div class="notice-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Notice Title">
          <el-input v-model="queryParams.noticeTitle" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item label="Notice Type">
          <el-select v-model="queryParams.noticeType" placeholder="Please select" clearable style="width: 150px">
            <el-option label="Notice" value="1" />
            <el-option label="Announcement" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">Add Notice</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="noticeId" label="ID" width="80" />
      <el-table-column prop="noticeTitle" label="Title" />
      <el-table-column prop="noticeType" label="Type" width="120">
        <template #default="{ row }">
          <el-tag :type="row.noticeType === '1' ? 'success' : 'warning'">
            {{ row.noticeType === '1' ? 'Notice' : 'Announcement' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'">
            {{ row.status === '0' ? 'Normal' : 'Closed' }}
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="Title" required>
          <el-input v-model="formData.noticeTitle" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="Type" required>
          <el-radio-group v-model="formData.noticeType">
            <el-radio label="1">Notice</el-radio>
            <el-radio label="2">Announcement</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Content">
          <el-input v-model="formData.noticeContent" type="textarea" rows="4" />
        </el-form-item>
        <el-form-item label="Status">
          <el-radio-group v-model="formData.status">
            <el-radio label="0">Normal</el-radio>
            <el-radio label="1">Closed</el-radio>
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
import { noticeApi, type SysNotice } from '@/api/notice'

const loading = ref(false)
const tableData = ref<SysNotice[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryParams = reactive({
  noticeTitle: '',
  noticeType: '',
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<SysNotice>({
  noticeTitle: '',
  noticeType: '1',
  noticeContent: '',
  status: '0'
})

const loadData = async () => {
  loading.value = true
  try {
    const result = await noticeApi.list(queryParams)
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
  queryParams.noticeTitle = ''
  queryParams.noticeType = ''
  queryParams.pageNum = 1
  loadData()
}

const handleAdd = () => {
  Object.assign(formData, {
    noticeId: undefined,
    noticeTitle: '',
    noticeType: '1',
    noticeContent: '',
    status: '0'
  })
  dialogTitle.value = 'Add Notice'
  dialogVisible.value = true
}

const handleEdit = (row: SysNotice) => {
  Object.assign(formData, row)
  dialogTitle.value = 'Edit Notice'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (formData.noticeId) {
      await noticeApi.update(formData.noticeId, formData)
      ElMessage.success('Updated successfully')
    } else {
      await noticeApi.create(formData)
      ElMessage.success('Created successfully')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('Operation failed')
  }
}

const handleDelete = async (row: SysNotice) => {
  try {
    await ElMessageBox.confirm(`Delete notice ${row.noticeTitle}?`, 'Confirm', { type: 'warning' })
    await noticeApi.delete(row.noticeId!)
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
.notice-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>