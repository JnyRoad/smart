import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  clearQuerySession,
  fetchApprovalProgress,
  fetchApplyDetail,
  fetchMyApplies,
  getQuerySession,
  revokeApply,
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
  localStorage.clear()
  fetchMock.mockReset()
  fetchMock.mockResolvedValue(jsonResponse({ code: 0, data: {} }))
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  vi.unstubAllGlobals()
  vi.restoreAllMocks()
  window.__SMART_CONFIG__ = undefined
})

describe('mock 开关', () => {
  it('开关开：不发请求，返回 fixture 列表与 token', async () => {
    setMockFlag(true)
    const res = await fetchMyApplies({ mobile: '13700001234', smsCode: '123456' })
    expect(fetchMock).not.toHaveBeenCalled()
    expect(res.code).toBe(0)
    expect(res.data?.queryToken).toBeTruthy()
    expect(res.data?.maskedName).toBe('李明')
    expect(res.data?.maskedMobile).toBe('137****1234')
    expect(res.data?.records.length).toBeGreaterThanOrEqual(5)
    expect(res.data?.records[0]?.receptionistName).toBe('王强')
    expect(res.data?.records[0]?.currentNode).toBe('部门负责人 张三 审批中')
  })

  it('开关开：sendRecordSms 仍复用访客申请短信 GET', async () => {
    setMockFlag(true)
    await sendRecordSms('13700001234')
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toBe('/app/sms/send/getCode/13700001234')
    expect(init.method).toBe('GET')
    expect(init.body).toBeUndefined()
  })

  it('开关开：详情按 applyId 返回对应演示态', async () => {
    setMockFlag(true)
    const res = await fetchApplyDetail('mock-pending')
    expect(res.data?.applyStatus).toBe('PENDING')
  })

  it('开关开：详情姓名不脱敏，手机号继续脱敏', async () => {
    setMockFlag(true)
    const res = await fetchApplyDetail('mock-pending')

    expect(res.data?.receptionistName).toBe('王强')
    expect(res.data?.visitorName).toBe('李明')
    expect(res.data?.visitorPhone).toBe('137****1234')
    expect(res.data?.fellows.map((f) => f.name)).toEqual(['赵六', '周燕'])
  })

  it('开关开：审批进度姓名不脱敏', async () => {
    setMockFlag(true)
    const res = await fetchApprovalProgress('mock-pending')

    expect(res.data?.nodes.map((node) => node.approverName)).toEqual(['王强', '张三'])
  })

  it('开关关：listMyApply 走真实 POST', async () => {
    setMockFlag(false)
    await fetchMyApplies({ mobile: '13700001234', smsCode: '123456' })
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toBe('/platform/admittance/apply/app/listMyApply')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body as string)).toEqual({ mobile: '13700001234', smsCode: '123456' })
  })

  it('开关关：拒绝后端不支持的 openId 免验形态', async () => {
    setMockFlag(false)
    await expect(fetchMyApplies({ openId: 'oid-1' } as never)).rejects.toThrow(
      '访客记录查询仅支持短信验证码或已有查询凭证',
    )
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('开关关：token 刷新形态（null 入参）空体 + token 头', async () => {
    setMockFlag(false)
    saveQuerySession({ queryToken: 'tok-q', maskedName: '李明', maskedMobile: '137****1234' })
    await fetchMyApplies(null)
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(JSON.parse(init.body as string)).toEqual({})
    expect((init.headers as Record<string, string>)['X-Visitor-Query-Token']).toBe('tok-q')
  })

  it('开关关：详情请求带 X-Visitor-Query-Token 头', async () => {
    setMockFlag(false)
    saveQuerySession({ queryToken: 'tok-q', maskedName: '李明', maskedMobile: '137****1234' })
    await fetchApplyDetail('a-1')
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toBe('/platform/admittance/apply/app/applyDetail?applyId=a-1')
    expect((init.headers as Record<string, string>)['X-Visitor-Query-Token']).toBe('tok-q')
  })

  it('开关关：作废申请使用本人查询凭证提交申请单 ID', async () => {
    setMockFlag(false)
    saveQuerySession({ queryToken: 'tok-q', maskedName: '李明', maskedMobile: '137****1234' })

    await revokeApply('a-1')

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toBe('/platform/admittance/apply/app/revoke')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body as string)).toEqual({ applyId: 'a-1' })
    expect((init.headers as Record<string, string>)['X-Visitor-Query-Token']).toBe('tok-q')
  })
})

describe('query session', () => {
  it('localStorage 保存 24 小时查询凭证', () => {
    saveQuerySession({ queryToken: 't', maskedName: '李明', maskedMobile: '137****1234' })

    expect(getQuerySession()?.queryToken).toBe('t')
    expect(getQuerySession()?.maskedName).toBe('李明')
    sessionStorage.clear()
    expect(getQuerySession()?.queryToken).toBe('t')
  })

  it('清除本地查询凭证', () => {
    saveQuerySession({ queryToken: 't', maskedName: '李明', maskedMobile: '137****1234' })

    clearQuerySession()

    expect(getQuerySession()).toBeNull()
  })

  it('本地查询凭证超过 24 小时自动失效', () => {
    const nowSpy = vi.spyOn(Date, 'now').mockReturnValue(0)
    saveQuerySession({ queryToken: 't', maskedName: '李明', maskedMobile: '137****1234' })
    nowSpy.mockReturnValue(24 * 60 * 60 * 1000 + 1)

    expect(getQuerySession()).toBeNull()
    expect(localStorage.getItem('visitor-query-session')).toBeNull()
  })

  it('同一个 token 刷新列表时不重置 24 小时起点', () => {
    const nowSpy = vi.spyOn(Date, 'now').mockReturnValue(0)
    saveQuerySession({ queryToken: 't', maskedName: '李明', maskedMobile: '137****1234' })
    nowSpy.mockReturnValue(23 * 60 * 60 * 1000)
    saveQuerySession({ queryToken: 't', maskedName: '李明', maskedMobile: '137****1234' })
    nowSpy.mockReturnValue(24 * 60 * 60 * 1000 + 1)

    expect(getQuerySession()).toBeNull()
  })

  it('新 token 会重新计算 24 小时起点', () => {
    const nowSpy = vi.spyOn(Date, 'now').mockReturnValue(0)
    saveQuerySession({ queryToken: 'old-token', maskedName: '李明', maskedMobile: '137****1234' })
    nowSpy.mockReturnValue(23 * 60 * 60 * 1000)
    saveQuerySession({ queryToken: 'new-token', maskedName: '李明', maskedMobile: '137****1234' })
    nowSpy.mockReturnValue(46 * 60 * 60 * 1000)

    expect(getQuerySession()?.queryToken).toBe('new-token')
  })
})
