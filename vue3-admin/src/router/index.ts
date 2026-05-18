import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/users',
    name: 'UserManagement',
    component: () => import('@/views/user/UserList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/users/:id',
    name: 'UserDetail',
    component: () => import('@/views/user/UserDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/roles',
    name: 'RoleManagement',
    component: () => import('@/views/role/RoleList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/roles/:id',
    name: 'RoleDetail',
    component: () => import('@/views/role/RoleDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/menus',
    name: 'MenuManagement',
    component: () => import('@/views/menu/MenuList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/depts',
    name: 'DeptManagement',
    component: () => import('@/views/dept/DeptList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/dicts',
    name: 'DictManagement',
    component: () => import('@/views/dict/DictList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/configs',
    name: 'ConfigManagement',
    component: () => import('@/views/config/ConfigList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/logs',
    name: 'LogManagement',
    component: () => import('@/views/log/LogList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/posts',
    name: 'PostManagement',
    component: () => import('@/views/system/post/PostList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/notices',
    name: 'NoticeManagement',
    component: () => import('@/views/system/notice/NoticeList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/online',
    name: 'OnlineUsers',
    component: () => import('@/views/monitor/online/OnlineList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/jobs',
    name: 'JobManagement',
    component: () => import('@/views/monitor/job/JobList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/jobLogs',
    name: 'JobLogManagement',
    component: () => import('@/views/monitor/job/JobLogList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/files',
    name: 'FileManagement',
    component: () => import('@/views/file/FileList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/ai/servers',
    name: 'AIServerManagement',
    component: () => import('@/views/ai/ServerList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router: Router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth !== false && !authStore.isAuthenticated()) {
    ElMessage.warning('Please login first')
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated()) {
    next('/home')
  } else {
    next()
  }
})

export default router