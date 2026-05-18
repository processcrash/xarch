import { http } from '@/utils/http'

export interface Dict {
  id?: number
  dictName: string
  dictCode: string
  description?: string
  status?: number
}

export interface DictData {
  id?: number
  dictId: number
  dictLabel: string
  dictValue: string
  sortOrder?: number
  status?: number
}

export interface PageResult<T> {
  list: T[]
  total: number
}

export const dictApi = {
  page: (params: { dictName?: string; pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<Dict>>('/dicts', params),
  detail: (id: number) => http.get<Dict>(`/dicts/${id}`),
  create: (data: Dict) => http.post('/dicts', data),
  update: (id: number, data: Dict) => http.put(`/dicts/${id}`, data),
  delete: (id: number) => http.delete(`/dicts/${id}`),
  getDataByCode: (dictCode: string) => http.get<DictData[]>(`/dicts/data/${dictCode}`),
  getDataById: (dictId: number) => http.get<DictData[]>(`/dicts/data/id/${dictId}`),
  createData: (data: DictData) => http.post('/dicts/data', data),
  updateData: (id: number, data: DictData) => http.put(`/dicts/data/${id}`, data),
  deleteData: (id: number) => http.delete(`/dicts/data/${id}`),
}