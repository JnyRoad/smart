import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: cfg => request(cfg) }))

const api = await import('./_service')

describe('incoming_record _service 请求签名契约', () => {
  beforeEach(() => request.mockClear())

  it('revoke → POST 管理端作废路径，申请单 ID 走 body', () => {
    expect(typeof api.xcIncomingRecordApi.revoke).toBe('function')

    api.xcIncomingRecordApi.revoke({ id: 3201 })

    expect(request).toHaveBeenCalledWith({
      url: '/platform/manage/admittance/apply/revoke',
      method: 'post',
      data: { id: 3201 }
    })
  })
})
