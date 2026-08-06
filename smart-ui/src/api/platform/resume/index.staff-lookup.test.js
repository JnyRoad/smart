import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn()
vi.mock('@/router/axios', () => ({ default: config => request(config) }))

const api = await import('./index')

describe('api/platform/resume 亲属任职员工查询契约', () => {
  beforeEach(() => request.mockReset())

  it('先使用受控工号检索，再按返回主键读取最小管理员详情', async () => {
    request
      .mockResolvedValueOnce({ data: { data: [{ staffId: 42, badge: 'A100' }] } })
      .mockResolvedValueOnce({
        data: {
          data: {
            staffId: 42,
            badge: 'A100',
            name: '测试员工',
            sex: '1',
            companyName: '许昌园区',
            departmentName: '生产部',
            jobName: '操作员',
            certno: '不得保留',
            phone: '不得保留'
          }
        }
      })

    const response = await api.selectStaffInfo(' A100 ')

    expect(request).toHaveBeenNthCalledWith(1, {
      url: '/platform/staff/lookup',
      method: 'get',
      params: { badge: 'A100' }
    })
    expect(request).toHaveBeenNthCalledWith(2, {
      url: '/platform/staff/admin/42',
      method: 'get'
    })
    expect(response.data.data.smtStaff).toEqual({
      badge: 'A100',
      name: '测试员工',
      sex: '1',
      compName: '许昌园区',
      depName: '生产部',
      jobName: '操作员'
    })
  })

  it('无精确工号匹配时不读取详情', async () => {
    request.mockResolvedValueOnce({ data: { data: [{ staffId: 42, badge: 'A1001' }] } })

    const response = await api.selectStaffInfo('A100')

    expect(request).toHaveBeenCalledTimes(1)
    expect(response.data.data.smtStaff).toBeNull()
  })
})
