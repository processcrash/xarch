import { http } from '@/utils/http'

export interface LoginLog {
  id?: number
  username?: string
  ip?: string
  location?: string
  loginTime?: string
  loginType?: number
  status?: number
  message?: string
}

export interface OpLog {
  id?: number
  username?: string
  operation?: string
  type?: string
  method?: string
  ip?: string
  location?: string
  params?: string
  result?: string
  status?: number
  costTime?: number
  createTime?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
}

export const logApi = {
  loginLogPage: (params: { username?: string; pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<LoginLog>>('/logs/login', params),
  opLogPage: (params: { username?: string; pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<OpLog>>('/logs/op', params),
}