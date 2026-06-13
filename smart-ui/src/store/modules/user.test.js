/**
 * The weak-password flag lifecycle (CR-C14-002): the interceptor only ever
 * writes isStrongPwd="false" now, so every successful login must clear the
 * stale flag or remediated users would loop on the /login guard forever.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/api/login', () => ({
  getUserInfo: vi.fn(),
  loginByMobile: vi.fn(() => Promise.resolve({ data: { access_token: 'tok' } })),
  loginBySocial: vi.fn(() => Promise.resolve({ data: { access_token: 'tok' } })),
  loginByUsername: vi.fn(() => Promise.resolve({ data: { access_token: 'tok' } })),
  logout: vi.fn(),
  refreshToken: vi.fn()
}))
vi.mock('@/api/admin/menu', () => ({ GetMenu: vi.fn() }))
vi.mock('@/util/security', () => ({ getSecurityEncodeKey: vi.fn(() => 'k') }))
vi.mock('@/util/util', () => ({
  deepClone: vi.fn(value => value),
  encryption: vi.fn(({ data }) => data)
}))

import user from './user'

const commit = vi.fn()

beforeEach(() => {
  vi.clearAllMocks()
  localStorage.clear()
})

describe('login actions clear a stale weak-password flag', () => {
  it.each([
    ['LoginByUsername', { username: 'u', password: 'p' }],
    ['LoginByPhone', { mobile: '1', code: 'c' }],
    ['LoginBySocial', { state: 's', code: 'c' }]
  ])('%s removes isStrongPwd on success', async (action, payload) => {
    localStorage.setItem('isStrongPwd', 'false')
    await user.actions[action]({ commit }, payload)
    expect(localStorage.getItem('isStrongPwd')).toBe(null)
  })
})
