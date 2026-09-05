import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: {} }))

vi.mock('@/router/axios', () => ({ default: config => request(config) }))

const api = await import('./securityAuthDeleteLog')

describe('保密区自动删权报表 API', () => {
  beforeEach(() => request.mockClear())

  it('按当前组合筛选请求分页记录，并由服务端控制园区范围', () => {
    const query = {
      current: 2,
      size: 50,
      parkId: 1001,
      startTime: '2026-09-01 00:00:00',
      endTime: '2026-09-05 23:59:59',
      staffBadge: 'A001',
      staffName: '张三',
      department: '研发部',
      authName: '保密区权限',
      result: 'FAILED'
    }

    api.fetchPage(query)

    expect(request).toHaveBeenCalledWith({
      url: '/platform/security/auth/delete/log/page',
      method: 'get',
      params: query
    })
    expect(request.mock.calls[0][0].params).not.toHaveProperty('parkIdList')
  })

  it('使用当前筛选导出 CSV 二进制响应，并保留长任务超时', () => {
    const query = { parkId: 1001, result: 'SUCCESS' }

    api.exportLogs(query)

    expect(request).toHaveBeenCalledWith({
      url: '/platform/security/auth/delete/log/export',
      method: 'get',
      params: query,
      responseType: 'arraybuffer',
      timeout: 1000 * 60 * 5,
      transformResponse: expect.any(Array)
    })
  })

  it('将 401 的真实 JSON ArrayBuffer 解码为对象，让全局拦截器读取 msg 并执行登出', () => {
    api.exportLogs({ parkId: 1001 })
    const config = request.mock.calls[0][0]
    const transformResponse = Array.isArray(config.transformResponse)
      ? config.transformResponse[0]
      : config.transformResponse
    const payload = new TextEncoder().encode(JSON.stringify({ msg: '认证失败' })).buffer

    expect(transformResponse(payload, { 'content-type': 'application/json;charset=UTF-8' })).toStrictEqual({
      msg: '认证失败'
    })
  })

  it('将 200 code=1 的真实 JSON ArrayBuffer 解码为对象并保留超限提示', () => {
    api.exportLogs({ parkId: 1001 })
    const config = request.mock.calls[0][0]
    const transformResponse = Array.isArray(config.transformResponse)
      ? config.transformResponse[0]
      : config.transformResponse
    const payload = new TextEncoder().encode(JSON.stringify({ code: 1, msg: '记录超过 10000 条，请缩小范围' })).buffer

    expect(transformResponse(payload, { 'content-type': 'application/json;charset=UTF-8' })).toStrictEqual({
      code: 1,
      msg: '记录超过 10000 条，请缩小范围'
    })
  })

  it('保持成功 CSV ArrayBuffer 原样传给下载逻辑', () => {
    api.exportLogs({ parkId: 1001 })
    const config = request.mock.calls[0][0]
    const transformResponse = Array.isArray(config.transformResponse)
      ? config.transformResponse[0]
      : config.transformResponse
    const payload = new TextEncoder().encode('执行时间,结果\n2026-09-05 10:00:00,SUCCESS\n').buffer

    expect(transformResponse(payload, { 'content-type': 'text/csv;charset=UTF-8' })).toBe(payload)
  })

  it('只从指定审计记录下钻任务，不暴露任意任务查询', () => {
    api.fetchTasks('9007199254740993')

    expect(request).toHaveBeenCalledWith({
      url: '/platform/security/auth/delete/log/9007199254740993/tasks',
      method: 'get'
    })
  })
})
