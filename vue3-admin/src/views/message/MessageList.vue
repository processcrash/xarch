<template>
  <div class="message-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item label="Message Type">
          <el-select v-model="queryParams.msgType" placeholder="Please select" clearable style="width: 180px">
            <el-option label="System" value="SYSTEM" />
            <el-option label="Notice" value="NOTICE" />
            <el-option label="Private" value="PRIVATE" />
            <el-option label="Todo" value="TODO" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">Send Message</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :name="'all'">
        <template #label>
          <span>All</span>
        </template>
      </el-tab-pane>
      <el-tab-pane :name="'todo'">
        <template #label>
          <span>Todo</span>
        </template>
      </el-tab-pane>
      <el-tab-pane :name="'unread'">
        <template #label>
          <span>
            Unread
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="unread-badge" />
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="Title" min-width="200" />
      <el-table-column prop="msgType" label="Type" width="120">
        <template #default="{ row }">
          <el-tag :type="getMsgTypeColor(row.msgType)" size="small">
            {{ row.msgType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sender" label="Sender" width="120" />
      <el-table-column prop="receiver" label="Receiver" width="120" />
      <el-table-column prop="status" label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? 'Read' : 'Unread' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Create Time" width="180" />
      <el-table-column label="Actions" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleView(row)">View</el-button>
          <el-button size="small" @click="handleEdit(row)">Edit</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="activeTab === 'all'"
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
      style="margin-top: 20px"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="formData" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="Title" prop="title">
          <el-input v-model="formData.title" placeholder="Please enter title" />
        </el-form-item>
        <el-form-item label="Type" prop="msgType">
          <el-select v-model="formData.msgType" placeholder="Please select" style="width: 100%">
            <el-option label="System" value="SYSTEM" />
            <el-option label="Notice" value="NOTICE" />
            <el-option label="Private" value="PRIVATE" />
            <el-option label="Todo" value="TODO" />
          </el-select>
        </el-form-item>
        <el-form-item label="Sender">
          <el-input v-model="formData.sender" placeholder="Sender (optional)" />
        </el-form-item>
        <el-form-item label="Receiver">
          <el-input v-model="formData.receiver" placeholder="Receiver (optional)" />
        </el-form-item>
        <el-form-item label="Content" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="5" placeholder="Please enter content" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="handleSubmit">Submit</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" title="Message Detail" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="Title">{{ currentMessage?.title }}</el-descriptions-item>
        <el-descriptions-item label="Type">{{ currentMessage?.msgType }}</el-descriptions-item>
        <el-descriptions-item label="Sender">{{ currentMessage?.sender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Receiver">{{ currentMessage?.receiver || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Create Time">{{ currentMessage?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="Content">
          <div style="white-space: pre-wrap">{{ currentMessage?.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { messageApi } from '@/api/message'
import type { Message } from '@/api/message'

const formRef = ref()

const loading = ref(false)
const tableData = ref<Message[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('')
const currentMessage = ref<Message | null>(null)
const unreadCount = ref(0)
const activeTab = ref<'all' | 'todo' | 'unread'>('all')

const queryParams = reactive({
  msgType: '',
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<Message>({
  title: '',
  msgType: 'NOTICE',
  content: '',
  sender: '',
  receiver: ''
})

const formRules = {
  title: [
    { required: true, message: 'Title is required', trigger: 'blur' },
    { min: 2, max: 100, message: 'Length 2-100 characters', trigger: 'blur' }
  ],
  msgType: [
    { required: true, message: 'Message type is required', trigger: 'change' }
  ],
  content: [
    { required: true, message: 'Content is required', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'all') {
      const result = await messageApi.page(queryParams)
      tableData.value = result.list || []
      total.value = result.total || 0
    } else if (activeTab.value === 'todo') {
      const list = await messageApi.listTodo()
      tableData.value = list || []
      total.value = (list || []).length
    } else if (activeTab.value === 'unread') {
      const list = await messageApi.listMsg()
      tableData.value = (list || []).filter(m => m.status === 0)
      total.value = tableData.value.length
    }
  } catch {
    ElMessage.error('Failed to load messages')
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const result = await messageApi.count()
    unreadCount.value = result?.unreadCount || 0
  } catch {
    // ignore
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.msgType = ''
  queryParams.pageNum = 1
  loadData()
}

const handleTabChange = () => {
  loadData()
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    title: '',
    msgType: 'NOTICE',
    content: '',
    sender: '',
    receiver: ''
  })
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = 'Send Message'
  dialogVisible.value = true
}

const handleEdit = (row: Message) => {
  Object.assign(formData, row)
  dialogTitle.value = 'Edit Message'
  dialogVisible.value = true
}

const handleView = (row: Message) => {
  currentMessage.value = row
  viewDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (formData.id) {
      await messageApi.update(formData.id, formData)
      ElMessage.success('Updated successfully')
    } else {
      await messageApi.create(formData)
      ElMessage.success('Sent successfully')
    }
    dialogVisible.value = false
    loadData()
    loadUnreadCount()
  } catch (e: any) {
    if (e?.message) {
      ElMessage.error('Operation failed: ' + e.message)
    }
  }
}

const handleDelete = async (row: Message) => {
  try {
    await ElMessageBox.confirm(`Delete message "${row.title}"?`, 'Confirm', { type: 'warning' })
    await messageApi.delete(row.id!)
    ElMessage.success('Deleted successfully')
    loadData()
    loadUnreadCount()
  } catch {
    // cancelled
  }
}

const getMsgTypeColor = (type: string) => {
  const map: Record<string, string> = {
    SYSTEM: 'danger',
    NOTICE: 'warning',
    PRIVATE: 'primary',
    TODO: 'success'
  }
  return map[type] || 'info'
}

onMounted(() => {
  loadData()
  loadUnreadCount()
})
</script>

<style scoped>
.message-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.actions {
  text-align: right;
}

.unread-badge {
  margin-left: 4px;
}
</style>
