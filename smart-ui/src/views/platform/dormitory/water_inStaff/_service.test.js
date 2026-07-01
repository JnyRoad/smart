import { beforeEach, describe, expect, it, vi } from 'vitest'

// axios.js 里全局超时是 30 秒（约定：跑得久的导出类接口要自己覆盖超时，不要加大全局超时）。
// 但 by-dor / export/detail 这两个导出接口实测稳定要 45-50 秒，一直没有按约定覆盖超时，
// 导致导出必然在拿到数据前被前端超时打断。这里锁定两个导出接口都单独放宽了超时。

const request = vi.fn(() => Promise.resolve({ data: { data: [] } }))
vi.mock('@/router/axios', () => ({ default: (...args) => request(...args) }))

const { exportData, exportShareData, fetchList } = await import('./_service')

describe('water_inStaff _service export requests', () => {
  beforeEach(() => {
    request.mockClear()
  })

  it('exportData overrides the 30s global axios timeout for the by-dor export', async () => {
    await exportData({ dormitoryIds: '5010481', meterMonth: '2026-06' })

    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: '/platform/dormitory/staff/statementdetail/by-dor',
      timeout: expect.any(Number)
    }))
    const config = request.mock.calls[0][0]
    expect(config.timeout).toBeGreaterThan(30000)
  })

  it('exportShareData overrides the 30s global axios timeout for the export/detail export', async () => {
    await exportShareData({ dormitoryIds: '5010481', meterMonth: '2026-06' })

    const config = request.mock.calls[0][0]
    expect(config.url).toBe('/platform/dormitory/staff/statementdetail/export/detail')
    expect(config.timeout).toBeGreaterThan(30000)
  })

  it('fetchList (paginated list, already fast) keeps using the global default timeout', async () => {
    await fetchList({ current: 1, size: 20 })

    const config = request.mock.calls[0][0]
    expect(config.timeout).toBeUndefined()
  })
})
