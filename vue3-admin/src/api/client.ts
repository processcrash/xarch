import { http } from '@/utils/http'

export interface Client {
  id?: number
  clientId: string
  clientKey: string
  clientSecret?: string
  clientName: string
  grantTypes?: string
  scope?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface SelectIdsDTO {
  ids: number[]
}

export const clientApi = {
  page: (params: { clientName?: string; clientId?: string; pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<Client>>('/clients', params),
  detail: (id: number) => http.get<Client>(`/clients/${id}`),
  create: (data: Client) => http.post('/clients', data),
  update: (id: number, data: Client) => http.put(`/clients/${id}`, data),
  delete: (data: SelectIdsDTO) => http.delete('/clients', { data }),
  options: () => http.get<Client[]>('/clients/options')
}
