import { request } from '@/utils/http'

export interface SysJob {
  jobId?: number
  jobName: string
  jobGroup: string
  invokeTarget: string
  cronExpression: string
  misfirePolicy: string
  concurrent: string
  status: string
  nextValidTime?: string
  remark?: string
  createTime?: string
}

export interface JobQuery {
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

export const jobApi = {
  list(params: JobQuery) {
    return request.get<PageResult<SysJob>>('/monitor/job/list', { params })
  },
  get(id: number) {
    return request.get<SysJob>(`/monitor/job/${id}`)
  },
  create(data: SysJob) {
    return request.post('/monitor/job', data)
  },
  update(id: number, data: SysJob) {
    return request.put(`/monitor/job/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/monitor/job/${id}`)
  },
  changeStatus(id: number, status: string) {
    return request.put('/monitor/job/changeStatus', { jobId: id, status })
  },
  run(id: number) {
    return request.put('/monitor/job/run', { jobId: id })
  }
}