<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>xarch Login</span>
        </div>
      </template>
      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="Username">
          <el-input v-model="loginForm.username" placeholder="Please enter username" />
        </el-form-item>
        <el-form-item label="Password">
          <el-input v-model="loginForm.password" type="password" placeholder="Please enter password" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%" :loading="loading">Login</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('Please enter username and password')
    return
  }

  loading.value = true
  try {
    const success = await authStore.login(loginForm.username, loginForm.password)
    if (success) {
      ElMessage.success('Login successful')
      router.push('/home')
    } else {
      ElMessage.error('Invalid username or password')
    }
  } catch {
    ElMessage.error('Login failed')
  } finally {
    loading.value = false
  }
}
</script>