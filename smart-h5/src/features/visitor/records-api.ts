import { ApiError, request } from '@/lib/api/http'
import { getTenantConfig } from '@/lib/config/tenant'
import { sendVisitorSms } from './api'
import type { ApplyStatus, ApprovalNodeState, DispatchStatus } from './record-status'
import { MOCK_DETAILS, MOCK_IDENTITY, MOCK_LIST, MOCK_QUERY_TOKEN, mockDelay, revokeMockApply } from './records-mock'

/**
 * 访客“我的申请记录”接口：契约对齐网关侧本人申请查询能力。
 * features.visitorRecordsMock 开启时，列表和详情返回演示 fixture；短信仍走真实访客申请短信接口。
 *
 * 认证模型：listMyApply 在短信验证通过后签发 1 天 queryToken；详情接口必须带
 * X-Visitor-Query-Token 头，避免越权查看其他申请。
 */
interface Envelope<T> {
  code: number
  data?: T
  message?: string
}

export interface RecordSummary {
  applyId: string
  parkName: string
  applyStatus: ApplyStatus
  receptionistName: string
  startTime: string
  endTime: string
  fellowCount: number
  plates: string[]
  currentNode?: string
  dispatchStatus?: DispatchStatus
  submitTime: string
}

export interface ApplyRecordDetail {
  applyId: string
  applyNo: string
  parkName: string
  applyStatus: ApplyStatus
  dispatchStatus?: DispatchStatus
  receptionistName: string
  startTime: string
  endTime: string
  cause: string
  visitorName: string
  visitorPhone: string
  fellows: { name: string; phone: string }[]
  vehicles: { plate: string; type?: string }[]
  areas: string[]
  submitTime: string
}

export interface ApprovalNode {
  title: string
  state: ApprovalNodeState
  statusText?: string
  approverName?: string
  time?: string
  comment?: string
}

export interface QuerySession {
  queryToken: string
  maskedName: string
  maskedMobile: string
}

const SESSION_KEY = 'visitor-query-session'
const QUERY_SESSION_TTL_MS = 24 * 60 * 60 * 1000

type StoredQuerySession = QuerySession & { savedAt: number }

export function saveQuerySession(session: QuerySession): void {
  const now = Date.now()
  const savedAt = existingSavedAtForToken(session.queryToken, now) ?? now
  localStorage.setItem(SESSION_KEY, JSON.stringify({ ...session, savedAt }))
}

export function getQuerySession(): QuerySession | null {
  const raw = localStorage.getItem(SESSION_KEY)
  if (raw === null) return null
  try {
    const session = JSON.parse(raw) as Partial<StoredQuerySession>
    if (
      typeof session.queryToken !== 'string' ||
      typeof session.savedAt !== 'number' ||
      Date.now() - session.savedAt > QUERY_SESSION_TTL_MS
    ) {
      clearQuerySession()
      return null
    }
    return {
      queryToken: session.queryToken,
      maskedName: typeof session.maskedName === 'string' ? session.maskedName : '',
      maskedMobile: typeof session.maskedMobile === 'string' ? session.maskedMobile : '',
    }
  } catch {
    clearQuerySession()
    return null
  }
}

export function clearQuerySession(): void {
  localStorage.removeItem(SESSION_KEY)
}

function existingSavedAtForToken(queryToken: string, now: number): number | null {
  const raw = localStorage.getItem(SESSION_KEY)
  if (raw === null) return null
  try {
    const session = JSON.parse(raw) as Partial<StoredQuerySession>
    if (
      session.queryToken === queryToken &&
      typeof session.savedAt === 'number' &&
      now - session.savedAt <= QUERY_SESSION_TTL_MS
    ) {
      return session.savedAt
    }
  } catch {
    return null
  }
  return null
}

/**
 * 鉴权拒绝有两类形态：网关业务 envelope（http.ts 会返回所有 HTTP 状态的 body），
 * 或无 body 的 401/403 抛出的 ApiError。两者都要清掉 queryToken 并重新验证。
 */
export function isAuthRejected(value: unknown): boolean {
  if (value instanceof ApiError) return value.status === 401 || value.status === 403
  if (typeof value === 'object' && value !== null && 'code' in value) {
    const code = (value as { code: unknown }).code
    return code === 401 || code === 403
  }
  return false
}

const isMockOn = () => getTenantConfig().features.visitorRecordsMock

function tokenHeaders(): Record<string, string> {
  const token = getQuerySession()?.queryToken
  return token ? { 'X-Visitor-Query-Token': token } : {}
}

export async function sendRecordSms(mobile: string): Promise<Envelope<unknown>> {
  return sendVisitorSms(mobile)
}

export interface MyAppliesResult {
  queryToken: string
  maskedName: string
  maskedMobile: string
  records: RecordSummary[]
}

/** 校验身份并查询申请列表；传 null 时使用已有 queryToken 刷新。 */
export async function fetchMyApplies(
  input: { mobile?: string; smsCode?: string } | null,
): Promise<Envelope<MyAppliesResult>> {
  if (input && 'openId' in input) {
    throw new Error('访客记录查询仅支持短信验证码或已有查询凭证')
  }
  if (isMockOn()) {
    await mockDelay()
    return {
      code: 0,
      data: { queryToken: MOCK_QUERY_TOKEN, ...MOCK_IDENTITY, records: MOCK_LIST },
    }
  }
  return request({
    module: 'platform',
    url: '/admittance/apply/app/listMyApply',
    method: 'POST',
    data: input ?? {},
    auth: 'none',
    headers: tokenHeaders(),
  })
}

export async function fetchApplyDetail(applyId: string): Promise<Envelope<ApplyRecordDetail>> {
  if (isMockOn()) {
    await mockDelay()
    const entry = MOCK_DETAILS[applyId]
    return entry ? { code: 0, data: entry.detail } : { code: 1, message: '申请单不存在' }
  }
  return request({
    module: 'platform',
    url: '/admittance/apply/app/applyDetail',
    params: { applyId },
    auth: 'none',
    headers: tokenHeaders(),
  })
}

export async function fetchApprovalProgress(
  applyId: string,
): Promise<Envelope<{ nodes: ApprovalNode[] }>> {
  if (isMockOn()) {
    await mockDelay()
    const entry = MOCK_DETAILS[applyId]
    return entry ? { code: 0, data: { nodes: entry.nodes } } : { code: 1, message: '申请单不存在' }
  }
  return request({
    module: 'platform',
    url: '/admittance/apply/app/approvalProgress',
    params: { applyId },
    auth: 'none',
    headers: tokenHeaders(),
  })
}

/** 作废本人名下的入厂申请，并由后端提交关联权限的回收任务。 */
export async function revokeApply(applyId: string): Promise<Envelope<boolean>> {
  if (isMockOn()) {
    await mockDelay()
    return revokeMockApply(applyId)
      ? { code: 0, data: true }
      : { code: 1, message: '申请单不存在或已作废' }
  }
  return request({
    module: 'platform',
    url: '/admittance/apply/app/revoke',
    method: 'POST',
    data: { applyId },
    auth: 'none',
    headers: tokenHeaders(),
  })
}
