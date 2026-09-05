import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: cfg => request(cfg) }))

const api = await import('./manualAuth')

describe('访客手动授权 API 请求契约', () => {
  beforeEach(() => request.mockClear())

  it('查询授权选项使用受保护路径和 applyId 参数', () => {
    api.getManualAuthOptions('101')

    expect(request).toHaveBeenCalledWith({
      url: '/platform/manage/admittance/apply/device/auth/options',
      method: 'get',
      params: { applyId: '101' }
    })
  })

  it('提交手动授权使用契约 body，不改写请求载荷', () => {
    const data = { applyId: '101', fellowId: '201', authIds: [401] }
    api.submitManualAuth(data)

    expect(request).toHaveBeenCalledWith({
      url: '/platform/manage/admittance/apply/device/auth',
      method: 'post',
      data
    })
  })
})
