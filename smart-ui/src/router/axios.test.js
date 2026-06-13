/**
 * Characterization + behavior tests for the axios response interceptor (CR-C14-002).
 *
 * axios is a webpack external (global script tag in public/index.html), so the
 * module under test references the bare global `axios`. We install the real
 * axios package as that global before dynamically importing the module.
 */
import { describe, it, expect, vi, beforeAll, beforeEach } from 'vitest'
import { Message } from 'element-ui'
import store from '@/store'
import router from '@/router/router'

vi.mock('element-ui', () => ({ Message: vi.fn() }))
vi.mock('@/store', () => ({
  default: { getters: { access_token: '' }, dispatch: vi.fn(() => Promise.resolve()) }
}))
vi.mock('@/router/router', () => ({ default: { push: vi.fn() } }))
vi.mock('nprogress', () => ({
  default: { start: vi.fn(), done: vi.fn(), configure: vi.fn() }
}))
vi.mock('@/util/util', () => ({ serialize: vi.fn((data) => data) }))
vi.mock('@/util/store', () => ({ getStore: vi.fn(), setStore: vi.fn() }))

let onResponse
let onResponseError

beforeAll(async () => {
  window.axios = (await import('axios')).default
  await import('./axios')
  const handler = window.axios.interceptors.response.handlers[0]
  onResponse = handler.fulfilled
  onResponseError = handler.rejected
})

beforeEach(() => {
  vi.clearAllMocks()
  localStorage.clear()
})

function makeRes(status, data) {
  return { status, data, config: {}, headers: {} }
}

describe('response interceptor: success', () => {
  it('returns the response untouched for status 200 and business code 0', () => {
    const res = makeRes(200, { code: 0, data: [1, 2] })
    expect(onResponse(res)).toBe(res)
    expect(Message).not.toHaveBeenCalled()
  })
})

describe('response interceptor: business failure (code === 1)', () => {
  it('shows an error Message with the backend msg', () => {
    const res = makeRes(200, { code: 1, msg: '余额不足' })
    Promise.resolve(onResponse(res)).catch(error => { console.error(error) })
    expect(Message).toHaveBeenCalledWith({ message: '余额不足', type: 'error' })
  })

  it('swallows the failure and resolves with the response by default (legacy behavior)', () => {
    const res = makeRes(200, { code: 1, msg: '余额不足' })
    expect(onResponse(res)).toBe(res)
  })

  it('also treats non-200/non-401 http status as business failure', () => {
    const res = makeRes(500, { msg: '服务器错误' })
    Promise.resolve(onResponse(res)).catch(error => { console.error(error) })
    expect(Message).toHaveBeenCalledWith({ message: '服务器错误', type: 'error' })
  })
})

describe('response interceptor: 401', () => {
  it('logs out and redirects to /login, returning a rejection', async () => {
    const res = makeRes(401, { msg: '认证失败' })
    const result = onResponse(res)
    await expect(result).rejects.toThrow('认证失败')
    expect(store.dispatch).toHaveBeenCalledWith('FedLogOut')
    await Promise.resolve()
    expect(router.push).toHaveBeenCalledWith({ path: '/login' })
  })

  it('redirects weak-password users to /editPwd and persists isStrongPwd=false', async () => {
    const res = makeRes(401, { msg: 'isStrongPwd:false' })
    await expect(onResponse(res)).rejects.toThrow()
    expect(localStorage.getItem('isStrongPwd')).toBe('false')
    expect(router.push).toHaveBeenCalledWith({ path: '/editPwd', query: { loginTo: true } })
    expect(store.dispatch).not.toHaveBeenCalled()
  })
})

describe('response interceptor: network error', () => {
  it('rejects with an Error', async () => {
    await expect(onResponseError(new Error('Network Error'))).rejects.toBeInstanceOf(Error)
  })
})

describe('strict reject switch (SMART_UI_STRICT_REJECT)', () => {
  it('rejects business failures with Error(message) when the switch is on', async () => {
    localStorage.setItem('SMART_UI_STRICT_REJECT', 'true')
    const res = makeRes(200, { code: 1, msg: '余额不足' })
    await expect(onResponse(res)).rejects.toThrow('余额不足')
    expect(Message).toHaveBeenCalledWith({ message: '余额不足', type: 'error' })
  })

  it('rejects non-200 http status when the switch is on', async () => {
    localStorage.setItem('SMART_UI_STRICT_REJECT', 'true')
    await expect(onResponse(makeRes(500, { msg: '服务器错误' }))).rejects.toThrow('服务器错误')
  })

  it('keeps the legacy swallow behavior when the switch is absent', () => {
    const res = makeRes(200, { code: 1, msg: '余额不足' })
    expect(onResponse(res)).toBe(res)
  })

  it('keeps the legacy swallow behavior for any value other than "true"', () => {
    localStorage.setItem('SMART_UI_STRICT_REJECT', 'false')
    const res = makeRes(500, { msg: '服务器错误' })
    expect(onResponse(res)).toBe(res)
  })

  it('does not affect successful responses when the switch is on', () => {
    localStorage.setItem('SMART_UI_STRICT_REJECT', 'true')
    const res = makeRes(200, { code: 0, data: [] })
    expect(onResponse(res)).toBe(res)
  })
})

describe('defaults', () => {
  it('uses a 30s global timeout', () => {
    expect(window.axios.defaults.timeout).toBe(30000)
  })
})

describe('isStrongPwd side effect', () => {
  it('no longer touches isStrongPwd on normal responses', () => {
    onResponse(makeRes(200, { code: 0 }))
    expect(localStorage.getItem('isStrongPwd')).toBe(null)
  })

  it('still persists isStrongPwd=false on the weak-password 401', async () => {
    await expect(onResponse(makeRes(401, { msg: 'isStrongPwd:false' }))).rejects.toThrow()
    expect(localStorage.getItem('isStrongPwd')).toBe('false')
  })
})
