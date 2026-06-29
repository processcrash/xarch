<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('auth.loginTitle') }}</span>
        </div>
      </template>
      <el-form :model="loginForm" label-width="80px">
        <el-form-item :label="t('auth.username')">
          <el-input
            v-model="loginForm.username"
            :placeholder="t('auth.username')"
          />
        </el-form-item>
        <el-form-item :label="t('auth.password')">
          <el-input
            v-model="loginForm.password"
            type="password"
            :placeholder="t('auth.password')"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%" :loading="loading">
            {{ t('auth.login') }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="lang-row">
        <LangSwitch />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/composables/useI18n'
import LangSwitch from '@/components/LangSwitch.vue'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning(t('auth.pleaseEnterCredentials'))
    return
  }

  loading.value = true
  try {
    const success = await authStore.login(loginForm.username, loginForm.password)
    if (success) {
      ElMessage.success(t('auth.loginSuccess'))
      router.push('/home')
    } else {
      ElMessage.error(t('auth.invalidCredentials'))
    }
  } catch {
    ElMessage.error(t('auth.loginFailed'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.login-card {
  width: 400px;
}

.card-header {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
}

.lang-row {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}
</style>
