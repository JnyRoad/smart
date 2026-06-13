import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  clearQuerySession,
  fetchApplyDetail,
  fetchMyApplies,
  getQuerySession,
  saveQuerySession,
  sendRecordSms,
} from './records-api'

const fetchMock = vi.fn()

function setMockFlag(on: boolean) {
  window.__SMART_CONFIG__ = { features: { visitorRecordsMock: on } }
}

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

beforeEach(() => {
  sessionStorage.clear()
  fetchMock.mockReset()
  fetchMock.mockResolvedValue(jsonResponse({ code: 0, data: {} }))
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  vi.unstubAllGlobals()
  window.__SMART_CONFIG__ = undefined
})

describe('mock 开关', () => {
  it('开关开：不发请求，返回 fixture 列表与 token', async () => {
    setMockFlag(true)
    const res = await fetchMyApplies({ mobile: '13700001234', smsCode: '123456' })
    expect(fetchMock).not.toHaveBeenCalled()
    expect(res.code).toBe(0)
    expect(res.data?.queryToken).toBeTruthy()
    expect(res.data?.records.length).toBeGreaterThanOrEqual(5)
  })

  it('开关开：sendRecordSms 仍走真实短信 POST', async () => {
    setMockFlag(true)
    await sendRecordSms('13700001234')
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toBe('/platform/admittance/apply/app/sendRecordSms')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body as string)).toEqual({ mobile: '13700001234' })
  })

  it('开关开：详情按 applyId 返回对应演示态', async () => {
    setMockFlag(true)
    const res = await fetchApplyDetail('mock-pending')
    expect(res.data?.applyStatus).toBe('PENDING')
  })

  it('开关关：listMyApply 走真实 POST', async () => {
    setMockFlag(false)
    await fetchMyApplies({ mobile: '13700001234', smsCode: '123456' })
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toBe('/platform/admittance/apply/app/listMyApply')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body as string)).toEqual({ mobile: '13700001234', smsCode: '123456' })
  })

  it('开关关：openId 免验形态走 POST body', async () => {
    setMockFlag(false)
    await fetchMyApplies({ openId: 'oid-1' })
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(JSON.parse(init.body as string)).toEqual({ openId: 'oid-1' })
  })

  it('开关关：token 刷新形态（null 入参）空体 + token 头', async () => {
    setMockFlag(false)
    saveQuerySession({ queryToken: 'tok-q', maskedName: '李**', maskedMobile: '137****1234' })
    await fetchMyApplies(null)
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(JSON.parse(init.body as string)).toEqual({})
    expect((init.headers as Record<string, string>)['X-Visitor-Query-Token']).toBe('tok-q')
  })

  it('开关关：详情请求带 X-Visitor-Query-Token 头', async () => {
    setMockFlag(false)
    saveQuerySession({ queryToken: 'tok-q', maskedName: '李**', maskedMobile: '137****1234' })
    await fetchApplyDetail('a-1')
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toBe('/platform/admittance/apply/app/applyDetail?applyId=a-1')
    expect((init.headers as Record<string, string>)['X-Visitor-Query-Token']).toBe('tok-q')
  })
})

describe('query session', () => {
  it('sessionStorage 存取与清除', () => {
    saveQuerySession({ queryToken: 't', maskedName: '李**', maskedMobile: '137****1234' })
    expect(getQuerySession()?.queryToken).toBe('t')
    clearQuerySession()
    expect(getQuerySession()).toBeNull()
  })
})
