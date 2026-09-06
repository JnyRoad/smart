import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: {} }))

vi.mock('@/router/axios', () => ({ default: config => request(config) }))

const api = await import('./limit')

describe('权限任务查询 API 契约', () => {
  beforeEach(() => request.mockClear())

  it('批次列表把分页和精确筛选作为 GET 查询参数发送', () => {
    const params = {
      current: 2,
      size: 20,
      parkId: '26',
      action: 'DELETE',
      status: 'VERIFYING',
      sourceType: 'STAFF_AUTHORITY',
      sourceId: 'operation-key-001'
    }

    api.fetchOperationBatchPage(params)

    expect(request).toHaveBeenCalledWith({
      url: '/platform/device/authority/operation/batch/page',
      method: 'get',
      params
    })
  })

  it('批次详情请求完整保留超过 JavaScript 安全整数的字符串 ID', () => {
    api.getOperationBatchDetail('9007199254740993')

    expect(request).toHaveBeenCalledWith({
      url: '/platform/device/authority/operation/batch/9007199254740993',
      method: 'get'
    })
  })

  it('目标明细只请求当前批次的服务端分页', () => {
    const params = {
      batchId: '9007199254740993',
      current: 3,
      size: 20,
      state: 'FAILED,VERIFYING',
      deviceId: 'device-001',
      subjectType: 'STAFF'
    }

    api.fetchOperationTargetPage(params)

    expect(request).toHaveBeenCalledWith({
      url: '/platform/device/authority/operation/target/page',
      method: 'get',
      params
    })
  })
})

 it('人员回执使用独立写端点，旧端点仍可用于旧客户端', async () => {
    request.mockClear()
    const payload = { authId: '9', type: '2', delIds: ['5'] }
    await api.batchDelPersonWithReceipt(payload)
    await api.clearPersonWithReceipt('9')
    await api.batchDel(payload)
    expect(request.mock.calls.map(([config]) => config.url)).toEqual([
      '/platform/device/authority/relation/person/del/receipt',
      '/platform/device/authority/relation/person/clear/9/receipt',
      '/platform/device/authority/relation/del'
    ])
    expect(request.mock.calls[0][0].data).toEqual(payload)
  })

 it('能力查询与可选请求键契约只作用于人员回执', async () => {
  request.mockClear()
  await api.personIntakeCapability(9)
  await api.batchDelPersonWithReceipt({ authId: 9, type: 1, delIds: [5] }, 'stable-client-key')
  await api.clearPersonWithReceipt(9, 'stable-client-key')
  expect(request.mock.calls[0][0]).toEqual({ url: '/platform/device/authority/relation/person/9/intake-capability', method: 'get' })
  expect(request.mock.calls[1][0].headers).toEqual({ 'Idempotency-Key': 'stable-client-key' })
  expect(request.mock.calls[2][0].headers).toEqual({ 'Idempotency-Key': 'stable-client-key' })
 })
