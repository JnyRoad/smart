import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

// 与 index.vue 同一份 download() 逻辑复制出来的“部门分摊水电表”导出弹窗，锁定同样的修复行为：
// 请求期间显示 loading、失败时不关闭弹窗并弹出友好提示、只有成功时才关闭弹窗。

const exportShareData = vi.fn()
vi.mock('../../_service', () => ({ exportShareData: (...args) => exportShareData(...args) }))
vi.mock('@/util/date', () => ({ dateFormat2: vi.fn(date => String(date)) }))
vi.mock('@/filters/index', () => ({ staffStatusInit: vi.fn(status => String(status)) }))

const component = (await import('./exportShareData.vue')).default

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

describe('exportShareData download()', () => {
  beforeEach(() => {
    exportShareData.mockReset()
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
    exportShareData.mockRejectedValue(new Error('timeout of 30000ms exceeded'))
    const vm = createVm()

    await component.methods.download.call(vm)

    expect(vm.$message.error).toHaveBeenCalledWith(expect.stringContaining('导出'))
    expect(vm.close).not.toHaveBeenCalled()
    expect(vm.exporting).toBe(false)
  })

  it('toggles the loading flag and only closes the dialog once the export succeeds', async () => {
    exportShareData.mockResolvedValue({ data: { data: [] } })
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
