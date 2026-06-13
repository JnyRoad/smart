import { request } from '@/lib/api/http'

export interface EmployeeFullInfo {
  employeeId?: string
  employeeName?: string
}

export interface EmployeeBaseInfo {
  employeeName?: string
  employeeBadge?: string
  employeePhoto?: string
  /** 0 = male, 1 = female */
  employeeSex?: number
  mobile?: string
  buName?: string
  deptName?: string
  jobName?: string
  jcheName?: string
  welfareLevel?: string
  statusDes?: string
  empTypeDes?: string
  empAttribute?: string
  /** 0 = resigned: the home page force-logs the user out */
  status?: number
  isSecurityGuard?: number | boolean
}

interface GatewayEnvelope<T> {
  code: number
  data?: T
  message?: string
}

export function getEmployeeFullInfo(): Promise<GatewayEnvelope<EmployeeFullInfo>> {
  return request({ module: 'app', url: '/employee/fullinfo' })
}

export function getEmployeeBaseInfo(): Promise<GatewayEnvelope<EmployeeBaseInfo>> {
  return request({ module: 'app', url: '/employee/baseinfo' })
}

/** Unbinds the WeChat account from the employee badge. */
export function unbindWechat(): Promise<GatewayEnvelope<unknown>> {
  return request({ module: 'app', url: '/wechat/xc/unbind', method: 'POST' })
}
