import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const component = (await import('./index.vue')).default

function createVm () {
  return {
    time: '',
    taskInterval: null,
    getTime: vi.fn()
  }
}

describe('platform panel timer lifecycle', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('stores and clears the clock interval during the component lifecycle', () => {
    const vm = createVm()

    component.created.call(vm)

    expect(vm.getTime).toHaveBeenCalledTimes(1)
    expect(vm.taskInterval).not.toBe(null)

    vi.advanceTimersByTime(1000)
    expect(vm.getTime).toHaveBeenCalledTimes(2)

    component.beforeDestroy.call(vm)

    expect(vm.taskInterval).toBe(null)
    vi.advanceTimersByTime(1000)
    expect(vm.getTime).toHaveBeenCalledTimes(2)
  })
})
