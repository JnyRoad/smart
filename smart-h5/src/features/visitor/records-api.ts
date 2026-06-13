import { ApiError, request } from '@/lib/api/http'
import { getTenantConfig } from '@/lib/config/tenant'
import type { ApplyStatus, DispatchStatus } from './record-status'
import { MOCK_DETAILS, MOCK_IDENTITY, MOCK_LIST, MOCK_QUERY_TOKEN, mockDelay } from './records-mock'

/**
 * Visitor "my applies" APIs. The contracts below mirror the gateway surface
 * for querying a visitor's own applications. With features.visitorRecordsMock on, list/detail functions
 * return demo fixtures instead of hitting the network. Sending an SMS remains
 * a real side-effect and is never short-circuited by the records mock.
 *
 * Auth model: listMyApply issues a short-lived queryToken bound to the
 * verified mobile/openId; the detail endpoints require it via the
 * X-Visitor-Query-Token header (anti-IDOR, see spec §2.1).
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
  state: 'done' | 'current' | 'wait' | 'rejected'
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

/** sessionStorage only: the token dies with the tab, never persisted. */
export function saveQuerySession(session: QuerySession): void {
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(session))
}

export function getQuerySession(): QuerySession | null {
  const raw = sessionStorage.getItem(SESSION_KEY)
  if (raw === null) return null
  try {
    return JSON.parse(raw) as QuerySession
  } catch {
    return null
  }
}

export function clearQuerySession(): void {
  sessionStorage.removeItem(SESSION_KEY)
}

/**
 * Auth rejection comes in two shapes: the gateway's business envelope
 * (`{code: 401|403}` — http.ts returns bodies for every HTTP status) or an
 * ApiError thrown for body-less 401/403 responses. Both mean: drop the
 * queryToken and re-verify.
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
  return request({
    module: 'platform',
    url: '/admittance/apply/app/sendRecordSms',
    method: 'POST',
    data: { mobile },
    auth: 'none',
  })
}

export interface MyAppliesResult {
  queryToken: string
  maskedName: string
  maskedMobile: string
  records: RecordSummary[]
}

/**
 * Verify identity and list applies. Pass `{mobile, smsCode}` or `{openId}`;
 * pass `null` to refresh with the existing queryToken header.
 */
export async function fetchMyApplies(
  input: { mobile?: string; smsCode?: string; openId?: string } | null,
): Promise<Envelope<MyAppliesResult>> {
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
