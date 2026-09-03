import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => {
  globalThis.tce = { mixins: { executeOnce: {} } }
  return { upDeviceAuthList: vi.fn() }
})

vi.mock('@/api/platform/basic/staff_info', () => ({
  deviceAuthList: vi.fn(),
  editAppList: vi.fn(),
  fetchAppList: vi.fn(),
  fetchList: vi.fn(),
  getStaffImgInfo: vi.fn(),
  importImgs: vi.fn(),
  upDeviceAuthList: mocks.upDeviceAuthList
}))

vi.mock('@/api/platform/_publicService', () => ({
  getCompTree: vi.fn()
}))

// 组件只使用 isArrayFn；隔离 util 兼容入口的 axios/router 副作用，避免测试加载整套路由表。
vi.mock('@/util/util', () => ({
  isArrayFn: vi.fn()
}))

vi.mock('./doPasteBadge', () => ({ default: { name: 'DoPasteBadgeStub' } }))
vi.mock('./issueAuth', () => ({ default: { name: 'IssueAuthStub' } }))

import StaffInfo from './index.vue'

function createEntryContext(dateRange) {
  return {
    selectStaffs: [{ id: '1001' }],
    selectedAuth: [2001],
    entryForm: { dateRange },
    addType: 1,
    entryLoading: false,
    taskRecordStr: '',
    $message: { warning: vi.fn() },
    $notify: vi.fn(),
    $refs: { IssueAuthDialogs: { open: vi.fn() } }
  }
}

describe('员工通关权限有效期窗口', () => {
  beforeEach(() => {
    mocks.upDeviceAuthList.mockReset()
  })

  it('提交合法日期时将起止日期传给既有员工授权接口', () => {
    const context = createEntryContext(['2026-09-03', '2026-09-05'])
    mocks.upDeviceAuthList.mockResolvedValue({ data: { code: 0, data: 'task-1' } })

    StaffInfo.methods.entrySubmit.call(context)

    expect(mocks.upDeviceAuthList).toHaveBeenCalledWith({
      ids: ['1001'],
      deviceAuthIds: [2001],
      startTime: '2026-09-03',
      endTime: '2026-09-05'
    }, 1)
  })

  it('日期倒置时不调用员工授权接口', () => {
    const context = createEntryContext(['2026-09-05', '2026-09-03'])

    StaffInfo.methods.entrySubmit.call(context)

    expect(context.$message.warning).toHaveBeenCalledWith('权限结束日期不能早于开始日期')
    expect(mocks.upDeviceAuthList).not.toHaveBeenCalled()
  })
})
