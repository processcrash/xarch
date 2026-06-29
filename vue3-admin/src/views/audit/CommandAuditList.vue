<template>
  <div class="command-audit">
    <div class="toolbar">
      <span class="page-title">{{ t('audit.title') }}</span>
      <el-button type="primary" @click="loadData" :loading="loading">
        <el-icon><Refresh /></el-icon>
        {{ t('common.refresh') }}
      </el-button>
    </div>

    <el-row :gutter="20" v-loading="statsLoading">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#409eff"><Document /></el-icon>
            <span class="stat-label">{{ t('audit.total') }}</span>
          </div>
          <div class="stat-value">{{ stats.total || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#67c23a"><CircleCheck /></el-icon>
            <span class="stat-label">{{ t('audit.approved') }}</span>
          </div>
          <div class="stat-value">{{ stats.approved || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#f56c6c"><CircleClose /></el-icon>
            <span class="stat-label">{{ t('audit.rejected') }}</span>
          </div>
          <div class="stat-value">{{ stats.rejected || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">
            <el-icon class="stat-icon" color="#e6a23c"><Clock /></el-icon>
            <span class="stat-label">{{ t('audit.pending') }}</span>
          </div>
          <div class="stat-value">{{ stats.pending || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px">
      <el-form :model="queryParams" inline class="filter-form">
        <el-form-item :label="t('audit.serverId')">
          <el-input-number v-model="queryParams.serverId" :min="0" controls-position="right" :placeholder="t('audit.serverId')" />
        </el-form-item>
        <el-form-item :label="t('audit.userId')">
          <el-input-number v-model="queryParams.userId" :min="0" controls-position="right" :placeholder="t('audit.userId')" />
        </el-form-item>
        <el-form-item :label="t('audit.riskLevel')">
          <el-select v-model="queryParams.riskLevel" :placeholder="t('common.all')" clearable style="width: 140px">
            <el-option :label="t('common.all')" value="" />
            <el-option :label="t('audit.riskLow')" value="LOW" />
            <el-option :label="t('audit.riskMedium')" value="MEDIUM" />
            <el-option :label="t('audit.riskHigh')" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('audit.approval')">
          <el-select v-model="queryParams.approvalStatus" :placeholder="t('common.all')" clearable style="width: 140px">
            <el-option :label="t('common.all')" value="" />
            <el-option :label="t('common.status.pending')" value="PENDING" />
            <el-option :label="t('common.status.approved')" value="APPROVED" />
            <el-option :label="t('common.status.rejected')" value="REJECTED" />
            <el-option :label="t('common.status.executed')" value="EXECUTED" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('audit.dateRange')">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            :range-separator="t('common.to')"
            :start-placeholder="t('audit.start')"
            :end-placeholder="t('audit.end')"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane :label="t('audit.tabAllLogs')" name="all" />
        <el-tab-pane :label="t('audit.tabPendingApprovals')" name="pending" />
        <el-tab-pane :label="t('audit.tabMyHistory')" name="history" />
      </el-tabs>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="serverName" :label="t('audit.server')" width="140" show-overflow-tooltip />
        <el-table-column prop="userName" :label="t('audit.user')" width="120" show-overflow-tooltip />
        <el-table-column prop="command" :label="t('audit.command')" min-width="220">
          <template #default="{ row }">
            <el-tooltip :content="row.command" placement="top">
              <span class="truncate">{{ row.command }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" :label="t('audit.risk')" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskLevelType(row.riskLevel)" size="small">
              {{ row.riskLevel || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approvalStatus" :label="t('audit.approvalStatus')" width="110">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.approvalStatus)" size="small">
              {{ row.approvalStatus || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="result" :label="t('audit.result')" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.result || '-'" placement="top">
              <span class="truncate">{{ row.result || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('audit.createTime')" width="170" />
        <el-table-column :label="t('audit.actions')" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">{{ t('audit.view') }}</el-button>
            <el-button
              v-if="row.approvalStatus === 'PENDING'"
              size="small"
              type="success"
              @click="handleApprove(row)"
            >
              {{ t('audit.approve') }}
            </el-button>
            <el-button
              v-if="row.approvalStatus === 'PENDING'"
              size="small"
              type="danger"
              @click="handleReject(row)"
            >
              {{ t('audit.reject') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="activeTab !== 'pending'"
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px"
      />
      <el-pagination
        v-else
        v-model:current-page="pendingParams.pageNum"
        v-model:page-size="pendingParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px"
      />
    </el-card>

    <el-drawer v-model="detailDrawerVisible" :title="t('audit.detailTitle')" size="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ currentRecord?.id }}</el-descriptions-item>
        <el-descriptions-item :label="t('audit.server')">{{ currentRecord?.serverName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('audit.user')">{{ currentRecord?.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('audit.riskLevel')">
          <el-tag :type="getRiskLevelType(currentRecord?.riskLevel)" size="small">
            {{ currentRecord?.riskLevel || '-' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('audit.approvalStatus')">
          <el-tag :type="getApprovalStatusType(currentRecord?.approvalStatus)" size="small">
            {{ currentRecord?.approvalStatus || '-' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('audit.createTime')">{{ currentRecord?.createTime }}</el-descriptions-item>
        <el-descriptions-item :label="t('audit.command')">
          <pre class="code-block">{{ currentRecord?.command }}</pre>
        </el-descriptions-item>
        <el-descriptions-item :label="t('audit.result')">
          <pre class="code-block">{{ currentRecord?.result || '-' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="approveDialogVisible" :title="t('audit.approveTitle')" width="500px">
      <el-form :model="approveForm" label-width="100px">
        <el-form-item :label="t('audit.command')">
          <pre class="code-block">{{ currentRecord?.command }}</pre>
        </el-form-item>
        <el-form-item :label="t('audit.comment')">
          <el-input v-model="approveForm.comment" type="textarea" :rows="4" :placeholder="t('audit.commentPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="success" @click="submitApprove" :loading="actionLoading">{{ t('audit.approve') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" :title="t('audit.rejectTitle')" width="500px">
      <el-form :model="rejectForm" label-width="100px" :rules="rejectRules" ref="rejectFormRef">
        <el-form-item :label="t('audit.command')">
          <pre class="code-block">{{ currentRecord?.command }}</pre>
        </el-form-item>
        <el-form-item :label="t('audit.reason')" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" :placeholder="t('audit.reasonPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="danger" @click="submitReject" :loading="actionLoading">{{ t('audit.reject') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  Document,
  CircleCheck,
  CircleClose,
  Clock
} from '@element-plus/icons-vue'
import { auditApi } from '@/api/audit'
import type { CommandAudit, ComplianceStats, RiskLevel, ApprovalStatus } from '@/api/audit'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()
const loading = ref(false)
const statsLoading = ref(false)
const actionLoading = ref(false)

const tableData = ref<CommandAudit[]>([])
const total = ref(0)
const stats = ref<ComplianceStats>({
  total: 0,
  approved: 0,
  rejected: 0,
  pending: 0,
  highRiskCount: 0,
  byRiskLevel: {}
})

const activeTab = ref<'all' | 'pending' | 'history'>('all')
const dateRange = ref<string[]>([])

const queryParams = reactive({
  serverId: undefined as number | undefined,
  userId: undefined as number | undefined,
  riskLevel: '' as string,
  approvalStatus: '' as string,
  startTime: '' as string,
  endTime: '' as string,
  pageNum: 1,
  pageSize: 10
})

const pendingParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const detailDrawerVisible = ref(false)
const currentRecord = ref<CommandAudit | null>(null)

const approveDialogVisible = ref(false)
const approveForm = reactive({ comment: '' })

const rejectDialogVisible = ref(false)
const rejectFormRef = ref()
const rejectForm = reactive({ reason: '' })
const rejectRules = {
  reason: [
    { required: true, message: t('audit.validation.reasonRequired'), trigger: 'blur' },
    { min: 2, max: 500, message: t('audit.validation.reasonLength'), trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'all') {
      const result = await auditApi.page(queryParams)
      tableData.value = result.list || []
      total.value = result.total || 0
    } else if (activeTab.value === 'pending') {
      const result = await auditApi.pending(pendingParams)
      tableData.value = result.list || []
      total.value = result.total || 0
    } else {
      const result = await auditApi.userHistory({
        pageNum: pendingParams.pageNum,
        pageSize: pendingParams.pageSize
      })
      tableData.value = result.list || []
      total.value = result.total || 0
    }
  } catch {
    ElMessage.error(t('audit.loadFailed'))
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  statsLoading.value = true
  try {
    const result = await auditApi.stats({})
    stats.value = result
  } catch {
    // ignore
  } finally {
    statsLoading.value = false
  }
}

const handleSearch = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startTime = dateRange.value[0]
    queryParams.endTime = dateRange.value[1]
  } else {
    queryParams.startTime = ''
    queryParams.endTime = ''
  }
  if (activeTab.value === 'all') {
    queryParams.pageNum = 1
  } else {
    pendingParams.pageNum = 1
  }
  loadData()
}

const handleReset = () => {
  queryParams.serverId = undefined
  queryParams.userId = undefined
  queryParams.riskLevel = ''
  queryParams.approvalStatus = ''
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = []
  queryParams.pageNum = 1
  loadData()
}

const handleTabChange = () => {
  loadData()
}

const handleView = (row: CommandAudit) => {
  currentRecord.value = row
  detailDrawerVisible.value = true
}

const handleApprove = (row: CommandAudit) => {
  currentRecord.value = row
  approveForm.comment = ''
  approveDialogVisible.value = true
}

const submitApprove = async () => {
  if (!currentRecord.value?.id) return
  actionLoading.value = true
  try {
    await auditApi.approve(currentRecord.value.id, { comment: approveForm.comment })
    ElMessage.success(t('audit.approvedSuccess'))
    approveDialogVisible.value = false
    loadData()
    loadStats()
  } catch {
    ElMessage.error(t('audit.approveFailed'))
  } finally {
    actionLoading.value = false
  }
}

const handleReject = (row: CommandAudit) => {
  currentRecord.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!rejectFormRef.value) return
  try {
    await rejectFormRef.value.validate()
  } catch {
    return
  }
  if (!currentRecord.value?.id) return
  actionLoading.value = true
  try {
    await auditApi.reject(currentRecord.value.id, { reason: rejectForm.reason })
    ElMessage.success(t('audit.rejectedSuccess'))
    rejectDialogVisible.value = false
    loadData()
    loadStats()
  } catch {
    ElMessage.error(t('audit.rejectFailed'))
  } finally {
    actionLoading.value = false
  }
}

const getRiskLevelType = (level?: RiskLevel) => {
  switch (level) {
    case 'HIGH': return 'danger'
    case 'MEDIUM': return 'warning'
    case 'LOW': return 'success'
    default: return 'info'
  }
}

const getApprovalStatusType = (status?: ApprovalStatus) => {
  switch (status) {
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    case 'PENDING': return 'warning'
    case 'EXECUTED': return 'primary'
    default: return 'info'
  }
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.command-audit {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.stat-card {
  height: 110px;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.stat-icon {
  font-size: 20px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.filter-form {
  margin-bottom: 10px;
}

.truncate {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.code-block {
  margin: 0;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}
</style>
