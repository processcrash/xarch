<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>xarch Backend Framework</h2>
          <div class="header-actions">
            <span class="username">{{ authStore.userInfo?.username || username }}</span>
            <LangSwitch />
            <el-button type="primary" @click="handleLogout">{{ t('auth.logout') }}</el-button>
          </div>
        </div>
      </el-header>
      <el-container>
        <el-aside width="220px">
          <el-menu :default-active="activeMenu" class="el-menu-vertical" @select="handleMenuSelect">
            <el-menu-item index="1" @click="router.push('/users')">
              <el-icon><User /></el-icon>
              <span>{{ t('nav.userManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="2" @click="router.push('/roles')">
              <el-icon><Role /></el-icon>
              <span>{{ t('nav.roleManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="3" @click="router.push('/menus')">
              <el-icon><Menu /></el-icon>
              <span>{{ t('nav.menuManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="4" @click="router.push('/depts')">
              <el-icon><Office /></el-icon>
              <span>{{ t('nav.deptManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="5" @click="router.push('/dicts')">
              <el-icon><Document /></el-icon>
              <span>{{ t('nav.dictManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="6" @click="router.push('/configs')">
              <el-icon><Setting /></el-icon>
              <span>{{ t('nav.configManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="7" @click="router.push('/logs')">
              <el-icon><List /></el-icon>
              <span>{{ t('nav.logManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="8" @click="router.push('/clients')">
              <el-icon><UserFilled /></el-icon>
              <span>{{ t('nav.clientManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="9" @click="router.push('/messages')">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ t('nav.messageCenter') }}</span>
              <el-badge v-if="unreadCount > 0" :value="unreadCount" class="menu-badge" />
            </el-menu-item>
            <el-menu-item index="10" @click="router.push('/monitor/server')">
              <el-icon><Monitor /></el-icon>
              <span>{{ t('nav.monitorServer') }}</span>
            </el-menu-item>
            <el-menu-item index="11" @click="router.push('/monitor/cache')">
              <el-icon><Coin /></el-icon>
              <span>{{ t('nav.monitorCache') }}</span>
            </el-menu-item>
            <el-menu-item index="12" @click="router.push('/resources')">
              <el-icon><Folder /></el-icon>
              <span>{{ t('nav.resourceManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="13" @click="router.push('/tempfiles')">
              <el-icon><Document /></el-icon>
              <span>{{ t('nav.tempFileManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="14" @click="router.push('/audit')">
              <el-icon><List /></el-icon>
              <span>{{ t('nav.commandAudit') }}</span>
            </el-menu-item>
            <el-menu-item index="15" @click="router.push('/excel/users')">
              <el-icon><Download /></el-icon>
              <span>{{ t('nav.excelImportExport') }}</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <div class="content">
            <h3>{{ t('home.welcome', { name: authStore.userInfo?.nickname || username }) }}</h3>
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
                <span>{{ t('home.quickStart') }}</span>
              </template>
              <div class="card-content">
                <p>{{ t('home.desc') }}</p>
                <ul>
                  <li>{{ t('home.features.springBoot') }}</li>
                  <li>{{ t('home.features.database') }}</li>
                  <li>{{ t('home.features.redis') }}</li>
                  <li>{{ t('home.features.frontend') }}</li>
                  <li>{{ t('home.features.auth') }}</li>
                  <li>{{ t('home.features.orm') }}</li>
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
import { useI18n } from '@/composables/useI18n'
import LangSwitch from '@/components/LangSwitch.vue'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()
const activeMenu = ref('1')
const unreadCount = ref(0)

const username = computed(() => localStorage.getItem('username') || '')

const stats = ref([
  { title: t('home.stats.users'), value: '10', icon: User },
  { title: t('home.stats.roles'), value: '5', icon: Role },
  { title: t('home.stats.menus'), value: '20', icon: Menu },
  { title: t('home.stats.depts'), value: '8', icon: Office },
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
  ElMessage.success(t('auth.loggedOut'))
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
