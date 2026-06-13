import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { saveSession } from '@/lib/auth/token'
import { ApiError, request, setInvalidTokenHandler } from './http'

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

const fetchMock = vi.fn()

beforeEach(() => {
  localStorage.clear()
  fetchMock.mockReset()
  fetchMock.mockResolvedValue(jsonResponse({ code: 0, data: 'ok' }))
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  vi.unstubAllGlobals()
  setInvalidTokenHandler(() => {})
})

describe('module prefix and query params', () => {
  it('prefixes the gateway module and serializes params', async () => {
    saveSession({ accessToken: 'tok' })
    await request({ module: 'app', url: '/common/weather', params: { city: '许昌' } })
    const [url] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toBe(`/app/common/weather?city=${encodeURIComponent('许昌')}`)
  })

  it('promotes POST pagination fields to the query string and keeps them in the body', async () => {
    saveSession({ accessToken: 'tok' })
    await request({
      module: 'app',
      url: '/approve/list/new/page',
      method: 'POST',
      data: { current: 2, size: 10, recordType: 3 },
    })
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toContain('current=2')
    expect(url).toContain('size=10')
    expect(JSON.parse(init.body as string)).toEqual({ current: 2, size: 10, recordType: 3 })
  })
})

describe('auth modes', () => {
  it('sends Bearer token by default', async () => {
    saveSession({ accessToken: 'tok-abc' })
    await request({ module: 'app', url: '/employee/baseinfo' })
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect((init.headers as Record<string, string>).Authorization).toBe('Bearer tok-abc')
  })

  it('sends Basic credentials for auth: basic', async () => {
    await request({ module: 'auth', url: '/wx/public/token', method: 'POST', auth: 'basic', data: { code: 'c' } })
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect((init.headers as Record<string, string>).Authorization).toMatch(/^Basic /)
  })

  it('omits Authorization for auth: none', async () => {
    await request({ module: 'app', url: '/wechat/xc/banging/badge', method: 'POST', auth: 'none', data: {} })
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect((init.headers as Record<string, string>).Authorization).toBeUndefined()
  })

  it('fails fast when bearer is required but no valid token exists', async () => {
    await expect(request({ module: 'app', url: '/employee/baseinfo' })).rejects.toThrow(ApiError)
    expect(fetchMock).not.toHaveBeenCalled()
  })
})

describe('response handling', () => {
  it('unescapes legacy HTML entities recursively', async () => {
    saveSession({ accessToken: 'tok' })
    fetchMock.mockResolvedValue(
      jsonResponse({ code: 0, data: { title: '&lt;b&gt;放行&#40;生活区&#41;&#39;', list: ['&gt;1'] } }),
    )
    const body = await request<{ data: { title: string; list: string[] } }>({
      module: 'app',
      url: '/home/bbs/list',
    })
    expect(body.data.title).toBe("<b>放行(生活区)'")
    expect(body.data.list[0]).toBe('>1')
  })

  it('invokes the invalid-token handler on code 401 + invalid_token', async () => {
    saveSession({ accessToken: 'tok' })
    fetchMock.mockResolvedValue(jsonResponse({ code: 401, msg: 'invalid_token' }))
    const handler = vi.fn()
    setInvalidTokenHandler(handler)
    await expect(request({ module: 'app', url: '/employee/baseinfo' })).rejects.toThrow(ApiError)
    expect(handler).toHaveBeenCalledOnce()
  })

  it('processes the body even on non-2xx status (legacy validateStatus semantics)', async () => {
    saveSession({ accessToken: 'tok' })
    fetchMock.mockResolvedValue(jsonResponse({ code: 1, message: 'boom' }, 400))
    const body = await request<{ code: number; message: string }>({
      module: 'app',
      url: '/employee/baseinfo',
    })
    expect(body).toEqual({ code: 1, message: 'boom' })
  })

  it('triggers the invalid-token handler when the gateway replies HTTP 401 + invalid_token body', async () => {
    saveSession({ accessToken: 'tok' })
    fetchMock.mockResolvedValue(jsonResponse({ code: 401, msg: 'invalid_token' }, 401))
    const handler = vi.fn()
    setInvalidTokenHandler(handler)
    await expect(request({ module: 'app', url: '/employee/baseinfo' })).rejects.toThrow(ApiError)
    expect(handler).toHaveBeenCalledOnce()
  })

  it('throws ApiError when a failed status has no parseable JSON body', async () => {
    saveSession({ accessToken: 'tok' })
    fetchMock.mockResolvedValue(new Response('Bad Gateway', { status: 502 }))
    await expect(request({ module: 'app', url: '/employee/baseinfo' })).rejects.toThrow('502')
  })

  it('returns undefined for a successful empty response', async () => {
    saveSession({ accessToken: 'tok' })
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }))
    await expect(request({ module: 'app', url: '/employee/baseinfo' })).resolves.toBeUndefined()
  })

  it('throws ApiError for a failed empty response', async () => {
    saveSession({ accessToken: 'tok' })
    fetchMock.mockResolvedValue(new Response(null, { status: 500 }))
    await expect(request({ module: 'app', url: '/employee/baseinfo' })).rejects.toThrow('500')
  })
})

describe('custom headers', () => {
  it('passes extra headers through (built-ins win on conflict)', async () => {
    saveSession({ accessToken: 'tok' })
    await request({
      module: 'platform',
      url: '/admittance/apply/app/applyDetail',
      auth: 'none',
      headers: { 'X-Visitor-Query-Token': 'tok-q', 'Content-Type': 'text/evil' },
    })
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    const headers = init.headers as Record<string, string>
    expect(headers['X-Visitor-Query-Token']).toBe('tok-q')
    expect(headers['Content-Type']).toBe('application/json')
  })
})

describe('request dedupe', () => {
  it('aborts the previous in-flight request with the same dedupeKey', async () => {
    saveSession({ accessToken: 'tok' })
    const seenSignals: AbortSignal[] = []
    fetchMock.mockImplementation((_url: string, init: RequestInit) => {
      seenSignals.push(init.signal as AbortSignal)
      return new Promise<Response>((resolve) => {
        setTimeout(() => resolve(jsonResponse({ code: 0 })), 5)
      })
    })
    const first = request({ module: 'app', url: '/home/bbs/list', dedupeKey: 'bbs' })
    const second = request({ module: 'app', url: '/home/bbs/list', dedupeKey: 'bbs' })
    await Promise.allSettled([first, second])
    expect(seenSignals[0]?.aborted).toBe(true)
    expect(seenSignals[1]?.aborted).toBe(false)
  })
})
