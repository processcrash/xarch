import { request } from '@/utils/http'

export interface SysJobLog {
  jobLogId?: number
  jobName: string
  jobGroup: string
  invokeTarget: string
  jobMessage: string
  status: string
  exceptionInfo?: string
  startTime: string
  endTime: string
}

export interface JobLogQuery {
  jobName?: string
  jobGroup?: string
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

export const jobLogApi = {
  list(params: JobLogQuery) {
    return request.get<PageResult<SysJobLog>>('/monitor/jobLog/list', { params })
  },
  get(id: number) {
    return request.get<SysJobLog>(`/monitor/jobLog/${id}`)
  },
  delete(id: number) {
    return request.delete(`/monitor/jobLog/${id}`)
  },
  clean() {
    return request.delete('/monitor/jobLog/clean')
  }
}