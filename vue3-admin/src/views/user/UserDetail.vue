<template>
  <div class="user-detail">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>User Details</span>
          <div>
            <el-button @click="handleBack">Back</el-button>
            <el-button type="primary" @click="handleEdit">Edit</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border v-loading="loading">
        <el-descriptions-item label="User ID">{{ userInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="Username">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="Nickname">{{ userInfo.nickname }}</el-descriptions-item>
        <el-descriptions-item label="Email">{{ userInfo.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Mobile">{{ userInfo.mobile || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Status">
          <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
            {{ userInfo.status === 1 ? 'Active' : 'Disabled' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="User Type">{{ getUserTypeLabel(userInfo.userType) }}</el-descriptions-item>
        <el-descriptions-item label="Department">{{ userInfo.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Created Time">{{ userInfo.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Last Login">{{ userInfo.loginDate || 'Never' }}</el-descriptions-item>
        <el-descriptions-item label="Last Login IP">{{ userInfo.loginIp || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <h4>Assigned Roles</h4>
      <el-tag v-for="role in userInfo.roles" :key="role.roleId" size="small" style="margin-right: 8px">
        {{ role.roleName }}
      </el-tag>
      <el-empty v-if="!userInfo.roles || userInfo.roles.length === 0" description="No roles assigned" :image-size="50" />
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog v-model="editDialogVisible" title="Edit User" width="600px">
      <el-form :model="formData" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="Username" prop="username">
          <el-input v-model="formData.username" disabled />
        </el-form-item>
        <el-form-item label="Nickname" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="Please enter nickname" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="formData.email" placeholder="Please enter email" />
        </el-form-item>
        <el-form-item label="Mobile" prop="mobile">
          <el-input v-model="formData.mobile" placeholder="Please enter mobile" />
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">Active</el-radio>
            <el-radio :label="0">Disabled</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Department">
          <el-tree-select
            v-model="formData.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
            placeholder="Select department"
            clearable
            check-strictly
          />
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
import { userApi, type User } from '@/api/user'
import { deptApi } from '@/api/dept'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const editDialogVisible = ref(false)
const formRef = ref()

const userInfo = reactive<any>({
  id: undefined,
  username: '',
  nickname: '',
  email: '',
  mobile: '',
  status: 1,
  userType: '',
  deptId: undefined,
  deptName: '',
  createTime: '',
  loginDate: '',
  loginIp: '',
  roles: []
})

const formData = reactive<any>({
  username: '',
  nickname: '',
  email: '',
  mobile: '',
  status: 1,
  deptId: undefined
})

const deptTree = ref<any[]>([])

const formRules = {
  username: [{ required: true, message: 'Username is required', trigger: 'blur' }],
  email: [
    { required: false },
    { type: 'email', message: 'Please enter valid email', trigger: 'blur' }
  ],
  mobile: [
    { required: false },
    { pattern: /^1[3-9]\d{9}$/, message: 'Please enter valid mobile', trigger: 'blur' }
  ]
}

const loadUserDetail = async () => {
  const id = route.params.id as string
  if (!id) {
    ElMessage.error('Invalid user ID')
    return
  }

  loading.value = true
  try {
    const result = await userApi.detail(Number(id))
    if (result.code === '0000') {
      Object.assign(userInfo, result.data)
    }
  } catch {
    ElMessage.error('Failed to load user details')
  } finally {
    loading.value = false
  }
}

const loadDeptTree = async () => {
  try {
    const result = await deptApi.list()
    if (result.code === '0000') {
      deptTree.value = result.data || []
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
    username: userInfo.username,
    nickname: userInfo.nickname,
    email: userInfo.email,
    mobile: userInfo.mobile,
    status: userInfo.status,
    deptId: userInfo.deptId
  })
  editDialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true
    const result = await userApi.update(userInfo.id!, formData)
    if (result.code === '0000') {
      ElMessage.success('Updated successfully')
      editDialogVisible.value = false
      loadUserDetail()
    } else {
      ElMessage.error(result.msg || 'Update failed')
    }
  } catch {
    // validation failed
  } finally {
    submitting.value = false
  }
}

const getUserTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    '1': 'System User',
    '2': 'Operation User',
    '3': 'Common User'
  }
  return map[type] || type || '-'
}

onMounted(() => {
  loadUserDetail()
  loadDeptTree()
})
</script>

<style scoped>
.user-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
