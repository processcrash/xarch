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
        <el-aside width="220px">
          <el-menu :default-active="activeMenu" class="el-menu-vertical" @select="handleMenuSelect">
            <el-menu-item index="1" @click="router.push('/users')">
              <el-icon><User /></el-icon>
              <span>User Management</span>
            </el-menu-item>
            <el-menu-item index="2" @click="router.push('/roles')">
              <el-icon><Role /></el-icon>
              <span>Role Management</span>
            </el-menu-item>
            <el-menu-item index="3" @click="router.push('/menus')">
              <el-icon><Menu /></el-icon>
              <span>Menu Management</span>
            </el-menu-item>
            <el-menu-item index="4" @click="router.push('/depts')">
              <el-icon><Office /></el-icon>
              <span>Dept Management</span>
            </el-menu-item>
            <el-menu-item index="5" @click="router.push('/dicts')">
              <el-icon><Document /></el-icon>
              <span>Dictionary</span>
            </el-menu-item>
            <el-menu-item index="6" @click="router.push('/configs')">
              <el-icon><Setting /></el-icon>
              <span>System Config</span>
            </el-menu-item>
            <el-menu-item index="7" @click="router.push('/logs')">
              <el-icon><List /></el-icon>
              <span>Logs</span>
            </el-menu-item>
            <el-menu-item index="8" @click="router.push('/clients')">
              <el-icon><UserFilled /></el-icon>
              <span>Client Management</span>
            </el-menu-item>
            <el-menu-item index="9" @click="router.push('/messages')">
              <el-icon><ChatDotRound /></el-icon>
              <span>Message Center</span>
              <el-badge v-if="unreadCount > 0" :value="unreadCount" class="menu-badge" />
            </el-menu-item>
            <el-menu-item index="10" @click="router.push('/monitor/server')">
              <el-icon><Monitor /></el-icon>
              <span>Server Monitor</span>
            </el-menu-item>
            <el-menu-item index="11" @click="router.push('/monitor/cache')">
              <el-icon><Coin /></el-icon>
              <span>Cache Monitor</span>
            </el-menu-item>
            <el-menu-item index="12" @click="router.push('/resources')">
              <el-icon><Folder /></el-icon>
              <span>Resource Management</span>
            </el-menu-item>
            <el-menu-item index="13" @click="router.push('/tempfiles')">
              <el-icon><Document /></el-icon>
              <span>Temp File</span>
            </el-menu-item>
            <el-menu-item index="14" @click="router.push('/audit')">
              <el-icon><List /></el-icon>
              <span>Command Audit</span>
            </el-menu-item>
            <el-menu-item index="15" @click="router.push('/excel/users')">
              <el-icon><Download /></el-icon>
              <span>Excel Import/Export</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <div class="content">
            <h3>Welcome, {{ authStore.userInfo?.nickname || username }}</h3>
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="6" v-for="item in stats" :key="item.title">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-icon">
                    <el-icon :size="32"><component :is="item.icon" /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-title">{{ item.title }}</div>
                    <div class="stat-value">{{ item.value }}</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            <el-card style="margin-top: 20px">
              <template #header>
                <span>Quick Start</span>
              </template>
              <div class="card-content">
                <p>Enterprise Backend Development Framework</p>
                <ul>
                  <li>Spring Boot 4.0 + JDK 25</li>
                  <li>MySQL / PostgreSQL Support</li>
                  <li>Redis Cache Support</li>
                  <li>Vue 3 + Element Plus Frontend</li>
                  <li>Sa-Token Authentication</li>
                  <li>MyBatis Plus ORM</li>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  User,
  Role,
  Menu,
  Office,
  Document,
  Setting,
  List,
  Monitor,
  Coin,
  Folder,
  Download,
  UserFilled,
  ChatDotRound
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { messageApi } from '@/api/message'

const router = useRouter()
const authStore = useAuthStore()
const activeMenu = ref('1')
const unreadCount = ref(0)

const username = computed(() => localStorage.getItem('username') || '')

const stats = ref([
  { title: 'Users', value: '10', icon: User },
  { title: 'Roles', value: '5', icon: Role },
  { title: 'Menus', value: '20', icon: Menu },
  { title: 'Depts', value: '8', icon: Office },
])

const loadUnreadCount = async () => {
  try {
    const result = await messageApi.count()
    unreadCount.value = result?.unreadCount || 0
  } catch {
    // ignore
  }
}

const handleLogout = async () => {
  await authStore.logout()
  ElMessage.success('Logged out successfully')
  router.push('/login')
}

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
}

onMounted(() => {
  loadUnreadCount()
})
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
  background-color: #f5f5f5;
}

.content {
  max-width: 1200px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background: #ecf5ff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.card-content ul {
  list-style: disc;
  padding-left: 20px;
}

.card-content ul li {
  padding: 8px 0;
  color: #666;
}

.menu-badge {
  margin-left: 8px;
}
</style>