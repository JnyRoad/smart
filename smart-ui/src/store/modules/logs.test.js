/**
 * ADD_LOGS must cap the persisted list: the unhandledrejection listener
 * (CR-C14-002) raises log volume, and an unbounded array re-persisted to
 * localStorage on every event would eventually blow the storage quota.
 */
import { describe, it, expect, vi } from 'vitest'

vi.mock('@/util/store', () => ({ getStore: vi.fn(), setStore: vi.fn() }))
vi.mock('@/filters/', () => ({ dateFormat: vi.fn(() => '2026-06-12 00:00:00') }))
vi.mock('@/api/admin/log', () => ({ sendLogs: vi.fn() }))

import logs from './logs'

describe('ADD_LOGS', () => {
  it('keeps at most the last 100 entries', () => {
    const state = { logsList: [] }
    for (let i = 0; i < 105; i++) {
      logs.mutations.ADD_LOGS(state, { type: 'error', message: `m${i}`, stack: '', info: 'x' })
    }
    expect(state.logsList.length).toBe(100)
    expect(state.logsList[99].message).toBe('m104')
    expect(state.logsList[0].message).toBe('m5')
  })
})
