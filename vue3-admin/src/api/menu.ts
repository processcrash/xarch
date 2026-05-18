import { http } from '@/utils/http'

export interface Menu {
  id?: number
  parentId?: number
  menuName: string
  menuCode?: string
  menuType?: number
  path?: string
  icon?: string
  sortOrder?: number
  status?: number
  children?: Menu[]
}

export interface PageResult<T> {
  list: T[]
  total: number
}

export const menuApi = {
  page: (params: { menuName?: string; pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<Menu>>('/menus', params),
  tree: () => http.get<Menu[]>('/menus/tree'),
  detail: (id: number) => http.get<Menu>(`/menus/${id}`),
  create: (data: Menu) => http.post('/menus', data),
  update: (id: number, data: Menu) => http.put(`/menus/${id}`, data),
  delete: (id: number) => http.delete(`/menus/${id}`),
  options: () => http.get<Menu[]>('/menus/options'),
}