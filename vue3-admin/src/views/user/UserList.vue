<template>
  <div class="user-list">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">Add User</el-button>
      <el-button @click="loadUsers">Refresh</el-button>
    </div>

    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="Username" />
      <el-table-column prop="email" label="Email" />
      <el-table-column prop="mobile" label="Mobile" />
      <el-table-column prop="status" label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? 'Active' : 'Disabled' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">Edit</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadUsers"
      @current-change="loadUsers"
      style="margin-top: 20px"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'
import type { User } from '@/api/user'

const users = ref<User[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadUsers = async () => {
  loading.value = true
  try {
    const data = await userApi.list()
    users.value = data || []
    total.value = data?.length || 0
  } catch {
    ElMessage.error('Failed to load users')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  ElMessage.info('Add user - feature coming soon')
}

const handleEdit = (row: User) => {
  ElMessage.info(`Edit user ${row.username} - feature coming soon`)
}

const handleDelete = async (row: User) => {
  try {
    await ElMessageBox.confirm(`Delete user ${row.username}?`, 'Confirm', {
      type: 'warning'
    })
    await userApi.delete(row.id!)
    ElMessage.success('Deleted successfully')
    loadUsers()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-list {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>