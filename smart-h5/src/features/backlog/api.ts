import { request } from '@/lib/api/http'
import { hasValidToken } from '@/lib/auth/token'
import type { ReleaseDetail } from '@/features/good-release/api'

interface Envelope<T> {
  code: number
  data?: T
  message?: string
  msg?: string
}

interface PageData<T> {
  records?: T[]
  pages?: number
}

/** 各审批列表共用的可选搜索条件（licensePlate 契约保留、UI 无入口）。 */
export interface ApprovalSearch {
  badge?: string
  name?: string
  startTime?: string
  endTime?: string
  releaseItem?: number
  licensePlate?: string
}

/** 免登录 /code 页详情：有 token 则带，无 token 匿名访问（旧版同语义）。 */
export function getReleaseDetailForCode(id: string): Promise<Envelope<ReleaseDetail>> {
  return request({
    module: 'platform',
    url: `/articlesrelease/detail/${id}`,
    auth: hasValidToken() ? 'bearer' : 'none',
  })
}

// ===== 生活区放行审批 =====

export interface LiveApprovalItem {
  approveId?: string | number
  approveName?: string
  approveNodeDesc?: string
  approveDesc?: string
  /** '1' 通过（绿）/ '2' 拒绝（红）。 */
  approveState?: string
  /** 1 室友 / 2 宿管 / 3 保安。 */
  sort?: number
  articlesTypeDesc?: string
  articleName?: string
  carrier?: string
  roomInfo?: string
  createTime?: string
}

export function getLiveApprovalPage(
  params: { recordState: 0 | 1; current: number; size: number } & ApprovalSearch,
): Promise<Envelope<PageData<LiveApprovalItem>>> {
  return request({
    module: 'platform',
    url: '/approve/list/new/page',
    params: { ...params, recordType: 3 },
  })
}

/** 室友/宿管审批（status 2 通过 / 3 拒绝）。 */
export function updateReleaseStatus(params: {
  approveBadge: string
  id: string | number
  status: 2 | 3
  remark?: string
}): Promise<Envelope<unknown>> {
  return request({ module: 'platform', url: '/articlesrelease/status/update', params: { ...params } })
}

/** 保安放行（status 4 确认 / 5 拒绝），三图按序铺开。 */
export function securityUpdateRelease(data: {
  guardOneImg: string
  guardTwoImg: string
  guardThreeImg: string
  id: string | number
  parkId: number
  status: 4 | 5
  badge: string
  remark?: string
}): Promise<Envelope<unknown>> {
  return request({
    module: 'platform',
    url: '/articlesrelease/status/security/update',
    method: 'POST',
    data,
  })
}

// ===== 办公区放行审批 =====

export interface WorkApprovalItem {
  id?: string | number
  name?: string
  backStatus?: string
  deptName?: string
  releaseItemDesc?: string
  createTime?: string
  oaNode?: string
}

export function getWorkApprovalPage(
  params: { approvalStatus: 0 | 1; current: number; size: number } & ApprovalSearch,
): Promise<Envelope<PageData<WorkApprovalItem>>> {
  return request({ module: 'platform', url: '/articlesrelease/office/approval/page', params: { ...params } })
}

// ===== 报修审批 =====

export interface RepairApprovalItem {
  approveId?: string | number
  approveName?: string
  statusDesc?: string
  rangeTypeDesc?: string
  repairTypeDesc?: string
  dormitoryName?: string
  faultDesc?: string
  createTime?: string
}

export function getRepairApprovalPage(params: {
  recordState: 0 | 1
  current: number
  size: number
}): Promise<Envelope<PageData<RepairApprovalItem>>> {
  return request({
    module: 'platform',
    url: '/approve/list/repairs/list',
    params: { ...params, recordType: 5 },
  })
}

/** 接单（status 1）/ 不接单（status 2）。 */
export function updateRepairStatus(params: {
  approveBadge: string
  id: string | number
  status: 1 | 2
  remark?: string
}): Promise<Envelope<unknown>> {
  return request({ module: 'platform', url: '/dormitory/repair/status/update', params: { ...params } })
}

/** 维修结果回复（status 3 已安排维修 / 4 无法维修）。 */
export function replyRepair(data: {
  approveBadge: string
  id: string | number
  result: string
  status: 3 | 4
}): Promise<Envelope<unknown>> {
  return request({ module: 'platform', url: '/dormitory/repair/reply', method: 'POST', data })
}

// ===== 退宿审批 =====

export interface ExitApprovalItem {
  id?: string | number
  name?: string
  status?: number
  statusDesc?: string
  dorDetailStr?: string[]
  quitReasonDesc?: string
  applyLeaveTime?: string
  createTime?: string
}

export function getExitApprovalPage(data: {
  isSecurityGuard: number
  parkId: number
  status: 0 | 1
  current: number
  size: number
  badge?: string
  name?: string
}): Promise<Envelope<PageData<ExitApprovalItem>>> {
  return request({ module: 'platform', url: '/dor/quit/list/approval', method: 'POST', data })
}

/** 退宿审批（status 2 通过 / 3 拒绝 / 4 保安通过 / 5 保安拒绝）。 */
export function updateExitStatus(params: {
  id: string | number
  status: 2 | 3 | 4 | 5
  approveBadge: string
  remark?: string
}): Promise<Envelope<unknown>> {
  return request({ module: 'platform', url: '/dor/quit/status/update', params: { ...params } })
}
