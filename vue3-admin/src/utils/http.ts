import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

export interface ApiResponse<T = any> {
  code: string
  message: string
  data: T
  timestamp: number
}

export interface RequestConfig extends AxiosRequestConfig {
  loading?: boolean
}

class HttpClient {
  private instance: AxiosInstance

  constructor() {
    this.instance = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    this.instance.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResponse>) => {
        const { code, message } = response.data
        if (code !== '200') {
          ElMessage.error(message || 'Request failed')
          if (code === '401') {
            router.push('/login')
          }
          return Promise.reject(new Error(message))
        }
        return response
      },
      (error) => {
        ElMessage.error(error.message || 'Network error')
        return Promise.reject(error)
      }
    )
  }

  get<T = any>(url: string, config?: RequestConfig): Promise<T> {
    return this.instance.get<ApiResponse<T>>(url, config).then((res) => res.data.data)
  }

  post<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return this.instance.post<ApiResponse<T>>(url, data, config).then((res) => res.data.data)
  }

  put<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return this.instance.put<ApiResponse<T>>(url, data, config).then((res) => res.data.data)
  }

  delete<T = any>(url: string, config?: RequestConfig): Promise<T> {
    return this.instance.delete<ApiResponse<T>>(url, config).then((res) => res.data.data)
  }
}

export const http = new HttpClient()

export default http