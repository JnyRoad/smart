import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: cfg => request(cfg) }))

const api = await import('./room')

describe('api/platform/dormitory/room 请求签名契约', () => {
  beforeEach(() => request.mockClear())

  it('fetchList → GET /platform/dormitory/room/page，query 走 params', () => {
    api.fetchList({ current: 1, size: 20 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/dormitory/room/page',
      method: 'get',
      params: { current: 1, size: 20 }
    })
  })

  it('fetchRoomList → GET /platform/dormitory/room/list，query 走 params', () => {
    api.fetchRoomList({ parkId: 10, floorId: 30 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/dormitory/room/list',
      method: 'get',
      params: { parkId: 10, floorId: 30 }
    })
  })

  it('floorList → GET /platform/park/dormTreeNonRoom', () => {
    api.floorList()
    expect(request).toHaveBeenCalledWith({
      url: '/platform/park/dormTreeNonRoom',
      method: 'get'
    })
  })

  it('putObj / putBatchObj / putSDBatchObj keep POST data signatures', () => {
    api.putObj({ id: 1 })
    api.putBatchObj({ roomIds: [1, 2] })
    api.putSDBatchObj({ roomIds: [3], sdTemplateId: 9 })

    expect(request).toHaveBeenNthCalledWith(1, {
      url: '/platform/dormitory/room/updateRoom',
      method: 'post',
      data: { id: 1 }
    })
    expect(request).toHaveBeenNthCalledWith(2, {
      url: '/platform/dormitory/room/batchUpdateAttrByIds',
      method: 'post',
      data: { roomIds: [1, 2] }
    })
    expect(request).toHaveBeenNthCalledWith(3, {
      url: '/platform/dormitory/room/batchUpdateSDTemp',
      method: 'post',
      data: { roomIds: [3], sdTemplateId: 9 }
    })
  })

  it('delObj → POST /platform/dormitory/room/:id', () => {
    api.delObj(42)
    expect(request).toHaveBeenCalledWith({
      url: '/platform/dormitory/room/42',
      method: 'post'
    })
  })

  it('keeps room type, bed number, sd template, and visual endpoints stable', () => {
    api.dormTypeApi({ parkId: 10 })
    api.allDormitoryType()
    api.getBedNum(7)
    api.fetchSDTempList(10)
    api.roomVisual({ roomId: 1 })

    expect(request).toHaveBeenNthCalledWith(1, {
      url: '/platform/dormitory/type/by/park',
      method: 'get',
      params: { parkId: 10 }
    })
    expect(request).toHaveBeenNthCalledWith(2, {
      url: '/platform/dormitory/type/all',
      method: 'get'
    })
    expect(request).toHaveBeenNthCalledWith(3, {
      url: '/platform/dormitory/type/7',
      method: 'get'
    })
    expect(request).toHaveBeenNthCalledWith(4, {
      url: '/platform/dormitory/sd/parkId/10',
      method: 'get'
    })
    expect(request).toHaveBeenNthCalledWith(5, {
      url: '/platform/dormitory/room/roomVisual',
      method: 'post',
      data: { roomId: 1 }
    })
  })
})
