import { describe, expect, it } from 'vitest'
import { admittanceNoticeHtml } from './api'

describe('admittanceNoticeHtml', () => {
  it('uses backend content when admittance notice is enabled', () => {
    expect(admittanceNoticeHtml({ isNeedNotice: 1, content: '<p>入园请佩戴口罩</p>' })).toBe(
      '<p>入园请佩戴口罩</p>',
    )
  })

  it('keeps compatibility with existing noticeContent mocks', () => {
    expect(
      admittanceNoticeHtml({ isNeedNotice: 1, noticeContent: '<p>凭码通行</p>' }),
    ).toBe('<p>凭码通行</p>')
  })

  it('returns empty string when notice is disabled or blank', () => {
    expect(admittanceNoticeHtml({ isNeedNotice: 0, content: '<p>隐藏</p>' })).toBe('')
    expect(admittanceNoticeHtml({ isNeedNotice: 1, content: '   ' })).toBe('')
    expect(admittanceNoticeHtml(undefined)).toBe('')
  })
})
