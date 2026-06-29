<template>
  <div class="message-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item :label="t('message.messageType')">
          <el-select v-model="queryParams.msgType" :placeholder="t('common.pleaseSelect')" clearable style="width: 180px">
            <el-option :label="t('message.typeSystem')" value="SYSTEM" />
            <el-option :label="t('message.typeNotice')" value="NOTICE" />
            <el-option :label="t('message.typePrivate')" value="PRIVATE" />
            <el-option :label="t('message.typeTodo')" value="TODO" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">{{ t('message.sendMessage') }}</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :name="'all'">
        <template #label>
          <span>{{ t('message.tabAll') }}</span>
        </template>
      </el-tab-pane>
      <el-tab-pane :name="'todo'">
        <template #label>
          <span>{{ t('message.tabTodo') }}</span>
        </template>
      </el-tab-pane>
      <el-tab-pane :name="'unread'">
        <template #label>
          <span>
            {{ t('message.tabUnread') }}
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="unread-badge" />
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" :label="t('message.titleField')" min-width="200" />
      <el-table-column prop="msgType" :label="t('message.type')" width="120">
        <template #default="{ row }">
          <el-tag :type="getMsgTypeColor(row.msgType)" size="small">
            {{ row.msgType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sender" :label="t('message.sender')" width="120" />
      <el-table-column prop="receiver" :label="t('message.receiver')" width="120" />
      <el-table-column :label="t('common.status.active')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? t('common.status.read') : t('common.status.unread') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" :label="t('message.createTime')" width="180" />
      <el-table-column :label="t('message.actions')" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleView(row)">{{ t('common.view') }}</el-button>
          <el-button size="small" @click="handleEdit(row)">{{ t('common.edit') }}</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
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
        <el-form-item :label="t('message.titleField')" prop="title">
          <el-input v-model="formData.title" :placeholder="t('message.titlePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('message.type')" prop="msgType">
          <el-select v-model="formData.msgType" :placeholder="t('common.pleaseSelect')" style="width: 100%">
            <el-option :label="t('message.typeSystem')" value="SYSTEM" />
            <el-option :label="t('message.typeNotice')" value="NOTICE" />
            <el-option :label="t('message.typePrivate')" value="PRIVATE" />
            <el-option :label="t('message.typeTodo')" value="TODO" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('message.sender')">
          <el-input v-model="formData.sender" :placeholder="t('message.senderPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('message.receiver')">
          <el-input v-model="formData.receiver" :placeholder="t('message.receiverPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('message.content')" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="5" :placeholder="t('message.contentPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ t('common.submit') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" :title="t('message.detailTitle')" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item :label="t('message.titleField')">{{ currentMessage?.title }}</el-descriptions-item>
        <el-descriptions-item :label="t('message.type')">{{ currentMessage?.msgType }}</el-descriptions-item>
        <el-descriptions-item :label="t('message.sender')">{{ currentMessage?.sender || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('message.receiver')">{{ currentMessage?.receiver || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('message.createTime')">{{ currentMessage?.createTime }}</el-descriptions-item>
        <el-descriptions-item :label="t('message.content')">
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
import { useI18n } from '@/composables/useI18n'

const formRef = ref()
const { t } = useI18n()

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
    { required: true, message: t('message.validation.titleRequired'), trigger: 'blur' },
    { min: 2, max: 100, message: t('message.validation.lengthRange'), trigger: 'blur' }
  ],
  msgType: [
    { required: true, message: t('message.validation.typeRequired'), trigger: 'change' }
  ],
  content: [
    { required: true, message: t('message.validation.contentRequired'), trigger: 'blur' }
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
    ElMessage.error(t('common.messages.loadFailed'))
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
  dialogTitle.value = t('message.sendMessage')
  dialogVisible.value = true
}

const handleEdit = (row: Message) => {
  Object.assign(formData, row)
  dialogTitle.value = t('message.editMessage')
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
      ElMessage.success(t('common.messages.updatedSuccess'))
    } else {
      await messageApi.create(formData)
      ElMessage.success(t('message.sentSuccess'))
    }
    dialogVisible.value = false
    loadData()
    loadUnreadCount()
  } catch (e: any) {
    if (e?.message) {
      ElMessage.error(t('common.operationFailed') + ': ' + e.message)
    }
  }
}

const handleDelete = async (row: Message) => {
  try {
    await ElMessageBox.confirm(t('message.confirmDelete', { title: row.title }), t('common.confirm'), { type: 'warning' })
    await messageApi.delete(row.id!)
    ElMessage.success(t('common.messages.deletedSuccess'))
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
