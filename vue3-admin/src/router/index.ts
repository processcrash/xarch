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
    path: '/roles',
    name: 'RoleManagement',
    component: () => import('@/views/role/RoleList.vue'),
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