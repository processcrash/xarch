import { request } from '@/utils/http'

export interface SysUserOnline {
  tokenId: string
  userName: string
  ipaddr: string
  loginLocation: string
  browser: string
  os: string
  loginTime: number
}

export interface OnlineQuery {
  userName?: string
  ipaddr?: string
  pageNum: number
  pageSize: number
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export const onlineApi = {
  list(params: OnlineQuery) {
    return request.get<PageResult<SysUserOnline>>('/monitor/online/list', { params })
  },
  forceLogout(tokenId: string) {
    return request.delete(`/monitor/online/${tokenId}`)
  }
}