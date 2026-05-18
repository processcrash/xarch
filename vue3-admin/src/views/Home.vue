<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>xarch Backend Framework</h2>
          <div class="header-actions">
            <span class="username">{{ authStore.userInfo?.username || username }}</span>
            <el-button type="primary" @click="handleLogout">Logout</el-button>
          </div>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-menu default-active="1" class="el-menu-vertical" @select="handleMenuSelect">
            <el-menu-item index="1">
              <el-icon><User /></el-icon>
              <span>User Management</span>
            </el-menu-item>
            <el-menu-item index="2">
              <el-icon><Setting /></el-icon>
              <span>System Settings</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <div class="content">
            <h3>Welcome to xarch</h3>
            <el-card class="box-card">
              <template #header>
                <div class="card-header">
                  <span>Quick Start</span>
                </div>
              </template>
              <div class="card-content">
                <p>Enterprise Backend Development Framework</p>
                <ul>
                  <li>Spring Boot 4.0 + JDK 25</li>
                  <li>MySQL / PostgreSQL Support</li>
                  <li>Redis Cache Support</li>
                  <li>Vue 3 Frontend</li>
                </ul>
              </div>
            </el-card>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = computed(() => localStorage.getItem('username') || '')

const handleLogout = async () => {
  await authStore.logout()
  ElMessage.success('Logged out successfully')
  router.push('/login')
}

const handleMenuSelect = (index: string) => {
  if (index === '1') {
    router.push('/users')
  }
}
</script>

<style scoped>
.home {
  height: 100%;
}

.el-container {
  height: 100%;
}

.el-header {
  background-color: #545c64;
  color: #fff;
  line-height: 60px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  color: #fff;
}

.el-aside {
  background-color: #f2f2f2;
}

.el-main {
  padding: 20px;
}

.content {
  max-width: 800px;
}

.card-content ul {
  list-style: none;
  padding: 0;
}

.card-content ul li {
  padding: 8px 0;
  color: #666;
}
</style>