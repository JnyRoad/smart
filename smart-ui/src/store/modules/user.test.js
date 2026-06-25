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

import { GetMenu as requestMenu } from '@/api/admin/menu'
import user from './user'

const commit = vi.fn()
const pendingMarker = { status: 'pending' }

function getSettledStatus (promise) {
  return Promise.race([
    promise.then(
      value => ({ status: 'resolved', value }),
      reason => ({ status: 'rejected', reason })
    ),
    new Promise(resolve => setTimeout(() => resolve(pendingMarker), 0))
  ])
}

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

describe('GetMenu action', () => {
  it('commits normalized menu data on success', async () => {
    const menu = [{
      path: '/platform',
      children: [{
        path: 'dashboard',
        children: []
      }]
    }]
    requestMenu.mockResolvedValueOnce({ data: { data: menu } })

    const result = await user.actions.GetMenu({ commit })

    expect(result).toBe(menu)
    expect(menu[0].children[0].path).toBe('/platform/dashboard')
    expect(commit).toHaveBeenCalledWith('SET_MENU', menu)
  })

  it('rejects and leaves menu state untouched when the menu request fails', async () => {
    const error = new Error('menu request failed')
    requestMenu.mockRejectedValueOnce(error)

    const settled = await getSettledStatus(user.actions.GetMenu({ commit }))
    expect(settled.status).toBe('rejected')
    expect(settled.reason).toBe(error)
    expect(commit).not.toHaveBeenCalledWith('SET_MENU', expect.anything())
  })
})
