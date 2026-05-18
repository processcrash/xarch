import { http } from '@/utils/http'

export interface Role {
  id?: number
  roleName: string
  roleCode: string
  roleType?: number
  description?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
}

export const roleApi = {
  page: (params: { roleName?: string; roleCode?: string; pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<Role>>('/roles', params),
  detail: (id: number) => http.get<Role>(`/roles/${id}`),
  create: (data: Role) => http.post('/roles', data),
  update: (id: number, data: Role) => http.put(`/roles/${id}`, data),
  delete: (id: number) => http.delete(`/roles/${id}`),
  options: () => http.get<Role[]>('/roles/options'),
}