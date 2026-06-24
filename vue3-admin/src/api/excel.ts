import { http } from '@/utils/http'
import axios from 'axios'

export interface ImportResult {
  data: number
}

export const excelApi = {
  exportUsers: async () => {
    const token = localStorage.getItem('token')
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
    const response = await axios.get(`${baseURL}/excel/export/users`, {
      responseType: 'blob',
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    return response.data as Blob
  },
  importUsers: (data: FormData) =>
    http.post<ImportResult>('/excel/import/users', data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
}
