import { beforeEach, describe, expect, it, vi } from 'vitest'

// 电表管理 _service.js 请求签名契约（安全网）。
// 目的：后续从 index.vue 抽离纯函数 / 弹窗 / 服务编排时，任何 url、method 大小写、
// 参数承载位置（params vs data）、特殊请求头与 responseType 的漂移都会在此处红测，
// 防止结构重构夹带 API 行为变更。本测试只锁当前真实契约，不主张其是否“正确”。
const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: cfg => request(cfg) }))

const api = await import('./_service')

describe('electric_manage _service 请求签名契约', () => {
  beforeEach(() => request.mockClear())

  it('concentratorList → GET /platform/ele/meter/concentrator/page，query 走 params', () => {
    api.concentratorList({ current: 1, size: 999 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/concentrator/page',
      method: 'get',
      params: { current: 1, size: 999 }
    })
  })

  it('allList → POST /platform/park/allList，无 body', () => {
    api.allList()
    expect(request).toHaveBeenCalledWith({
      url: '/platform/park/allList',
      method: 'post'
    })
  })

  it('fetchRoomList → GET /platform/dormitory/room/list，query 走 params', () => {
    api.fetchRoomList({ parkId: 10 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/dormitory/room/list',
      method: 'get',
      params: { parkId: 10 }
    })
  })

  it('getTagList → GET /platform/device/tag/list', () => {
    api.getTagList()
    expect(request).toHaveBeenCalledWith({
      url: '/platform/device/tag/list',
      method: 'get'
    })
  })

  it('addObj → POST /platform/ele/meter/save，body 走 data', () => {
    api.addObj({ name: 'm1' })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/save',
      method: 'post',
      data: { name: 'm1' }
    })
  })

  it('putObj → POST /platform/ele/meter/update，body 走 data', () => {
    api.putObj({ id: 1 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/update',
      method: 'post',
      data: { id: 1 }
    })
  })

  it('changeObj → POST /platform/ele/meter/device/change，body 走 data', () => {
    api.changeObj({ id: 2 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/device/change',
      method: 'post',
      data: { id: 2 }
    })
  })

  it('delObj → POST /platform/ele/meter/del/:meterId，id 拼进 url', () => {
    api.delObj(42)
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/del/42',
      method: 'post'
    })
  })

  it('batchDelete → POST /platform/ele/meter/del，body 走 data', () => {
    api.batchDelete({ meterIds: [1, 2] })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/del',
      method: 'post',
      data: { meterIds: [1, 2] }
    })
  })

  it('fetchList → GET /platform/ele/meter/page，query 走 params', () => {
    api.fetchList({ current: 1, size: 20 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/page',
      method: 'get',
      params: { current: 1, size: 20 }
    })
  })

  it('recordList → GET /platform/ele/meter/history/page，query 走 params', () => {
    api.recordList({ meterId: 5 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/history/page',
      method: 'get',
      params: { meterId: 5 }
    })
  })

  it('logList → GET /platform/operate/log/list，query 走 params', () => {
    api.logList({ id: 9 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/operate/log/list',
      method: 'get',
      params: { id: 9 }
    })
  })

  it('setTagApi → POST /platform/ele/meter/tag/edit，body 走 data', () => {
    api.setTagApi({ tagIds: [1] })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/tag/edit',
      method: 'post',
      data: { tagIds: [1] }
    })
  })

  it('putValve → GET /platform/ele/meter/brake/change，单表闸门参数走 params', () => {
    api.putValve({ status: 1, eleMeterId: 3 })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/brake/change',
      method: 'get',
      params: { status: 1, eleMeterId: 3 }
    })
  })

  it('putValves → POST（大写）/platform/ele/meter/brake/batch/change，body 走 data', () => {
    api.putValves({ status: 1, meterIds: [1, 2] })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/brake/batch/change',
      method: 'POST',
      data: { status: 1, meterIds: [1, 2] }
    })
  })

  it('meterRead → POST（大写）/platform/ele/meter/reading，body 走 data', () => {
    api.meterRead({ meterIds: [1] })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/reading',
      method: 'POST',
      data: { meterIds: [1] }
    })
  })

  it('reDownload → POST（大写）/platform/ele/meter/re/download，body 走 data', () => {
    api.reDownload({ meterIds: [1] })
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/re/download',
      method: 'POST',
      data: { meterIds: [1] }
    })
  })

  it('supplierImport → POST multipart 上传，保留 arraybuffer 响应与 5 分钟超时', () => {
    const formData = { _formData: true }
    api.supplierImport(formData)
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/excel/import',
      method: 'post',
      data: formData,
      headers: { 'content-type': 'multipart/form-data' },
      responseType: 'arraybuffer',
      timeout: 1000 * 60 * 5
    })
  })

  it('flushProgress → GET /platform/ele/meter/flushProgress', () => {
    api.flushProgress()
    expect(request).toHaveBeenCalledWith({
      url: '/platform/ele/meter/flushProgress',
      method: 'get'
    })
  })
})
