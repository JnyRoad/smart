import { describe, expect, it } from 'vitest'
import { listStatusTone, qrPanelState, showBackConfirm, showReleaseInfo } from './release-status'

describe('放行码三态（旧 detail.vue 互斥规则）', () => {
  it('expire 且 status<4 → 过期', () => {
    expect(qrPanelState({ expire: true, status: 2, qrCodePic: 'x' })).toBe('expired')
    expect(qrPanelState({ expire: true, status: 1 })).toBe('expired')
  })

  it('status=4 → 已出厂（即使 expire）', () => {
    expect(qrPanelState({ expire: false, status: 4 })).toBe('left')
    expect(qrPanelState({ expire: true, status: 4 })).toBe('left')
  })

  it('status=2 有码未过期 → 出码', () => {
    expect(qrPanelState({ expire: false, status: 2, qrCodePic: 'x' })).toBe('qr')
  })

  it('其余（审批中/拒绝/无码）→ 不显示', () => {
    expect(qrPanelState({ expire: false, status: 1 })).toBe('none')
    expect(qrPanelState({ expire: false, status: 3 })).toBe('none')
    expect(qrPanelState({ expire: false, status: 2 })).toBe('none') // 无 qrCodePic
    expect(qrPanelState({ status: 5 })).toBe('none')
  })
})

describe('列表状态配色', () => {
  it('2 绿 / 3 红 / 其余默认', () => {
    expect(listStatusTone(2)).toBe('success')
    expect(listStatusTone(3)).toBe('danger')
    expect(listStatusTone(1)).toBe('muted')
    expect(listStatusTone(undefined)).toBe('muted')
  })
})

describe('放行信息区与返厂按钮', () => {
  it('status 4/5 显示放行信息', () => {
    expect(showReleaseInfo(4)).toBe(true)
    expect(showReleaseInfo(5)).toBe(true)
    expect(showReleaseInfo(2)).toBe(false)
  })

  it('status=4 且无 backTime 才可确认返厂', () => {
    expect(showBackConfirm({ status: 4, backTime: null })).toBe(true)
    expect(showBackConfirm({ status: 4, backTime: undefined })).toBe(true)
    expect(showBackConfirm({ status: 4, backTime: '2026-06-12 10:00' })).toBe(false)
    expect(showBackConfirm({ status: 2, backTime: null })).toBe(false)
  })
})
