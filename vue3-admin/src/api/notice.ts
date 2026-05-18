import { request } from '@/utils/http'

export interface SysNotice {
  noticeId?: number
  noticeTitle: string
  noticeType: string
  noticeContent?: string
  status: string
  createTime?: string
}

export interface NoticeQuery {
  noticeTitle?: string
  noticeType?: string
  pageNum: number
  pageSize: number
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export const noticeApi = {
  list(params: NoticeQuery) {
    return request.get<PageResult<SysNotice>>('/system/notice/list', { params })
  },
  get(id: number) {
    return request.get<SysNotice>(`/system/notice/${id}`)
  },
  create(data: SysNotice) {
    return request.post('/system/notice', data)
  },
  update(id: number, data: SysNotice) {
    return request.put(`/system/notice/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/system/notice/${id}`)
  }
}