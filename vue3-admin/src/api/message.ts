import { http } from '@/utils/http'

export interface Message {
  id?: number
  msgType: string
  title: string
  content: string
  status?: number
  sender?: string
  receiver?: string
  createTime?: string
}

export interface UnreadCount {
  unreadCount: number
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export const messageApi = {
  page: (params: { msgType?: string; pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<Message>>('/messages', params),
  detail: (id: number) => http.get<Message>(`/messages/${id}`),
  create: (data: Message) => http.post('/messages', data),
  update: (id: number, data: Message) => http.put(`/messages/${id}`, data),
  delete: (id: number) => http.delete(`/messages/${id}`),
  count: () => http.get<UnreadCount>('/messages/count'),
  listTodo: () => http.get<Message[]>('/messages/list/todo'),
  listMsg: () => http.get<Message[]>('/messages/list/msg')
}
