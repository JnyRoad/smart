import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { sendSmsCode } from './api'

const fetchMock = vi.fn()

function jsonResponse(body: unknown) {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
  })
}

beforeEach(() => {
  localStorage.clear()
  fetchMock.mockReset()
  fetchMock.mockResolvedValue(jsonResponse({ code: 0, message: 'ok' }))
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('auth api', () => {
  it('sends login SMS code as POST JSON without requiring an existing token', async () => {
    await sendSmsCode('13700001234')

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toBe('/app/sms/login/send')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body as string)).toEqual({ mobile: '13700001234' })
    expect((init.headers as Record<string, string>).Authorization).toBeUndefined()
  })
})
