import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: cfg => request(cfg) }))

const api = await import('./floor')

describe('api/platform/dormitory/floor 请求签名契约', () => {
  beforeEach(() => request.mockClear())

  it('fetchList → GET /platform/dormitory/floor/page，query 走 params', () => {
    api.fetchList({ dormitoryId: 3 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/dormitory/floor/page',
      method: 'get',
      params: { dormitoryId: 3 }
    })
  })

  it('queryDormitory / addFloor / updateDormitoryFloor keep POST data signatures', () => {
    api.queryDormitory({ parkId: 10 })
    api.addFloor({ dormitoryId: 3, floorNum: 2 })
    api.updateDormitoryFloor({ id: 5, roomNum: 10 })

    expect(request).toHaveBeenNthCalledWith(1, {
      url: '/platform/dormitory/queryDormitory',
      method: 'post',
      data: { parkId: 10 }
    })
    expect(request).toHaveBeenNthCalledWith(2, {
      url: '/platform/dormitory/floor/addFloor',
      method: 'post',
      data: { dormitoryId: 3, floorNum: 2 }
    })
    expect(request).toHaveBeenNthCalledWith(3, {
      url: '/platform/dormitory/floor/updateDormitoryFloor',
      method: 'post',
      data: { id: 5, roomNum: 10 }
    })
  })

  it('delFloor / getFloor / getFloorStartNum keep path signatures', () => {
    api.delFloor(5)
    api.getFloor(5)
    api.getFloorStartNum(3)

    expect(request).toHaveBeenNthCalledWith(1, {
      url: '/platform/dormitory/floor/5',
      method: 'post'
    })
    expect(request).toHaveBeenNthCalledWith(2, {
      url: '/platform/dormitory/floor/5',
      method: 'get'
    })
    expect(request).toHaveBeenNthCalledWith(3, {
      url: '/platform/dormitory/floor/getFloorStartNum/3',
      method: 'get'
    })
  })
})
