import { afterEach, describe, expect, it } from 'vitest'
import { getTenantConfig } from './tenant'

afterEach(() => {
  window.__SMART_CONFIG__ = undefined
})

describe('tenant config defaults', () => {
  it('默认关闭访客申请记录 mock，避免真实业务误走演示数据', () => {
    window.__SMART_CONFIG__ = undefined
    expect(getTenantConfig().features.visitorRecordsMock).toBe(false)
  })

  it('允许运行时显式打开访客申请记录 mock', () => {
    window.__SMART_CONFIG__ = { features: { visitorRecordsMock: true } }
    expect(getTenantConfig().features.visitorRecordsMock).toBe(true)
  })
})
