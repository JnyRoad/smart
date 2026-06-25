import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: cfg => request(cfg) }))

const api = await import('./dormitory')

describe('api/platform/dormitory/dormitory 请求签名契约', () => {
  beforeEach(() => request.mockClear())

  it('fetchList → GET /platform/dormitory/page，query 走 params', () => {
    api.fetchList({ current: 1, parkId: 10 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/dormitory/page',
      method: 'get',
      params: { current: 1, parkId: 10 }
    })
  })

  it('putDormObj / addObj keep POST data signatures', () => {
    api.putDormObj({ id: 2, dormitoryName: 'A栋' })
    api.addObj({ parkId: 10, dormitoryName: 'B栋' })

    expect(request).toHaveBeenNthCalledWith(1, {
      url: '/platform/dormitory/updateDormitory',
      method: 'post',
      data: { id: 2, dormitoryName: 'A栋' }
    })
    expect(request).toHaveBeenNthCalledWith(2, {
      url: '/platform/dormitory/addDormitory',
      method: 'post',
      data: { parkId: 10, dormitoryName: 'B栋' }
    })
  })

  it('delDormObj → POST /platform/dormitory/:id', () => {
    api.delDormObj(8)
    expect(request).toHaveBeenCalledWith({
      url: '/platform/dormitory/8',
      method: 'post'
    })
  })

  it('getDormObj → GET /platform/dormitory/:id', () => {
    api.getDormObj(8)
    expect(request).toHaveBeenCalledWith({
      url: '/platform/dormitory/8',
      method: 'get'
    })
  })
})
