import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: (config) => request(config) }))

const api = await import('./personnel_manage')

describe('临时人员保存参数', () => {
  beforeEach(() => request.mockClear())

  it('提交临时人员时清除全部文本字段的首尾空格', () => {
    api.postAddStaff({
      badge: ' HC0460 ',
      certno: ' 411082200603033070 ',
      depName: ' 河南汇创 ',
      jcheId: ' 8 ',
      jcheName: ' 技工层 ',
      jobName: ' 钳工 ',
      name: ' 李思翔 ',
      phone: ' 13700893346 ',
      entryTime: ' 2026-07-21 18:39:36 ',
      dispatch: ' 外包 ',
      sex: 0,
      status: 4
    })

    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: '/platform/staff/addTempStaff',
      data: expect.objectContaining({
        badge: 'HC0460',
        certno: '411082200603033070',
        depName: '河南汇创',
        jcheId: '8',
        jcheName: '技工层',
        jobName: '钳工',
        name: '李思翔',
        phone: '13700893346',
        entryTime: '2026-07-21 18:39:36',
        dispatch: '外包'
      })
    }))
  })

  it('批量导入临时人员时清除全部文本字段的首尾空格', () => {
    api.postImportStaff([{
      jobNumber: ' HC0460 ',
      identity: ' 411082200603033070 ',
      department: ' 河南汇创 ',
      rank: ' 技工层 ',
      post: ' 钳工 ',
      name: ' 李思翔 ',
      phone: ' 13700893346 ',
      entryTime: ' 2026-07-21 ',
      dispatch: ' 外包 '
    }])

    expect(request).toHaveBeenCalledWith(expect.objectContaining({
      url: '/platform/staff/addBatchTempStaff',
      data: [{
        badge: 'HC0460',
        certno: '411082200603033070',
        depName: '河南汇创',
        jcheName: '技工层',
        jobName: '钳工',
        name: '李思翔',
        phone: '13700893346',
        entryTime: '2026-07-21',
        dispatch: '外包'
      }]
    }))
  })
})
