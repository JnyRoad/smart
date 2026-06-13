import { beforeEach, describe, expect, it } from 'vitest'
import {
  INVALID_TOKEN,
  clearSession,
  getAccessToken,
  hasValidToken,
  markTokenInvalid,
  saveSession,
} from './token'

beforeEach(() => {
  localStorage.clear()
})

describe('token storage (legacy xc- envelope compatible)', () => {
  it('saves access token in the legacy envelope format', () => {
    saveSession({ accessToken: 'tok-1', refreshToken: 'ref-1', expiresIn: 3600 })
    const raw = localStorage.getItem('xc-access_token')
    expect(raw).not.toBeNull()
    const envelope = JSON.parse(raw as string)
    expect(envelope.dataType).toBe('string')
    expect(envelope.content).toBe('tok-1')
    expect(JSON.parse(localStorage.getItem('xc-refresh_token') as string).content).toBe('ref-1')
  })

  it('reads a token written by the legacy app', () => {
    localStorage.setItem(
      'xc-access_token',
      JSON.stringify({ dataType: 'string', content: 'legacy-tok', datetime: 123 }),
    )
    expect(getAccessToken()).toBe('legacy-tok')
    expect(hasValidToken()).toBe(true)
  })

  it('treats missing or invalid_token as not logged in', () => {
    expect(getAccessToken()).toBeNull()
    expect(hasValidToken()).toBe(false)
    markTokenInvalid()
    expect(getAccessToken()).toBe(INVALID_TOKEN)
    expect(hasValidToken()).toBe(false)
  })

  it('clearSession removes all session keys', () => {
    saveSession({ accessToken: 'tok', refreshToken: 'ref', expiresIn: 60 })
    clearSession()
    expect(localStorage.getItem('xc-access_token')).toBeNull()
    expect(localStorage.getItem('xc-refresh_token')).toBeNull()
    expect(localStorage.getItem('xc-expires_in')).toBeNull()
  })
})
