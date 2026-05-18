import { http } from '@/utils/http'

export interface User {
  id?: number
  username: string
  password?: string
  nickname?: string
  email?: string
  mobile?: string
  status?: number
  deptId?: number
  userType?: number
  roleIds?: string
  createTime?: string
  updateTime?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
}

export const userApi = {
  page: (params: { username?: string; status?: string; pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<User>>('/users', params),
  detail: (id: number) => http.get<User>(`/users/${id}`),
  create: (data: User) => http.post('/users', data),
  update: (id: number, data: User) => http.put(`/users/${id}`, data),
  delete: (id: number) => http.delete(`/users/${id}`),
  options: () => http.get<User[]>('/users/options'),
}