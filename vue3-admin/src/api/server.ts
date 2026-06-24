import { http } from '@/utils/http'

export interface CpuInfo {
  cpuNum: number
  total: number
  sys: number
  used: number
  wait: number
  free: number
}

export interface MemInfo {
  total: number
  used: number
  free: number
  usage: number
}

export interface JvmInfo {
  total: number
  max: number
  free: number
  version: string
  home: string
  name: string
  vendor: string
  used: number
  usage: number
  startTime: string
  runTime: string
  inputArgs: string
}

export interface SysInfo {
  computerName: string
  computerIp: string
  userDir: string
  osName: string
  osArch: string
}

export interface SysFileInfo {
  dirName: string
  sysTypeName: string
  typeName: string
  total: string
  free: string
  used: string
  usage: number
}

export interface Server {
  cpu: CpuInfo
  mem: MemInfo
  jvm: JvmInfo
  sys: SysInfo
  sysFile: SysFileInfo[]
}

export const serverApi = {
  getInfo: () => http.get<Server>('/monitor/server')
}
