import { describe, expect, it } from 'vitest'
import { applyStatusBadge, dispatchStatusText, formatVisitRange } from './record-status'

describe('formatVisitRange', () => {
  it('同日缩短结束为 HH:mm，跨日保留完整日期', () => {
    expect(formatVisitRange('2026-06-12 09:30', '2026-06-12 18:00')).toBe(
      '2026-06-12 09:30 ~ 18:00',
    )
    expect(formatVisitRange('2026-06-12 22:00', '2026-06-13 02:00')).toBe(
      '2026-06-12 22:00 ~ 2026-06-13 02:00',
    )
  })
})

describe('applyStatusBadge', () => {
  it('五种申请状态映射徽章文案与色调', () => {
    expect(applyStatusBadge('PENDING')).toEqual({ text: '审批中', tone: 'warning' })
    expect(applyStatusBadge('PASSED')).toEqual({ text: '已通过', tone: 'success' })
    expect(applyStatusBadge('REJECTED')).toEqual({ text: '已拒绝', tone: 'danger' })
    expect(applyStatusBadge('EXPIRED')).toEqual({ text: '已过期', tone: 'muted' })
    expect(applyStatusBadge('REVOKED')).toEqual({ text: '已撤销', tone: 'muted' })
  })

  it('未知状态回退原值灰调（快速暴露契约漂移）', () => {
    expect(applyStatusBadge('WHATEVER' as never)).toEqual({ text: 'WHATEVER', tone: 'muted' })
  })
})

describe('dispatchStatusText', () => {
  it('三种下发状态文案', () => {
    expect(dispatchStatusText('SUCCESS')).toEqual({ text: '已下发成功', tone: 'success' })
    expect(dispatchStatusText('ISSUING')).toEqual({ text: '正在下发', tone: 'warning' })
    expect(dispatchStatusText('FAILED')).toEqual({ text: '下发失败', tone: 'danger' })
  })

  it('未知状态回退原值灰调', () => {
    expect(dispatchStatusText('NOPE' as never)).toEqual({ text: 'NOPE', tone: 'muted' })
  })
})
