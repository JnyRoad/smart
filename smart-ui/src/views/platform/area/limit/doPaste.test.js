import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => {
  globalThis.tce = { mixins: { executeOnce: {} } }
  return { batchAdd: vi.fn() }
})

vi.mock('@/api/platform/area/limit', () => ({
  batchAdd: mocks.batchAdd
}))

import DoPasteDialog from './doPaste.vue'

function createPasteContext(dateRange) {
  return {
    addData: { authId: 2001, type: 1 },
    addform: { badges: '1001\n1002', dateRange },
    btnLoading: false,
    validateForm: vi.fn().mockResolvedValue(true),
    close: vi.fn(),
    $emit: vi.fn(),
    $message: { warning: vi.fn() },
    $notify: vi.fn()
  }
}

describe('权限组人员批量下发有效期窗口', () => {
  beforeEach(() => {
    mocks.batchAdd.mockReset()
  })

  it('提交合法日期时将日期与人员工号一并传给共享批量接口', async () => {
    const context = createPasteContext(['2026-09-03', '2026-09-05'])
    mocks.batchAdd.mockResolvedValue({ data: { code: 0, data: [] } })

    await DoPasteDialog.methods.addSubmit.call(context)

    expect(mocks.batchAdd).toHaveBeenCalledWith({
      authId: 2001,
      type: 1,
      badges: ['1001', '1002'],
      startTime: '2026-09-03',
      endTime: '2026-09-05'
    })
  })

  it('日期倒置时阻止批量接口调用', async () => {
    const context = createPasteContext(['2026-09-05', '2026-09-03'])

    await DoPasteDialog.methods.addSubmit.call(context)

    expect(context.$message.warning).toHaveBeenCalledWith('权限结束日期不能早于开始日期')
    expect(mocks.batchAdd).not.toHaveBeenCalled()
  })

  it('批量接口异常时恢复保存按钮状态', async () => {
    const context = createPasteContext(['2026-09-03', '2026-09-05'])
    mocks.batchAdd.mockRejectedValue(new Error('网络异常'))

    await expect(DoPasteDialog.methods.addSubmit.call(context)).resolves.toBeUndefined()

    expect(context.btnLoading).toBe(false)
    expect(context.$notify).toHaveBeenCalledWith({
      title: '失败',
      message: '添加失败，请稍后重试',
      type: 'error',
      duration: 5000
    })
  })
})
