import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn()

vi.mock('@/router/axios', () => ({ default: config => request(config) }))

// 列表页依赖全局列表混入；此处只保留本任务需要的最小运行环境。
globalThis.tce = { mixins: { list: {} } }

const component = (await import('./index.vue')).default

function createContext() {
  const context = Object.assign(component.data(), {
    $message: vi.fn(),
    refresh: vi.fn()
  })
  ;[
    'handleSend',
    'isDispatchPending',
    'startDispatchProgressPolling',
    'pollDispatchProgress',
    'stopDispatchProgressPolling',
    'isDispatchTerminal',
    'applyDispatchProgress'
  ].forEach(name => {
    context[name] = (...args) => component.methods[name].apply(context, args)
  })
  return context
}

async function flushPromises() {
  await Promise.resolve()
  await Promise.resolve()
}

describe('保密门禁手动下发流程', () => {
  beforeEach(() => {
    request.mockReset()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('仅在 HTTP 202 时把命令视为已受理并开始轮询', async () => {
    request.mockResolvedValueOnce({
      status: 202,
      data: { code: 0, data: { batchId: 7001, acceptedCount: 12 } }
    })
    request.mockResolvedValueOnce({
      status: 200,
      data: {
        code: 0,
        data: {
          batchId: 7001,
          totalCount: 12,
          waitingCount: 12,
          inWorkCount: 0,
          successCount: 0,
          failCount: 0,
          canceledCount: 0
        }
      }
    })
    const context = createContext()

    await context.handleSend({ id: 88 })

    expect(request).toHaveBeenCalledWith({
      url: '/platform/security/auth/apply/88/dispatch',
      method: 'post'
    })
    expect(context.$message).toHaveBeenCalledWith({
      message: '已受理，正在下发（批次 7001）',
      type: 'success'
    })
    expect(context.refresh).toHaveBeenCalledOnce()
    expect(context.dispatchingIds[88]).toBe(false)
    expect(context.activeDispatch).toEqual({ applyId: 88, batchId: 7001 })
  })

  it('命令受理期间禁止同一申请重复点击', async () => {
    let resolveRequest
    request.mockReturnValue(new Promise(resolve => {
      resolveRequest = resolve
    }))
    const context = createContext()
    const row = { id: 88 }

    const firstSubmit = context.handleSend(row)
    await flushPromises()

    expect(context.isDispatchPending(88)).toBe(true)
    await context.handleSend(row)
    expect(request).toHaveBeenCalledTimes(1)

    resolveRequest({
      status: 202,
      data: { code: 0, data: { batchId: 7001, acceptedCount: 12 } }
    })
    await firstSubmit
  })

  it('当前批次全部终态后停止轮询', async () => {
    request.mockResolvedValue({
      status: 200,
      data: {
        code: 0,
        data: {
          batchId: 7001,
          totalCount: 3,
          waitingCount: 0,
          inWorkCount: 0,
          successCount: 3,
          failCount: 0
        }
      }
    })
    const context = createContext()

    await context.startDispatchProgressPolling(88, 7001)

    expect(context.dispatchPollingTimer).toBeNull()
    expect(context.activeDispatch).toBeNull()
    expect(request).toHaveBeenCalledWith({
      url: '/platform/security/auth/apply/88/dispatch/7001',
      method: 'get'
    })
  })
})
