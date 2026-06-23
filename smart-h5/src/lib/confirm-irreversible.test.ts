import { describe, expect, it, vi } from 'vitest'

// 用一个可控的桩替换 antd-mobile 的 Dialog.confirm，断言行为而非实现细节。
const confirmMock = vi.fn()
vi.mock('antd-mobile', () => ({
  Dialog: { confirm: (...args: unknown[]) => confirmMock(...args) },
}))

import { confirmIrreversible } from './confirm-irreversible'

describe('confirmIrreversible', () => {
  it('用户确认时返回 true', async () => {
    confirmMock.mockResolvedValueOnce(true)
    await expect(confirmIrreversible('确定拒绝该申请？拒绝后不可撤销')).resolves.toBe(true)
  })

  it('用户取消时返回 false', async () => {
    confirmMock.mockResolvedValueOnce(false)
    await expect(confirmIrreversible('确定拒绝该申请？拒绝后不可撤销')).resolves.toBe(false)
  })

  it('把文案作为 content 传给 Dialog.confirm，并带确定/取消按钮', async () => {
    confirmMock.mockResolvedValueOnce(true)
    await confirmIrreversible('确定确认放行？放行后不可撤销')
    expect(confirmMock).toHaveBeenCalledWith({
      content: '确定确认放行？放行后不可撤销',
      confirmText: '确定',
      cancelText: '取消',
    })
  })
})
