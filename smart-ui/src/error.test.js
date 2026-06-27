/**
 * Tests for global error capture (src/error.js): the unhandledrejection
 * listener added for the gated axios reject rollout (CR-C14-002) must log
 * to console and record into the vuex logs module without any popup.
 */
import { describe, it, expect, vi, beforeAll, beforeEach, afterEach } from 'vitest'
import store from '@/store'

vi.mock('@/store', () => ({ default: { commit: vi.fn() } }))

function dispatchUnhandledRejection(reason) {
  // jsdom has no PromiseRejectionEvent constructor; fake the shape.
  const event = new Event('unhandledrejection')
  event.reason = reason
  event.promise = Promise.resolve() // placeholder, never rejected
  window.dispatchEvent(event)
}

beforeAll(async () => {
  await import('./error')
})

let consoleError

beforeEach(() => {
  vi.clearAllMocks()
  consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
})

afterEach(() => {
  consoleError.mockRestore()
})

describe('unhandledrejection listener', () => {
  it('records Error reasons into the vuex logs module and console.errors them', () => {
    const reason = new Error('接口失败')
    dispatchUnhandledRejection(reason)
    expect(consoleError).toHaveBeenCalledWith('[unhandledrejection]', {
      type: 'error',
      message: '接口失败',
      stack: reason.stack,
      info: 'unhandledrejection'
    })
    expect(store.commit).toHaveBeenCalledWith('ADD_LOGS', {
      type: 'error',
      message: '接口失败',
      stack: reason.stack,
      info: 'unhandledrejection'
    })
  })

  it('handles non-Error reasons without crashing', () => {
    dispatchUnhandledRejection('cancel')
    expect(store.commit).toHaveBeenCalledWith('ADD_LOGS', {
      type: 'error',
      message: 'cancel',
      stack: undefined,
      info: 'unhandledrejection'
    })
  })
})
