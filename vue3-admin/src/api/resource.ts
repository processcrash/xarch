import { http } from '@/utils/http'
import axios from 'axios'

export interface Resource {
  id?: number
  resourceName: string
  objectKey: string
  accessUrl: string
  sceneCode: string
  fileSize: number
  fileType: string
  storageType: string
  bizKey?: string
  createUserId?: number
  createUserName?: string
  createTime?: string
}

export interface UploadResult {
  objectKey: string
  accessUrl: string
  resourceId: number
  fileName: string
  fileSize: number
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export const resourceApi = {
  page: (params: {
    sceneCode?: string
    storageType?: string
    keyword?: string
    pageNum?: number
    pageSize?: number
  }) => http.get<PageResult<Resource>>('/resources', params),
  detail: (id: number) => http.get<Resource>(`/resources/${id}`),
  upload: (data: FormData) =>
    http.post<UploadResult>('/resources/upload', data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  batchUpload: (data: FormData) =>
    http.post<UploadResult[]>('/resources/batchUpload', data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  delete: (id: number) => http.delete(`/resources/${id}`),
  options: () => http.get<Resource[]>('/resources/options'),
  download: async (id: number) => {
    const token = localStorage.getItem('token')
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
    const response = await axios.get(`${baseURL}/resources/download/${id}`, {
      responseType: 'blob',
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    return response.data as Blob
  }
}
