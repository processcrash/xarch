import { defineStore } from 'pinia'
import { ref } from 'vue'
import { http } from '@/utils/http'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  expireTime: number
  username: string
  roles: string
}

export interface UserInfo {
  username: string
  roles: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const login = async (username: string, password: string): Promise<boolean> => {
    try {
      const response = await http.post<LoginResponse>('/auth/login', { username, password })
      token.value = response.token
      userInfo.value = {
        username: response.username,
        roles: response.roles
      }
      localStorage.setItem('token', response.token)
      localStorage.setItem('username', response.username)
      return true
    } catch {
      return false
    }
  }

  const logout = async (): Promise<void> => {
    try {
      await http.post('/auth/logout', {})
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    }
  }

  const getCurrentUser = async (): Promise<UserInfo | null> => {
    if (!token.value) return null
    try {
      const response = await http.get<UserInfo>('/auth/me')
      userInfo.value = response
      return response
    } catch {
      return null
    }
  }

  const isAuthenticated = (): boolean => {
    return !!token.value
  }

  return {
    token,
    userInfo,
    login,
    logout,
    getCurrentUser,
    isAuthenticated
  }
})