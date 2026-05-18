<template>
  <div class="job-log-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Job Name">
          <el-input v-model="queryParams.jobName" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item label="Job Group">
          <el-input v-model="queryParams.jobGroup" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="queryParams.status" placeholder="Please select" clearable style="width: 120px">
            <el-option label="Success" value="0" />
            <el-option label="Failed" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
        <el-button type="danger" @click="handleClean">Clean Logs</el-button>
      </el-form>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="jobLogId" label="ID" width="80" />
      <el-table-column prop="jobName" label="Job Name" />
      <el-table-column prop="jobGroup" label="Job Group" width="120" />
      <el-table-column prop="invokeTarget" label="Invoke Target" />
      <el-table-column prop="jobMessage" label="Message" show-overflow-tooltip />
      <el-table-column prop="status" label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'">
            {{ row.status === '0' ? 'Success' : 'Failed' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="Start Time" width="180" />
      <el-table-column prop="endTime" label="End Time" width="180" />
      <el-table-column label="Actions" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleViewDetail(row)">Detail</el-button>
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

    <el-dialog v-model="detailVisible" title="Job Log Detail" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="Job Name">{{ detailData.jobName }}</el-descriptions-item>
        <el-descriptions-item label="Job Group">{{ detailData.jobGroup }}</el-descriptions-item>
        <el-descriptions-item label="Invoke Target">{{ detailData.invokeTarget }}</el-descriptions-item>
        <el-descriptions-item label="Status">
          <el-tag :type="detailData.status === '0' ? 'success' : 'danger'">
            {{ detailData.status === '0' ? 'Success' : 'Failed' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Message">{{ detailData.jobMessage }}</el-descriptions-item>
        <el-descriptions-item label="Exception" v-if="detailData.exceptionInfo">
          <pre style="white-space: pre-wrap">{{ detailData.exceptionInfo }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="Start Time">{{ detailData.startTime }}</el-descriptions-item>
        <el-descriptions-item label="End Time">{{ detailData.endTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { jobLogApi, type SysJobLog } from '@/api/jobLog'

const loading = ref(false)
const tableData = ref<SysJobLog[]>([])
const total = ref(0)
const detailVisible = ref(false)
const detailData = ref<SysJobLog>({})

const queryParams = reactive({
  jobName: '',
  jobGroup: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const loadData = async () => {
  loading.value = true
  try {
    const result = await jobLogApi.list(queryParams)
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
  queryParams.jobName = ''
  queryParams.jobGroup = ''
  queryParams.status = ''
  queryParams.pageNum = 1
  loadData()
}

const handleViewDetail = (row: SysJobLog) => {
  detailData.value = row
  detailVisible.value = true
}

const handleDelete = async (row: SysJobLog) => {
  try {
    await ElMessageBox.confirm(`Delete this log?`, 'Confirm', { type: 'warning' })
    await jobLogApi.delete(row.jobLogId!)
    ElMessage.success('Deleted successfully')
    loadData()
  } catch {
    // cancelled
  }
}

const handleClean = async () => {
  try {
    await ElMessageBox.confirm('Clean all job logs?', 'Confirm', { type: 'warning' })
    await jobLogApi.clean()
    ElMessage.success('Cleaned successfully')
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
.job-log-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>