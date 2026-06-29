<template>
  <div class="client-list">
    <div class="toolbar">
      <el-form :model="queryParams" inline>
        <el-form-item :label="t('client.clientId')">
          <el-input v-model="queryParams.clientId" :placeholder="t('client.placeholders.clientId')" clearable />
        </el-form-item>
        <el-form-item :label="t('client.clientName')">
          <el-input v-model="queryParams.clientName" :placeholder="t('client.placeholders.clientName')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
      <div class="actions">
        <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
          {{ t('common.batchDelete') }}
        </el-button>
        <el-button type="primary" @click="handleAdd">{{ t('client.addClient') }}</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="clientId" :label="t('client.clientId')" width="180" />
      <el-table-column prop="clientKey" :label="t('client.clientKey')" width="180" />
      <el-table-column prop="clientName" :label="t('client.clientName')" min-width="160" />
      <el-table-column prop="grantTypes" :label="t('client.grantTypes')" min-width="180" show-overflow-tooltip />
      <el-table-column prop="scope" :label="t('client.scope')" width="120" />
      <el-table-column :label="t('client.status')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? t('common.status.active') : t('common.status.disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" :label="t('client.createTime')" width="180" />
      <el-table-column :label="t('client.actions')" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">{{ t('common.edit') }}</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="formData" label-width="120px" :rules="formRules" ref="formRef">
        <el-form-item :label="t('client.clientId')" prop="clientId">
          <el-input v-model="formData.clientId" :placeholder="t('client.placeholders.uniqueClientId')" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item :label="t('client.clientKey')" prop="clientKey">
          <el-input v-model="formData.clientKey" :placeholder="t('client.placeholders.clientKey')" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item :label="t('client.clientSecret')" prop="clientSecret" v-if="!formData.id">
          <el-input v-model="formData.clientSecret" type="password" show-password :placeholder="t('client.placeholders.clientSecret')" />
        </el-form-item>
        <el-form-item :label="t('client.clientName')" prop="clientName">
          <el-input v-model="formData.clientName" :placeholder="t('client.placeholders.clientName')" />
        </el-form-item>
        <el-form-item :label="t('client.grantTypes')" prop="grantTypes">
          <el-input v-model="formData.grantTypes" :placeholder="t('client.placeholders.grantTypes')" />
        </el-form-item>
        <el-form-item :label="t('client.scope')" prop="scope">
          <el-input v-model="formData.scope" :placeholder="t('client.placeholders.scope')" />
        </el-form-item>
        <el-form-item :label="t('client.status')" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">{{ t('common.status.active') }}</el-radio>
            <el-radio :label="0">{{ t('common.status.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ t('common.submit') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clientApi } from '@/api/client'
import type { Client } from '@/api/client'
import { useI18n } from '@/composables/useI18n'

const formRef = ref()
const { t } = useI18n()

const loading = ref(false)
const tableData = ref<Client[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const selectedRows = ref<Client[]>([])

const queryParams = reactive({
  clientId: '',
  clientName: '',
  pageNum: 1,
  pageSize: 10
})

const formData = reactive<Client>({
  clientId: '',
  clientKey: '',
  clientSecret: '',
  clientName: '',
  grantTypes: '',
  scope: '',
  status: 1
})

const formRules = {
  clientId: [
    { required: true, message: t('client.validation.clientIdRequired'), trigger: 'blur' },
    { min: 2, max: 50, message: t('client.validation.lengthRange2to50'), trigger: 'blur' }
  ],
  clientKey: [
    { required: true, message: t('client.validation.clientKeyRequired'), trigger: 'blur' },
    { min: 2, max: 50, message: t('client.validation.lengthRange2to50'), trigger: 'blur' }
  ],
  clientName: [
    { required: true, message: t('client.validation.clientNameRequired'), trigger: 'blur' },
    { min: 2, max: 50, message: t('client.validation.lengthRange2to50'), trigger: 'blur' }
  ],
  clientSecret: [
    { required: true, message: t('client.validation.clientSecretRequired'), trigger: 'blur' },
    { min: 6, max: 100, message: t('client.validation.lengthRange6to100'), trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const result = await clientApi.page(queryParams)
    tableData.value = result.list || []
    total.value = result.total || 0
  } catch {
    ElMessage.error(t('client.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.clientId = ''
  queryParams.clientName = ''
  queryParams.pageNum = 1
  loadData()
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    clientId: '',
    clientKey: '',
    clientSecret: '',
    clientName: '',
    grantTypes: '',
    scope: '',
    status: 1
  })
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = t('client.addClient')
  dialogVisible.value = true
}

const handleEdit = (row: Client) => {
  Object.assign(formData, row)
  formData.clientSecret = ''
  dialogTitle.value = t('client.editClient')
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (formData.id) {
      await clientApi.update(formData.id, formData)
      ElMessage.success(t('common.messages.updatedSuccess'))
    } else {
      await clientApi.create(formData)
      ElMessage.success(t('common.messages.createdSuccess'))
    }
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    if (e?.message) {
      ElMessage.error(t('common.operationFailed') + ': ' + e.message)
    }
  }
}

const handleDelete = async (row: Client) => {
  try {
    await ElMessageBox.confirm(t('client.confirmDelete', { name: row.clientName }), t('common.confirm'), { type: 'warning' })
    await clientApi.delete({ ids: [row.id!] })
    ElMessage.success(t('common.messages.deletedSuccess'))
    loadData()
  } catch {
    // cancelled
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning(t('common.messages.pleaseSelectToDelete'))
    return
  }
  try {
    await ElMessageBox.confirm(t('client.confirmDelete', { name: `${selectedRows.value.length}` }), t('common.confirm'), { type: 'warning' })
    const ids = selectedRows.value.map(row => row.id!)
    await clientApi.delete({ ids })
    ElMessage.success(t('common.messages.deletedSuccess'))
    selectedRows.value = []
    loadData()
  } catch {
    // cancelled
  }
}

const handleSelectionChange = (rows: Client[]) => {
  selectedRows.value = rows
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.client-list {
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
</style>
