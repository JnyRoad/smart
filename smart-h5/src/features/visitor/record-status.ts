export type ApplyStatus = 'PENDING' | 'PASSED' | 'REJECTED' | 'EXPIRED' | 'REVOKED'
export type DispatchStatus = 'SUCCESS' | 'ISSUING' | 'FAILED'
export type Tone = 'success' | 'warning' | 'danger' | 'muted'

export interface StatusLabel {
  text: string
  tone: Tone
}

const APPLY_BADGES: Record<ApplyStatus, StatusLabel> = {
  PENDING: { text: '审批中', tone: 'warning' },
  PASSED: { text: '已通过', tone: 'success' },
  REJECTED: { text: '已拒绝', tone: 'danger' },
  EXPIRED: { text: '已过期', tone: 'muted' },
  REVOKED: { text: '已撤销', tone: 'muted' },
}

/** Unknown values fall back to the raw text in muted tone so contract drift is visible. */
export function applyStatusBadge(status: ApplyStatus): StatusLabel {
  return APPLY_BADGES[status] ?? { text: String(status), tone: 'muted' }
}

const DISPATCH_TEXTS: Record<DispatchStatus, StatusLabel> = {
  SUCCESS: { text: '已下发成功', tone: 'success' },
  ISSUING: { text: '正在下发', tone: 'warning' },
  FAILED: { text: '下发失败', tone: 'danger' },
}

export function dispatchStatusText(status: DispatchStatus): StatusLabel {
  return DISPATCH_TEXTS[status] ?? { text: String(status), tone: 'muted' }
}

/** Same-day ranges shorten the end to HH:mm; cross-day ranges keep the full date. */
export function formatVisitRange(start: string, end: string): string {
  const sameDay = start.slice(0, 10) === end.slice(0, 10)
  return `${start} ~ ${sameDay ? end.slice(-5) : end}`
}
