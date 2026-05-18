import { http } from '@/utils/http'
import type { ApiResponse } from '@/utils/http'

export interface User {
  id?: number
  username: string
  password?: string
  email?: string
  mobile?: string
  status?: number
  createTime?: number
  updateTime?: number
}

export const userApi = {
  list: () => http.get<User[]>('/users'),

  getById: (id: number) => http.get<User>(`/users/${id}`),

  getByUsername: (username: string) => http.get<User>(`/users/username/${username}`),

  create: (data: User) => http.post<User>('/users', data),

  update: (id: number, data: User) => http.put<User>(`/users/${id}`, data),

  delete: (id: number) => http.delete<boolean>(`/users/${id}`)
}