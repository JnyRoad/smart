import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

// 复现的生产问题：宿舍水电导出接口稳定要 45-50 秒，而 axios 全局超时是 30 秒，
// 导出必然超时失败；download() 原来在发起请求后立刻同步 close()，失败时只有
// console.error，没有任何界面反馈，导出按钮看起来“卡住后毫无反应”。
// 这里锁定修复后的行为：请求期间显示 loading、失败时不关闭弹窗并弹出友好提示、
// 只有成功时才关闭弹窗。

const exportData = vi.fn()
vi.mock('../../_service', () => ({ exportData: (...args) => exportData(...args) }))
vi.mock('@/util/date', () => ({ dateFormat2: vi.fn(date => String(date)) }))
vi.mock('@/filters/index', () => ({ staffStatusInit: vi.fn(status => String(status)) }))

const component = (await import('./index.vue')).default

function createVm () {
  return {
    searchform: { parkId: 1, dormitoryIds: [5010481], meterMonth: '2026-06' },
    $refs: {},
    validateForm: () => Promise.resolve(),
    formatJson: component.methods.formatJson,
    close: vi.fn(),
    $message: { error: vi.fn() },
    dateFormat: component.methods.dateFormat,
    validatenull: value => value === undefined || value === null || value === ''
  }
}

describe('exportMeterData download()', () => {
  beforeEach(() => {
    exportData.mockReset()
    globalThis.require = Object.assign(
      vi.fn(path => {
        if (path === '@/vendor/Export2Excel') {
          return { export_json_to_excel: vi.fn() }
        }
        throw new Error(`unexpected require: ${path}`)
      }),
      { ensure: (deps, cb) => cb() }
    )
  })

  afterEach(() => {
    delete globalThis.require
  })

  it('shows a friendly error and keeps the dialog open when the export request fails', async () => {
    exportData.mockRejectedValue(new Error('timeout of 30000ms exceeded'))
    const vm = createVm()

    await component.methods.download.call(vm)

    expect(vm.$message.error).toHaveBeenCalledWith(expect.stringContaining('导出'))
    expect(vm.close).not.toHaveBeenCalled()
    expect(vm.exporting).toBe(false)
  })

  it('toggles the loading flag and only closes the dialog once the export succeeds', async () => {
    exportData.mockResolvedValue({ data: { data: [] } })
    const vm = createVm()

    const pending = component.methods.download.call(vm)
    await Promise.resolve() // download() 先 await validateForm()，这里让那一次微任务先跑完
    expect(vm.exporting).toBe(true)
    expect(vm.close).not.toHaveBeenCalled()

    await pending

    expect(vm.close).toHaveBeenCalledTimes(1)
    expect(vm.exporting).toBe(false)
  })
})
