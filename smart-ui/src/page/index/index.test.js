import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const getScreen = vi.fn(() => 'desktop')

vi.mock('./tags', () => ({ default: { name: 'tags', render: createElement => createElement('div') } }))
vi.mock('./top/', () => ({ default: { name: 'top', render: createElement => createElement('div') } }))
vi.mock('./sidebar/', () => ({ default: { name: 'sidebar', render: createElement => createElement('div') } }))
vi.mock('@/util/admin', () => ({ default: { getScreen } }))
vi.mock('@/util/validate', () => ({ validatenull: vi.fn(value => value === undefined || value === null || value === '') }))
vi.mock('@/util/date.js', () => ({ calcDate: vi.fn() }))
vi.mock('@/util/store.js', () => ({ getStore: vi.fn() }))
vi.mock('stompjs', () => ({ default: { over: vi.fn() } }))
vi.mock('@/store', () => ({ default: { getters: { access_token: '' } } }))

const component = (await import('./index.vue')).default

const originalOnResize = window.onresize

function createVm () {
  return {
    $store: {
      commit: vi.fn()
    },
    previousOnResize: null,
    refreshTime: null,
    resizeHandler: null
  }
}

describe('page index resize lifecycle', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    window.onresize = originalOnResize
  })

  afterEach(() => {
    window.onresize = originalOnResize
  })

  it('restores the previous window resize handler when destroyed', () => {
    const previousResize = vi.fn()
    const vm = createVm()
    window.onresize = previousResize

    component.methods.init.call(vm)
    expect(window.onresize).not.toBe(previousResize)

    component.destroyed.call(vm)

    expect(window.onresize).toBe(previousResize)
  })

  it('does not remove another resize handler registered after init', () => {
    const previousResize = vi.fn()
    const nextResize = vi.fn()
    const vm = createVm()
    window.onresize = previousResize

    component.methods.init.call(vm)
    window.onresize = nextResize

    component.destroyed.call(vm)

    expect(window.onresize).toBe(nextResize)
  })
})
