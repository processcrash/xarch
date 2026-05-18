import { http } from '@/utils/http'

export interface Dept {
  id?: number
  parentId?: number
  deptName: string
  deptCode?: string
  sortOrder?: number
  leader?: string
  phone?: string
  status?: number
  children?: Dept[]
}

export interface PageResult<T> {
  list: T[]
  total: number
}

export const deptApi = {
  page: (params: { deptName?: string; pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<Dept>>('/depts', params),
  tree: () => http.get<Dept[]>('/depts/tree'),
  detail: (id: number) => http.get<Dept>(`/depts/${id}`),
  create: (data: Dept) => http.post('/depts', data),
  update: (id: number, data: Dept) => http.put(`/depts/${id}`, data),
  delete: (id: number) => http.delete(`/depts/${id}`),
  options: () => http.get<Dept[]>('/depts/options'),
}