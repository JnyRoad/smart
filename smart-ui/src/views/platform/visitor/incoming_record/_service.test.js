import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: cfg => request(cfg) }))

const api = await import('./_service')

describe('incoming_record _service 请求签名契约', () => {
  beforeEach(() => request.mockClear())

  it('revoke → POST 管理端作废路径，申请单 ID 走 body', () => {
    expect(typeof api.xcIncomingRecordApi.revoke).toBe('function')

    api.xcIncomingRecordApi.revoke({ id: 3201 })

    expect(request).toHaveBeenCalledWith({
      url: '/platform/manage/admittance/apply/revoke',
      method: 'post',
      data: { id: 3201 }
    })
  })

  it('手动授权选项查询使用受保护的申请单路径和 applyId 查询参数', () => {
    expect(typeof api.xcIncomingRecordApi.getManualAuthOptions).toBe('function')

    api.xcIncomingRecordApi.getManualAuthOptions('101')

    expect(request).toHaveBeenCalledWith({
      url: '/platform/manage/admittance/apply/device/auth/options',
      method: 'get',
      params: { applyId: '101' }
    })
  })

  it('手动授权提交只把后端契约载荷放入 body', () => {
    expect(typeof api.xcIncomingRecordApi.submitManualAuth).toBe('function')

    api.xcIncomingRecordApi.submitManualAuth({
      applyId: '101',
      fellowId: '201',
      authIds: [401]
    })

    expect(request).toHaveBeenCalledWith({
      url: '/platform/manage/admittance/apply/device/auth',
      method: 'post',
      data: {
        applyId: '101',
        fellowId: '201',
        authIds: [401]
      }
    })
  })

  it('人员授权只保留 ISC 类型 1 的权限组，并保留涉密项供界面解释后禁选', () => {
    expect(typeof api.filterManualAuthAuthorities).toBe('function')

    const authorities = [
      { id: 401, authorityName: '人员公共权限', type: 1, areaType: 0 },
      { id: 402, authorityName: '人员涉密权限', type: 1, areaType: 1 },
      { id: 404, authorityName: '访客类型权限', type: 2, areaType: 0 },
      { id: 403, authorityName: '车辆公共权限', type: 3, areaType: 0 }
    ]

    expect(api.filterManualAuthAuthorities(authorities)).toEqual([
      authorities[0],
      authorities[1]
    ])
  })

  it('手动授权载荷只包含申请单、人员身份和权限 ID，不带日期或员工字段', () => {
    expect(typeof api.buildManualAuthPayload).toBe('function')

    expect(api.buildManualAuthPayload({
      applyId: '101',
      fellowId: '201',
      authIds: [401, 402],
      startTime: '2026-09-05 08:00:00',
      endTime: '2026-09-06 18:00:00',
      staffId: 'should-not-be-sent'
    })).toEqual({
      applyId: '101',
      fellowId: '201',
      authIds: [401, 402]
    })
  })
})
