import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: { data: { records: [] } } }))
vi.mock('@/router/axios', () => ({ default: config => request(config) }))

const api = await import('./staff_info')

describe('api/platform/basic/staff_info 后台员工最小分页契约', () => {
  beforeEach(() => request.mockClear())

  it('不再调用旧 PII 分页端点，并剔除调用方伪造的园区、BU 和敏感查询字段', async () => {
    await api.fetchList({
      current: 2,
      size: 20,
      badge: 'A100',
      hasFace: true,
      phone: '13800000000',
      certno: '不得传递',
      facePicId: '文件标识不得传递',
      parkId: 999,
      compId: '越权 BU'
    })

    expect(request).toHaveBeenCalledWith({
      url: '/platform/staff/admin/page',
      method: 'post',
      params: { current: 2, size: 20 },
      data: {
        name: undefined,
        badge: 'A100',
        badges: undefined,
        depId: undefined,
        depAbbr: undefined,
        jobId: undefined,
        jobName: undefined,
        jcheId: undefined,
        status: undefined,
        hasFace: true,
        startTime: undefined,
        endTime: undefined
      }
    })
  })

  it('即使服务端意外携带 PII，页面状态也只接收最小展示字段', async () => {
    request.mockResolvedValueOnce({
      data: {
        data: {
          records: [{
            staffId: 1,
            badge: 'A100',
            name: '测试员工',
            compName: '测试 BU',
            hasFace: true,
            certno: '不得保留',
            phone: '不得保留',
            facePicId: '不得保留'
          }]
        }
      }
    })

    const response = await api.fetchList({ current: 1, size: 10 })

    expect(response.data.data.records).toEqual([{
      id: 1,
      badge: 'A100',
      name: '测试员工',
      compName: '测试 BU',
      depAbbr: undefined,
      depName: undefined,
      jcheName: undefined,
      jobName: undefined,
      createTime: undefined,
      status: undefined,
      parkName: undefined,
      hasFace: true,
      deviceAuth: undefined,
      appAuth: undefined
    }])
  })
})
