import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: { data: [] } }))
vi.mock('@/router/axios', () => ({ default: config => request(config) }))

const api = await import('./personnel_manage')

describe('api/platform/basic/personnel_manage 员工最小搜索契约', () => {
  beforeEach(() => request.mockClear())

  it('通过认证的最小人员查询端点按工号搜索', async () => {
    await api.getSearchStaff({ badge: 'A100' })

    expect(request).toHaveBeenCalledWith({
      url: '/platform/staff/lookup',
      method: 'get',
      params: { badge: 'A100' }
    })
  })

  it('搜索选项只保留允许的最小字段', () => {
    expect(api.normalizeLookup).toBeTypeOf('function')
    expect(api.normalizeLookup({
      staffId: 1,
      badge: 'A100',
      name: '测试员工',
      departmentName: '生产',
      certno: '不得保留',
      phone: '不得保留'
    })).toEqual({
      id: 1,
      badge: 'A100',
      name: '测试员工',
      departmentName: '生产'
    })
  })

  it('响应中的搜索结果也会投影为最小字段', async () => {
    request.mockResolvedValueOnce({
      data: {
        data: [{
          staffId: 1,
          badge: 'A100',
          name: '测试员工',
          departmentName: '生产',
          certno: '不得保留',
          phone: '不得保留'
        }]
      }
    })

    const response = await api.getSearchStaff({ badge: 'A100' })

    expect(response.data.data).toEqual([{
      id: 1,
      badge: 'A100',
      name: '测试员工',
      departmentName: '生产'
    }])
  })

  it('批量离职查询不再传递 compId，并只保留操作确认字段', async () => {
    request.mockResolvedValueOnce({
      data: {
        data: [{
          staffId: 1,
          badge: 'A100',
          name: '测试员工',
          certno: '不得保留',
          phone: '不得保留'
        }]
      }
    })

    const response = await api.getTemporaryStaffByBadgeBatch({ badges: 'A100', compId: '伪造值' })

    expect(request).toHaveBeenCalledWith({
      url: '/platform/staff/admin/temporary/by-badges',
      method: 'get',
      params: { badges: 'A100' }
    })
    expect(response.data.data).toEqual([{ id: 1, badge: 'A100', name: '测试员工' }])
  })

  it('临时人员列表改走受权限保护的最小资料接口，并剔除个人敏感字段', async () => {
    request.mockResolvedValueOnce({
      data: {
        data: {
          records: [{
            staffId: 1,
            badge: 'A100',
            name: '测试员工',
            sex: 1,
            jobName: '操作员',
            certno: '不得保留',
            phone: '不得保留',
            faceImg: '不得保留'
          }],
          total: 1
        }
      }
    })

    const response = await api.getStaffPage({ current: 1, size: 10 }, { badge: 'A100', status: 1 })

    expect(request).toHaveBeenCalledWith({
      url: '/platform/staff/admin/temporary/page',
      method: 'post',
      data: { current: 1, size: 10, badge: 'A100' }
    })
    expect(response.data.data.records).toEqual([{
      id: 1,
      badge: 'A100',
      name: '测试员工',
      sex: 1,
      jobName: '操作员'
    }])
  })
})
