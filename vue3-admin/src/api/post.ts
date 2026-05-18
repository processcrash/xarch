import { request } from '@/utils/http'

export interface SysPost {
  postId?: number
  postCode: string
  postName: string
  postSort: number
  status: string
  remark?: string
  createTime?: string
}

export interface PostQuery {
  postCode?: string
  postName?: string
  status?: string
  pageNum: number
  pageSize: number
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export const postApi = {
  list(params: PostQuery) {
    return request.get<PageResult<SysPost>>('/system/post/list', { params })
  },
  all() {
    return request.get<SysPost[]>('/system/post/all')
  },
  get(id: number) {
    return request.get<SysPost>(`/system/post/${id}`)
  },
  create(data: SysPost) {
    return request.post('/system/post', data)
  },
  update(id: number, data: SysPost) {
    return request.put(`/system/post/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/system/post/${id}`)
  }
}