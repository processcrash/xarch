<template>
  <div class="job-list">
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
            <el-option label="Normal" value="0" />
            <el-option label="Paused" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">Add Job</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="jobId" label="ID" width="80" />
      <el-table-column prop="jobName" label="Job Name" />
      <el-table-column prop="jobGroup" label="Job Group" width="120" />
      <el-table-column prop="invokeTarget" label="Invoke Target" />
      <el-table-column prop="cronExpression" label="Cron" width="120" />
      <el-table-column prop="status" label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'warning'">
            {{ row.status === '0' ? 'Normal' : 'Paused' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="concurrent" label="Concurrent" width="100">
        <template #default="{ row }">
          {{ row.concurrent === '0' ? 'Allow' : 'Prevent' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Create Time" width="180" />
      <el-table-column label="Actions" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">Edit</el-button>
          <el-button size="small" type="warning" @click="handleRun(row)">Run Once</el-button>
          <el-button size="small" :type="row.status === '0' ? 'warning' : 'success'" @click="handleChangeStatus(row)">
            {{ row.status === '0' ? 'Pause' : 'Resume' }}
          </el-button>
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
      <el-form :model="formData" label-width="120px">
        <el-form-item label="Job Name" required>
          <el-input v-model="formData.jobName" />
        </el-form-item>
        <el-form-item label="Job Group" required>
          <el-input v-model="formData.jobGroup" />
        </el-form-item>
        <el-form-item label="Invoke Target" required>
          <el-input v-model="formData.invokeTarget" placeholder="e.g. ryTask.test" />
        </el-form-item>
        <el-form-item label="Cron Expression" required>
          <el-input v-model="formData.cronExpression" placeholder="e.g. 0/30 * * * * ?" />
        </el-form-item>
        <el-form-item label="Concurrent">
          <el-radio-group v-model="formData.concurrent">
            <el-radio label="0">Allow</el-radio>
            <el-radio label="1">Prevent</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Misfire Policy">
          <el-radio-group v-model="formData.misfirePolicy">
            <el-radio label="0">Default</el-radio>
            <el-radio label="1">Fire Immediately</el-radio>
            <el-radio label="2">Execute Once</el-radio>
            <el-radio label="3">Do Not Execute</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Status">
          <el-radio-group v-model="formData.status">
            <el-radio label="0">Normal</el-radio>
            <el-radio label="1">Paused</el-radio>
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
import { jobApi, type SysJob } from '@/api/job'

const loading = ref(false)
const tableData = ref<SysJob[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryParams = reactive({
  jobName: '',
  jobGroup: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<SysJob>({
  jobName: '',
  jobGroup: 'DEFAULT',
  invokeTarget: '',
  cronExpression: '',
  concurrent: '1',
  misfirePolicy: '0',
  status: '0',
  remark: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const result = await jobApi.list(queryParams)
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

const handleAdd = () => {
  Object.assign(formData, {
    jobId: undefined,
    jobName: '',
    jobGroup: 'DEFAULT',
    invokeTarget: '',
    cronExpression: '',
    concurrent: '1',
    misfirePolicy: '0',
    status: '0',
    remark: ''
  })
  dialogTitle.value = 'Add Job'
  dialogVisible.value = true
}

const handleEdit = (row: SysJob) => {
  Object.assign(formData, row)
  dialogTitle.value = 'Edit Job'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (formData.jobId) {
      await jobApi.update(formData.jobId, formData)
      ElMessage.success('Updated successfully')
    } else {
      await jobApi.create(formData)
      ElMessage.success('Created successfully')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('Operation failed')
  }
}

const handleRun = async (row: SysJob) => {
  try {
    await jobApi.run(row.jobId!)
    ElMessage.success('Triggered successfully')
  } catch {
    ElMessage.error('Trigger failed')
  }
}

const handleChangeStatus = async (row: SysJob) => {
  try {
    await jobApi.changeStatus(row.jobId!, row.status === '0' ? '1' : '0')
    ElMessage.success('Status changed successfully')
    loadData()
  } catch {
    ElMessage.error('Status change failed')
  }
}

const handleDelete = async (row: SysJob) => {
  try {
    await ElMessageBox.confirm(`Delete job ${row.jobName}?`, 'Confirm', { type: 'warning' })
    await jobApi.delete(row.jobId!)
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
.job-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>