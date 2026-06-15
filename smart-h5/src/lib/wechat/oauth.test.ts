import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest'
import { buildWechatOAuthUrl, consumeWechatOAuthState, WX_OAUTH_STATE_KEY } from './oauth'

beforeEach(() => {
  sessionStorage.clear()
  vi.restoreAllMocks()
})

afterEach(() => {
  sessionStorage.clear()
  vi.restoreAllMocks()
})

describe('buildWechatOAuthUrl', () => {
  test('生成加密随机 state 并写入 sessionStorage，URL 使用同一 state', () => {
    const url = buildWechatOAuthUrl('/login/wechat/callback')

    const stored = sessionStorage.getItem(WX_OAUTH_STATE_KEY)
    expect(stored).toBeTruthy()
    // state 必须随机、足够长，绝不能再是硬编码的 123。
    expect(stored).not.toBe('123')
    expect((stored ?? '').length).toBeGreaterThanOrEqual(16)
    // URL 携带的 state 必须等于持久化的 state，回调才能比对。
    expect(url).toContain(`&state=${stored}`)
  })

  test('每次跳转生成不同的 state', () => {
    buildWechatOAuthUrl('/login/wechat/callback')
    const first = sessionStorage.getItem(WX_OAUTH_STATE_KEY)
    buildWechatOAuthUrl('/login/wechat/callback')
    const second = sessionStorage.getItem(WX_OAUTH_STATE_KEY)

    expect(first).toBeTruthy()
    expect(second).toBeTruthy()
    expect(first).not.toBe(second)
  })
})

describe('consumeWechatOAuthState', () => {
  test('state 与持久化值一致时校验通过，并清除已用 state', () => {
    buildWechatOAuthUrl('/login/wechat/callback')
    const stored = sessionStorage.getItem(WX_OAUTH_STATE_KEY)

    expect(consumeWechatOAuthState(stored)).toBe(true)
    // 一次性使用：成功后必须清除，防止重放。
    expect(sessionStorage.getItem(WX_OAUTH_STATE_KEY)).toBeNull()
  })

  test('state 不一致时拒绝，并清除 state（fail closed）', () => {
    buildWechatOAuthUrl('/login/wechat/callback')

    expect(consumeWechatOAuthState('forged-state')).toBe(false)
    // 失败也要清除，避免攻击者多次尝试。
    expect(sessionStorage.getItem(WX_OAUTH_STATE_KEY)).toBeNull()
  })

  test('URL 缺少 state 时拒绝（fail closed）', () => {
    buildWechatOAuthUrl('/login/wechat/callback')

    expect(consumeWechatOAuthState(null)).toBe(false)
    expect(sessionStorage.getItem(WX_OAUTH_STATE_KEY)).toBeNull()
  })

  test('sessionStorage 中没有 state 时拒绝（fail closed）', () => {
    // 未发起过授权 / state 已丢失，任何回调都应被拒。
    expect(consumeWechatOAuthState('whatever')).toBe(false)
  })

  test('sessionStorage 不可用时拒绝（fail closed）', () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('sessionStorage disabled')
    })

    expect(consumeWechatOAuthState('whatever')).toBe(false)
  })
})
