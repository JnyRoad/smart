import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: config => request(config) }))

const api = await import('./staff_info_detail')

describe('api/platform/basic/staff_info_detail 受控详情契约', () => {
  beforeEach(() => request.mockClear())

  it('通过受控管理员详情端点按 staffId 查询', () => {
    api.getAdminStaffDetail(42)

    expect(request).toHaveBeenCalledWith({
      url: '/platform/staff/admin/42',
      method: 'get'
    })
  })

  it('不再导出返回完整员工实体或修改手机号的旧 helper', () => {
    expect(api.getById).toBeUndefined()
    expect(api.editPhone).toBeUndefined()
  })
})
