/**
 * Release-pass status rules shared by living/office detail pages and the
 * return-factory flow (legacy detail.vue exclusive-branch facts).
 *
 * status: 1 待审批, 2 已通过(出码), 3 已拒绝, 4 已出厂, 5 异常终态(同 4 展示放行信息).
 */

export type QrPanelState = 'expired' | 'left' | 'qr' | 'none'

export function qrPanelState(detail: {
  expire?: boolean
  status?: number
  qrCodePic?: string
}): QrPanelState {
  // Deviation from legacy: status missing + expire shows 'expired' here
  // (legacy `undefined < 4` is false and showed nothing); real data has status.
  if (detail.expire && (detail.status ?? 0) < 4) return 'expired'
  if (detail.status === 4) return 'left'
  if (detail.qrCodePic && detail.status === 2 && !detail.expire) return 'qr'
  return 'none'
}

export type StatusTone = 'success' | 'danger' | 'muted'

export function listStatusTone(status: number | undefined): StatusTone {
  if (status === 2) return 'success'
  if (status === 3) return 'danger'
  return 'muted'
}

export function showReleaseInfo(status: number | undefined): boolean {
  return status === 4 || status === 5
}

/** 确认返厂按钮：已出厂且还没有返厂时间。 */
export function showBackConfirm(detail: { status?: number; backTime?: string | null }): boolean {
  return detail.status === 4 && !detail.backTime
}
