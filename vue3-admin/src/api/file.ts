/**
 * File Management API
 */
import request from '@/api/index'

export interface Resource {
  id?: number
  resourceName: string
  objectKey: string
  accessUrl: string
  sceneCode: string
  fileSize: number
  fileType: string
  storageType: string
  bizKey: string
  createUserId?: number
  createUserName?: string
  createTime?: string
}

export interface StorageConfig {
  id?: number
  storageType: string
  configName: string
  isDefault: number
  endpoint: string
  accessKey: string
  secretKey: string
  bucketName: string
  region: string
  basePath: string
  domain: string
  status: number
  description: string
}

export interface StorageStats {
  localCount: number
  minioCount: number
  ossCount: number
  totalSize: number
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

/**
 * Page query files
 */
export function filePage(params: {
  sceneCode?: string
  storageType?: string
  keyword?: string
  pageNum?: number
  pageSize?: number
}) {
  return request.get<any, { code: string; msg: string; data: PageResult<Resource> }>('/file/page', { params })
}

/**
 * Get file details
 */
export function fileDetail(id: number) {
  return request.get<any, { code: string; msg: string; data: Resource }>(`/file/${id}`)
}

/**
 * Upload file
 */
export function fileUpload(data: FormData) {
  return request.post<any, { code: string; msg: string; data: Resource }>('/file/upload', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * Download file
 */
export function fileDownload(id: number) {
  return request.get<any, any>(`/file/download/${id}`, { responseType: 'blob' })
}

/**
 * Delete file
 */
export function fileDelete(id: number) {
  return request.delete<any, { code: string; msg: string }>(`/file/${id}`)
}

/**
 * Get storage statistics
 */
export function fileStats() {
  return request.get<any, { code: string; msg: string; data: StorageStats }>('/file/stats')
}

/**
 * List storage configurations
 */
export function storageConfigList() {
  return request.get<any, { code: string; msg: string; data: StorageConfig[] }>('/file/storage/configs')
}

/**
 * Get storage configuration
 */
export function storageConfigGet(id: number) {
  return request.get<any, { code: string; msg: string; data: StorageConfig }>(`/file/storage/config/${id}`)
}

/**
 * Create storage configuration
 */
export function storageConfigCreate(data: StorageConfig) {
  return request.post<any, { code: string; msg: string }>('/file/storage/config', data)
}

/**
 * Update storage configuration
 */
export function storageConfigUpdate(data: StorageConfig) {
  return request.put<any, { code: string; msg: string }>('/file/storage/config', data)
}

/**
 * Delete storage configuration
 */
export function storageConfigDelete(id: number) {
  return request.delete<any, { code: string; msg: string }>(`/file/storage/config/${id}`)
}

/**
 * Test storage connection
 */
export function storageConfigTest(id: number) {
  return request.post<any, { code: string; msg: string; data: boolean }>(`/file/storage/config/${id}/test`)
}

/**
 * List available storage types
 */
export function storageTypes() {
  return request.get<any, { code: string; msg: string; data: Array<{ code: string; name: string; description: string }> }>('/file/storage/types')
}