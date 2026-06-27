import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { clearPanelRefresh, schedulePanelRefresh } from './panel-refresh'

function createPanelVm(path) {
  return {
    $route: { path },
    timeOut: undefined
  }
}

describe('panel refresh timer lifecycle', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('clears the previous refresh timeout before scheduling a new one', () => {
    const vm = createPanelVm('/platform/panel/bigdata')
    const refresh = vi.fn()

    schedulePanelRefresh(vm, '/platform/panel/bigdata', refresh, 1000)
    const firstTimeout = vm.timeOut
    schedulePanelRefresh(vm, '/platform/panel/bigdata', refresh, 1000)

    expect(vm.timeOut).not.toBe(firstTimeout)
    vi.advanceTimersByTime(1000)
    expect(refresh).toHaveBeenCalledTimes(1)
  })

  it('clears an existing refresh timeout when the route no longer matches', () => {
    const vm = createPanelVm('/platform/panel/bigdata')
    const refresh = vi.fn()

    schedulePanelRefresh(vm, '/platform/panel/bigdata', refresh, 1000)
    vm.$route.path = '/platform/panel/accessto'
    schedulePanelRefresh(vm, '/platform/panel/bigdata', refresh, 1000)

    expect(vm.timeOut).toBeUndefined()
    vi.advanceTimersByTime(1000)
    expect(refresh).not.toHaveBeenCalled()
  })

  it('clears and forgets the stored refresh timeout', () => {
    const vm = createPanelVm('/platform/panel/accessto')
    const refresh = vi.fn()

    schedulePanelRefresh(vm, '/platform/panel/accessto', refresh, 1000)
    clearPanelRefresh(vm)

    expect(vm.timeOut).toBeUndefined()
    vi.advanceTimersByTime(1000)
    expect(refresh).not.toHaveBeenCalled()
  })
})
