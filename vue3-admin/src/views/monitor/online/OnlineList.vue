<template>
  <div class="online-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Username">
          <el-input v-model="queryParams.userName" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item label="IP Address">
          <el-input v-model="queryParams.ipaddr" placeholder="Please enter" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
        <el-button type="danger" @click="handleForceLogout" :disabled="selectedUsers.length === 0">
          Force Logout
        </el-button>
      </el-form>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="userName" label="Username" />
      <el-table-column prop="ipaddr" label="IP Address" />
      <el-table-column prop="loginLocation" label="Login Location" />
      <el-table-column prop="browser" label="Browser" />
      <el-table-column prop="os" label="OS" />
      <el-table-column prop="loginTime" label="Login Time" width="180">
        <template #default="{ row }">
          {{ formatTime(row.loginTime) }}
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="150">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleSingleLogout(row)">Force Logout</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onlineApi, type SysUserOnline } from '@/api/online'

const loading = ref(false)
const tableData = ref<SysUserOnline[]>([])
const total = ref(0)
const selectedUsers = ref<SysUserOnline[]>([])

const queryParams = reactive({
  userName: '',
  ipaddr: '',
  pageNum: 1,
  pageSize: 10
})

const loadData = async () => {
  loading.value = true
  try {
    const result = await onlineApi.list(queryParams)
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
  queryParams.userName = ''
  queryParams.ipaddr = ''
  queryParams.pageNum = 1
  loadData()
}

const handleSelectionChange = (selection: SysUserOnline[]) => {
  selectedUsers.value = selection
}

const handleSingleLogout = async (row: SysUserOnline) => {
  try {
    await ElMessageBox.confirm(`Force logout user ${row.userName}?`, 'Confirm', { type: 'warning' })
    await onlineApi.forceLogout(row.tokenId)
    ElMessage.success('Force logout successfully')
    loadData()
  } catch {
    // cancelled
  }
}

const handleForceLogout = async () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('Please select users')
    return
  }
  try {
    await ElMessageBox.confirm(`Force logout ${selectedUsers.value.length} users?`, 'Confirm', { type: 'warning' })
    for (const user of selectedUsers.value) {
      await onlineApi.forceLogout(user.tokenId)
    }
    ElMessage.success('Force logout successfully')
    loadData()
  } catch {
    // cancelled
  }
}

const formatTime = (time: number) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.online-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>