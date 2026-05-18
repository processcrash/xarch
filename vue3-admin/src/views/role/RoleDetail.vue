<template>
  <div class="role-detail">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>Role Details</span>
          <div>
            <el-button @click="handleBack">Back</el-button>
            <el-button type="primary" @click="handleEdit">Edit</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border v-loading="loading">
        <el-descriptions-item label="Role ID">{{ roleInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="Role Name">{{ roleInfo.roleName }}</el-descriptions-item>
        <el-descriptions-item label="Role Code">{{ roleInfo.roleKey }}</el-descriptions-item>
        <el-descriptions-item label="Type">
          <el-tag :type="roleInfo.roleType === 1 ? 'danger' : 'success'">
            {{ roleInfo.roleType === 1 ? 'System' : 'Business' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Status">
          <el-tag :type="roleInfo.status === 1 ? 'success' : 'danger'">
            {{ roleInfo.status === 1 ? 'Active' : 'Disabled' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Sort">{{ roleInfo.roleSort }}</el-descriptions-item>
        <el-descriptions-item label="Created Time">{{ roleInfo.createTime }}</el-descriptions-item>
        <el-descriptions-item label="Description" :span="2">{{ roleInfo.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <h4>Assigned Permissions</h4>
      <div class="permission-tree" v-if="menuTree.length > 0">
        <el-tree
          :data="menuTree"
          :props="{ label: 'menuName', children: 'children' }"
          node-key="menuId"
          default-expand-all
        >
          <template #default="{ node, data }">
            <span class="menu-node">
              <span>{{ data.menuName }}</span>
              <el-tag v-if="data.menuType === 'C'" size="small" type="warning">Menu</el-tag>
              <el-tag v-if="data.menuType === 'F'" size="small" type="info">Button</el-tag>
            </span>
          </template>
        </el-tree>
      </div>
      <el-empty v-else description="No permissions assigned" :image-size="50" />
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog v-model="editDialogVisible" title="Edit Role" width="600px">
      <el-form :model="formData" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="Role Name" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="Please enter role name" />
        </el-form-item>
        <el-form-item label="Role Code" prop="roleKey">
          <el-input v-model="formData.roleKey" disabled />
        </el-form-item>
        <el-form-item label="Type" prop="roleType">
          <el-radio-group v-model="formData.roleType">
            <el-radio :label="1">System</el-radio>
            <el-radio :label="2">Business</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Sort" prop="roleSort">
          <el-input-number v-model="formData.roleSort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">Active</el-radio>
            <el-radio :label="0">Disabled</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="formData.remark" type="textarea" placeholder="Please enter description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">Submit</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { roleApi, type Role } from '@/api/role'
import { menuApi } from '@/api/menu'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const editDialogVisible = ref(false)
const formRef = ref()

const roleInfo = reactive<any>({
  id: undefined,
  roleName: '',
  roleKey: '',
  roleType: 2,
  roleSort: 0,
  status: 1,
  remark: '',
  createTime: '',
  menuIds: []
})

const formData = reactive<any>({
  roleName: '',
  roleKey: '',
  roleType: 2,
  roleSort: 0,
  status: 1,
  remark: ''
})

const menuTree = ref<any[]>([])

const formRules = {
  roleName: [{ required: true, message: 'Role name is required', trigger: 'blur' }],
  roleKey: [{ required: true, message: 'Role code is required', trigger: 'blur' }]
}

const loadRoleDetail = async () => {
  const id = route.params.id as string
  if (!id) {
    ElMessage.error('Invalid role ID')
    return
  }

  loading.value = true
  try {
    const result = await roleApi.detail(Number(id))
    if (result.code === '0000') {
      Object.assign(roleInfo, result.data)
    }
  } catch {
    ElMessage.error('Failed to load role details')
  } finally {
    loading.value = false
  }
}

const loadMenuTree = async () => {
  try {
    const result = await menuApi.treeselect()
    if (result.code === '0000') {
      menuTree.value = result.data || []
    }
  } catch {
    // ignore
  }
}

const handleBack = () => {
  router.back()
}

const handleEdit = () => {
  Object.assign(formData, {
    roleName: roleInfo.roleName,
    roleKey: roleInfo.roleKey,
    roleType: roleInfo.roleType,
    roleSort: roleInfo.roleSort,
    status: roleInfo.status,
    remark: roleInfo.remark
  })
  editDialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true
    const result = await roleApi.update(roleInfo.id!, formData)
    if (result.code === '0000') {
      ElMessage.success('Updated successfully')
      editDialogVisible.value = false
      loadRoleDetail()
    } else {
      ElMessage.error(result.msg || 'Update failed')
    }
  } catch {
    // validation failed
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadRoleDetail()
  loadMenuTree()
})
</script>

<style scoped>
.role-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.menu-node {
  display: flex;
  align-items: center;
  gap: 8px;
}

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
}
</style>
