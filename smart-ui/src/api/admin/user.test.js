// src/api/admin/user.test.js
import { describe, it, expect, vi, beforeEach } from 'vitest'

// 每个 api 文件顶层 `import request from '@/router/axios'`；mock 成可记录的 spy，
// 断言每个 api 函数把正确的 url/method/params/data 传给 request。
const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: (cfg) => request(cfg) }))

const api = await import('./user')

describe('api/admin/user 请求签名契约', () => {
  beforeEach(() => request.mockClear())

  it('fetchList → GET /admin/user/page，query 走 params', () => {
    api.fetchList({ current: 1, size: 10 })
    expect(request).toHaveBeenCalledWith({
      url: '/admin/user/page', method: 'get', params: { current: 1, size: 10 }
    })
  })

  it('addObj → POST /admin/user/save，obj 走 data', () => {
    api.addObj({ username: 'u' })
    expect(request).toHaveBeenCalledWith({
      url: '/admin/user/save', method: 'post', data: { username: 'u' }
    })
  })

  it('getObj → GET /admin/user/:id', () => {
    api.getObj(42)
    expect(request).toHaveBeenCalledWith({ url: '/admin/user/42', method: 'get' })
  })

  it('delObj → POST /admin/user/:id（当前用 post，不是 DELETE）', () => {
    api.delObj(42)
    expect(request).toHaveBeenCalledWith({ url: '/admin/user/42', method: 'post' })
  })

  it('putObj → POST /admin/user/update，obj 走 data', () => {
    api.putObj({ id: 1 })
    expect(request).toHaveBeenCalledWith({
      url: '/admin/user/update', method: 'post', data: { id: 1 }
    })
  })

  it('getDetails → GET /admin/user/details/:obj', () => {
    api.getDetails(7)
    expect(request).toHaveBeenCalledWith({ url: '/admin/user/details/7', method: 'get' })
  })
})
