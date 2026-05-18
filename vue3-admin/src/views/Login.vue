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
          <el-input v-model="loginForm.password" type="password" placeholder="Please enter password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">Login</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('Please enter username and password')
    return
  }
  localStorage.setItem('token', 'mock-token-' + loginForm.username)
  ElMessage.success('Login successful')
  router.push('/home')
}