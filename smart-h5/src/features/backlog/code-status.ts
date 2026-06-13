/**
 * 免登录放行条 /code 的码区五态（旧 code.vue 互斥链事实：
 * expire 最高优先，其余按 status 1 审批中 / 2 出码 / 3 审核失败 / 4 已出厂 / 5 拒绝放行）。
 */
export type CodePanelState = 'expired' | 'reviewing' | 'qr' | 'rejected' | 'left' | 'denied' | 'none'

export function codePanelState(detail: { expire?: boolean; status?: number }): CodePanelState {
  if (detail.expire) return 'expired'
  switch (detail.status) {
    case 1:
      return 'reviewing'
    case 2:
      return 'qr'
    case 3:
      return 'rejected'
    case 4:
      return 'left'
    case 5:
      return 'denied'
    default:
      return 'none'
  }
}
