import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { readFileSync } from 'node:fs'
import { admittanceNoticeHtml, faceCut } from './api'

const fetchMock = vi.fn()

beforeEach(() => {
  fetchMock.mockReset()
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('admittanceNoticeHtml', () => {
  it('uses backend content when admittance notice is enabled', () => {
    expect(admittanceNoticeHtml({ isNeedNotice: 1, content: '<p>入园请佩戴口罩</p>' })).toBe(
      '<p>入园请佩戴口罩</p>',
    )
  })

  it('keeps compatibility with existing noticeContent mocks', () => {
    expect(
      admittanceNoticeHtml({ isNeedNotice: 1, noticeContent: '<p>凭码通行</p>' }),
    ).toBe('<p>凭码通行</p>')
  })

  it('returns empty string when notice is disabled or blank', () => {
    expect(admittanceNoticeHtml({ isNeedNotice: 0, content: '<p>隐藏</p>' })).toBe('')
    expect(admittanceNoticeHtml({ isNeedNotice: 1, content: '   ' })).toBe('')
    expect(admittanceNoticeHtml(undefined)).toBe('')
  })
})

describe('faceCut', () => {
  it('uses a short-lived visitor capability instead of anonymous algorithm routing', async () => {
    fetchMock
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ code: 0, data: { capability: 'one-time-capability' } })),
      )
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: 'cut-base64' })))

    await expect(
      faceCut('raw-image-base64', { draftToken: 'draft-token', draftId: 'draft-id' }),
    ).resolves.toEqual({ code: 0, data: 'cut-base64' })

    expect(fetchMock).toHaveBeenCalledTimes(2)
    const [capabilityUrl, capabilityInit] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(capabilityUrl).toBe('/platform/admittance/visitor-face/capability')
    expect(capabilityInit.headers).toMatchObject({ 'X-Visitor-Draft-Token': 'draft-token' })
    expect(JSON.parse(capabilityInit.body as string)).toEqual({ draftId: 'draft-id' })

    const [cropUrl, cropInit] = fetchMock.mock.calls[1] as [string, RequestInit]
    expect(cropUrl).toBe('/platform/admittance/visitor-face/crop')
    expect(cropInit.headers).toMatchObject({ 'X-Visitor-Face-Capability': 'one-time-capability' })
    expect(JSON.parse(cropInit.body as string)).toEqual({ draftId: 'draft-id', imageData: 'raw-image-base64' })
  })

  it('contains no anonymous algorithm face-cut route regression', () => {
    const source = readFileSync('src/features/visitor/api.ts', 'utf8')
    expect(source).not.toContain("module: 'algorithm'")
    expect(source).not.toContain("url: '/out/face/cut'")
    expect(source).toContain("url: '/admittance/visitor-face/crop'")
  })
})
