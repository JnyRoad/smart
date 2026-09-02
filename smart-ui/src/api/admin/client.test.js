// src/api/admin/client.test.js
import { describe, it, expect, vi, beforeEach } from 'vitest'

// 每个 api 文件顶层 `import request from '@/router/axios'`；mock 成可记录的 spy，
// 断言每个 api 函数把正确的 url/method/params/data 传给 request。
const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: (cfg) => request(cfg) }))

const api = await import('./client')

describe('api/admin/client 请求签名契约', () => {
  beforeEach(() => request.mockClear())

  it('fetchList → GET /admin/client/page，query 走 params', () => {
    api.fetchList({ current: 1, size: 10 })
    expect(request).toHaveBeenCalledWith({
      url: '/admin/client/page', method: 'get', params: { current: 1, size: 10 }
    })
  })

  it('fetchScopes → GET /admin/client/scopes，读取后端权威 capability scope 目录', () => {
    expect(api.fetchScopes).toBeTypeOf('function')
    api.fetchScopes()
    expect(request).toHaveBeenCalledWith({ url: '/admin/client/scopes', method: 'get' })
  })

  it('addObj → POST /admin/client/save，obj 走 data', () => {
    api.addObj({ clientId: 'app-1' })
    expect(request).toHaveBeenCalledWith({
      url: '/admin/client/save', method: 'post', data: { clientId: 'app-1' }
    })
  })

  it('getObj → GET /admin/client/:id', () => {
    api.getObj('app-1')
    expect(request).toHaveBeenCalledWith({ url: '/admin/client/app-1', method: 'get' })
  })

  it('delObj → POST /admin/client/:id（当前用 post，不是 DELETE）', () => {
    api.delObj('app-1')
    expect(request).toHaveBeenCalledWith({ url: '/admin/client/app-1', method: 'post' })
  })

  it('putObj → POST /admin/client/update，obj 走 data', () => {
    api.putObj({ clientId: 'app-1' })
    expect(request).toHaveBeenCalledWith({
      url: '/admin/client/update', method: 'post', data: { clientId: 'app-1' }
    })
  })

  it('resetSecret → PUT /admin/client/secret/:clientId', () => {
    api.resetSecret('app-1')
    expect(request).toHaveBeenCalledWith({ url: '/admin/client/secret/app-1', method: 'put' })
  })
})

describe('mergeAllowedParkIds 防御性 merge', () => {
  it('原字段为空时，直接写入 allowedParkIds，不报解析错误', () => {
    const result = api.mergeAllowedParkIds(undefined, [1, 2])
    expect(JSON.parse(result.text)).toEqual({ allowedParkIds: [1, 2] })
    expect(result.parseError).toBe(false)
  })

  it('原字段已有其他 JSON 键时，只覆盖 allowedParkIds，其余键保留', () => {
    const raw = JSON.stringify({ foo: 'bar', allowedParkIds: [9] })
    const result = api.mergeAllowedParkIds(raw, [1, 2, 3])
    expect(JSON.parse(result.text)).toEqual({ foo: 'bar', allowedParkIds: [1, 2, 3] })
    expect(result.parseError).toBe(false)
  })

  it('原字段是非法 JSON 时，标记 parseError，不静默覆盖为只剩 allowedParkIds 之外的内容丢失', () => {
    const result = api.mergeAllowedParkIds('{not valid json', [1])
    expect(result.parseError).toBe(true)
    // 仍然返回可用的 text，交由调用方决定是否在提示用户后继续保存
    expect(JSON.parse(result.text)).toEqual({ allowedParkIds: [1] })
  })

  it('原字段解析结果不是对象（如数组/字符串）时，同样标记 parseError', () => {
    const arrayResult = api.mergeAllowedParkIds('[1,2,3]', [1])
    expect(arrayResult.parseError).toBe(true)

    const stringResult = api.mergeAllowedParkIds('"just a string"', [1])
    expect(stringResult.parseError).toBe(true)
  })

  it('allowedParkIds 为空数组时正常写入空数组', () => {
    const result = api.mergeAllowedParkIds(JSON.stringify({ foo: 'bar' }), [])
    expect(JSON.parse(result.text)).toEqual({ foo: 'bar', allowedParkIds: [] })
    expect(result.parseError).toBe(false)
  })
})
