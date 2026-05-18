import { http } from '@/utils/http'

export interface LoginRequest {
  username: string
  password: string
  captcha?: string
  captchaKey?: string
}

export interface LoginResponse {
  token: string
  expireTime: number
  username: string
  roles: string
  nickname: string
}

export interface CaptchaResponse {
  captchaBase64: string
  captchaKey: string
}

export interface UserInfo {
  userId: number
  username: string
  nickname: string
  userType: number
  roleIds: string
  roleNames: string
  permissions: string
  deptId?: number
  deptName?: string
}

export const authApi = {
  login: (data: LoginRequest) => http.post<LoginResponse>('/auth/login', data),
  logout: () => http.post('/auth/logout', {}),
  getCaptcha: () => http.get<CaptchaResponse>('/auth/captcha'),
  getCurrentUser: () => http.get<UserInfo>('/auth/me'),
}