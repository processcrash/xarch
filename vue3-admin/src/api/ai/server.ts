import request from '@/api/request'

export interface Server {
  id?: number
  name: string
  host: string
  port?: number
  username: string
  authType?: string
  password?: string
  privateKey?: string
  passphrase?: string
  description?: string
  serverGroup?: string
  osType?: string
  tags?: string
  status?: number
  lastConnectedTime?: string
  lastError?: string
  createTime?: string
  updateTime?: string
}

export interface CommandHistory {
  id?: number
  serverId: number
  serverName?: string
  userId?: number
  userName?: string
  command: string
  aiGeneratedCommand?: string
  aiPrompt?: string
  output?: string
  sessionId?: string
  workingDir?: string
  userIp?: string
  exitCode?: number
  status?: number
  duration?: number
  createTime?: string
}

export interface CommandTemplate {
  id: string
  name: string
  command: string
  category: string
}

export interface AiCommandResult {
  command: string
  category: string
  confidence: number
  safetyLevel: string
}

export interface AiValidationResult {
  isSafe: boolean
  message: string
  safetyLevel: string
}

export interface CommandRequest {
  serverId: number
  command: string
  sessionId?: string
}

export interface AiCommandRequest {
  serverId: number
  naturalLanguage: string
  sessionId?: string
}

// Server CRUD
export const serverPage = (params: {
  keyword?: string
  serverGroup?: string
  status?: number
  pageNum: number
  pageSize: number
}) => {
  return request.get<any>('/ai/server/page', { params })
}

export const serverDetail = (id: number) => {
  return request.get<Server>(`/ai/server/${id}`)
}

export const serverCreate = (data: Server) => {
  return request.post('/ai/server', data)
}

export const serverUpdate = (data: Server) => {
  return request.put('/ai/server', data)
}

export const serverDelete = (id: number) => {
  return request.delete(`/ai/server/${id}`)
}

export const serverConnect = (id: number) => {
  return request.post<Boolean>(`/ai/server/${id}/connect`)
}

export const serverDisconnect = (id: number) => {
  return request.post(`/ai/server/${id}/disconnect`)
}

export const serverTestConnection = (id: number) => {
  return request.post<Boolean>(`/ai/server/${id}/test`)
}

// Command execution
export const executeCommand = (data: CommandRequest) => {
  return request.post<CommandHistory>('/ai/server/command', data)
}

export const executeAiCommand = (data: AiCommandRequest) => {
  return request.post<CommandHistory>('/ai/server/command/ai', data)
}

// Command history
export const commandHistory = (params: {
  serverId?: number
  sessionId?: string
  pageNum: number
  pageSize: number
}) => {
  return request.get<any>('/ai/server/history', { params })
}

// AI features
export const aiGenerateCommand = (serverId: number, naturalLanguage: string) => {
  return request.get<AiCommandResult>('/ai/server/ai/generate', {
    params: { serverId, naturalLanguage }
  })
}

export const aiValidateCommand = (command: string) => {
  return request.get<AiValidationResult>('/ai/server/ai/validate', {
    params: { command }
  })
}

export const aiGetTemplates = () => {
  return request.get<CommandTemplate[]>('/ai/server/ai/templates')
}

// Private key import
export const importPrivateKey = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<string>('/ai/server/import-key', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
