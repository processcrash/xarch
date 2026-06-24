import { http } from '@/utils/http'

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH'
export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'EXECUTED'

export interface CommandAudit {
  id?: number
  serverId?: number
  serverName?: string
  userId?: number
  userName?: string
  command: string
  riskLevel?: RiskLevel
  approvalStatus?: ApprovalStatus
  status?: number
  result?: string
  createTime?: string
}

export interface ComplianceStats {
  total: number
  approved: number
  rejected: number
  pending: number
  highRiskCount: number
  byRiskLevel: Record<string, number>
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface ApproveDTO {
  comment: string
}

export interface RejectDTO {
  reason: string
}

export const auditApi = {
  page: (params: {
    serverId?: number
    userId?: number
    riskLevel?: string
    approvalStatus?: string
    startTime?: string
    endTime?: string
    pageNum?: number
    pageSize?: number
  }) => http.get<PageResult<CommandAudit>>('/ai/audit/page', params),
  detail: (id: number) => http.get<CommandAudit>(`/ai/audit/${id}`),
  pending: (params: { pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<CommandAudit>>('/ai/audit/pending', params),
  approve: (id: number, data: ApproveDTO) =>
    http.post(`/ai/audit/${id}/approve`, data),
  reject: (id: number, data: RejectDTO) =>
    http.post(`/ai/audit/${id}/reject`, data),
  stats: (params: { start?: string; end?: string }) =>
    http.get<ComplianceStats>('/ai/audit/stats', params),
  userHistory: (params: { pageNum?: number; pageSize?: number }) =>
    http.get<PageResult<CommandAudit>>('/ai/audit/user/history', params)
}
