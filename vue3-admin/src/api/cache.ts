import { http } from '@/utils/http'

export interface SysCache {
  cacheName: string
  cacheKey: string
  cacheValue: string
  remark: string
}

export interface CacheInfo {
  caches: SysCache[]
  size: number
}

export const cacheApi = {
  getInfo: () => http.get<CacheInfo>('/monitor/cache'),
  getNames: () => http.get<SysCache[]>('/monitor/cache/getNames'),
  getKeys: (cacheName: string) => http.get<string[]>(`/monitor/cache/getKeys/${cacheName}`),
  getValue: (cacheName: string, cacheKey: string) =>
    http.get<SysCache>(`/monitor/cache/getValue/${cacheName}/${cacheKey}`),
  clearCacheName: (cacheName: string) =>
    http.delete(`/monitor/cache/clearCacheName/${cacheName}`),
  clearCacheKey: (cacheKey: string) =>
    http.delete(`/monitor/cache/clearCacheKey/${cacheKey}`),
  clearCacheAll: () => http.delete('/monitor/cache/clearCacheAll')
}
