<!-- TODO: i18n - migrate all hardcoded labels/buttons to t('log.*') keys -->
<template>
  <div class="log-list">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="Login Logs" name="login">
        <div class="toolbar">
          <el-form :model="loginQueryParams" inline>
            <el-form-item label="Username">
              <el-input v-model="loginQueryParams.username" placeholder="Please enter" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadLoginLogs">Search</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="loginLogs" v-loading="loginLoading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="Username" />
          <el-table-column prop="ip" label="IP" />
          <el-table-column prop="location" label="Location" />
          <el-table-column prop="loginTime" label="Login Time" />
          <el-table-column prop="loginType" label="Type" width="100">
            <template #default="{ row }">
              {{ row.loginType === 1 ? 'Login' : 'Logout' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="Status" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? 'Success' : 'Failed' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="Message" />
        </el-table>
        <el-pagination
          v-model:current-page="loginQueryParams.pageNum"
          v-model:page-size="loginQueryParams.pageSize"
          :total="loginTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadLoginLogs"
          @current-change="loadLoginLogs"
          style="margin-top: 20px"
        />
      </el-tab-pane>

      <el-tab-pane label="Operation Logs" name="op">
        <div class="toolbar">
          <el-form :model="opQueryParams" inline>
            <el-form-item label="Username">
              <el-input v-model="opQueryParams.username" placeholder="Please enter" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadOpLogs">Search</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="opLogs" v-loading="opLoading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="Username" />
          <el-table-column prop="operation" label="Operation" />
          <el-table-column prop="type" label="Type" width="100" />
          <el-table-column prop="method" label="Method" />
          <el-table-column prop="ip" label="IP" width="120" />
          <el-table-column prop="costTime" label="Cost (ms)" width="100" />
          <el-table-column prop="createTime" label="Time" width="160" />
          <el-table-column prop="status" label="Status" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? 'Success' : 'Failed' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="opQueryParams.pageNum"
          v-model:page-size="opQueryParams.pageSize"
          :total="opTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadOpLogs"
          @current-change="loadOpLogs"
          style="margin-top: 20px"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { logApi, type LoginLog, type OpLog } from '@/api/log'

const activeTab = ref('login')
const loginLoading = ref(false)
const opLoading = ref(false)
const loginLogs = ref<LoginLog[]>([])
const opLogs = ref<OpLog[]>([])
const loginTotal = ref(0)
const opTotal = ref(0)

const loginQueryParams = reactive({
  username: '',
  pageNum: 1,
  pageSize: 10
})

const opQueryParams = reactive({
  username: '',
  pageNum: 1,
  pageSize: 10
})

const loadLoginLogs = async () => {
  loginLoading.value = true
  try {
    const result = await logApi.loginLogPage(loginQueryParams)
    loginLogs.value = result.list || []
    loginTotal.value = result.total || 0
  } catch {
    ElMessage.error('Failed to load login logs')
  } finally {
    loginLoading.value = false
  }
}

const loadOpLogs = async () => {
  opLoading.value = true
  try {
    const result = await logApi.opLogPage(opQueryParams)
    opLogs.value = result.list || []
    opTotal.value = result.total || 0
  } catch {
    ElMessage.error('Failed to load operation logs')
  } finally {
    opLoading.value = false
  }
}

onMounted(() => {
  loadLoginLogs()
  loadOpLogs()
})
</script>

<style scoped>
.log-list {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>