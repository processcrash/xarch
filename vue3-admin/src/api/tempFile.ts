import { http } from '@/utils/http'
import axios from 'axios'

export interface TempFile {
  id?: number
  fileName: string
  filePath: string
  fileSize: number
  fileType?: string
  createTime?: string
}

export interface UploadTempResult {
  fileId: number
  fileName: string
  filePath: string
  fileSize: number
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

export const tempFileApi = {
  page: (params: { fileName?: string; pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<TempFile>>('/temp-files', params),
  detail: (id: number) => http.get<TempFile>(`/temp-files/${id}`),
  upload: (data: FormData) =>
    http.post<UploadTempResult>('/temp-files/upload', data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  create: (data: TempFile) => http.post('/temp-files', data),
  update: (id: number, data: TempFile) => http.put(`/temp-files/${id}`, data),
  delete: (data: SelectIdsDTO) => http.delete('/temp-files', { data }),
  download: async (id: number) => {
    const token = localStorage.getItem('token')
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
    const response = await axios.get(`${baseURL}/temp-files/download/${id}`, {
      responseType: 'blob',
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    return response.data as Blob
  }
}
