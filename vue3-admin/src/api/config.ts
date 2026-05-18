import { http } from '@/utils/http'

export interface Config {
  id?: number
  configKey: string
  configValue: string
  configType?: string
  description?: string
  status?: number
}

export interface PageResult<T> {
  list: T[]
  total: number
}

export const configApi = {
  page: (params: { configKey?: string; pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<Config>>('/configs', params),
  detail: (id: number) => http.get<Config>(`/configs/${id}`),
  getValue: (configKey: string) => http.get<string>(`/configs/value/${configKey}`),
  create: (data: Config) => http.post('/configs', data),
  update: (id: number, data: Config) => http.put(`/configs/${id}`, data),
  delete: (id: number) => http.delete(`/configs/${id}`),
}