import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { readFileSync } from 'node:fs'
import {
  admittanceNoticeHtml,
  checkBlackVisitor,
  cropVisitorFace,
  uploadVisitorDocument,
  uploadVisitorPhoto,
  type VisitorFaceDraft,
} from './api'

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

describe('cropVisitorFace', () => {
  it('uses a short-lived visitor capability instead of anonymous algorithm routing', async () => {
    fetchMock
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ code: 0, data: { capability: 'one-time-capability' } })),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ code: 0, data: { imageData: 'cut-base64', uploadCapability: 'upload-capability' } })),
      )

    await expect(
      cropVisitorFace('raw-image-base64', { draftToken: 'draft-token', draftId: 'draft-id' }),
    ).resolves.toEqual({ code: 0, data: { imageData: 'cut-base64', uploadCapability: 'upload-capability' } })

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

describe('anonymous visitor action capability', () => {
  const draft: VisitorFaceDraft = { draftToken: 'draft-token', draftId: 'draft-id' }

  it('sends the crop-derived FACE_UPLOAD capability with the face upload', async () => {
    fetchMock
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ code: 0, data: { capability: 'crop-capability' } })),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ code: 0, data: { imageData: 'cut-face-base64', uploadCapability: 'face-upload-capability' } })),
      )
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: { photoId: 'photo-1' } })))

    const crop = await cropVisitorFace('raw-face-base64', draft)
    await expect(uploadVisitorPhoto(crop.data!.imageData!, draft, crop.data!.uploadCapability!)).resolves.toEqual({
      code: 0,
      data: { photoId: 'photo-1' },
    })

    expect(fetchMock).toHaveBeenCalledTimes(3)
    const [capabilityUrl, capabilityInit] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(capabilityUrl).toBe('/platform/admittance/visitor-face/capability')
    expect(capabilityInit.headers).toMatchObject({ 'X-Visitor-Draft-Token': 'draft-token' })
    expect(JSON.parse(capabilityInit.body as string)).toEqual({ draftId: 'draft-id' })

    const [uploadUrl, uploadInit] = fetchMock.mock.calls[2] as [string, RequestInit]
    expect(uploadUrl).toBe('/app/wechat/visit/checkFace')
    expect(uploadInit.headers).toMatchObject({
      'X-Visitor-Action-Capability': 'face-upload-capability',
      'X-Visitor-Draft-Id': 'draft-id',
    })
    expect(JSON.parse(uploadInit.body as string)).toEqual({ visitorPhoto: 'cut-face-base64' })
  })

  it('mints a DOCUMENT_UPLOAD capability bound to the raw document image', async () => {
    fetchMock
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ code: 0, data: { capability: 'document-capability' } })),
      )
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: { photoId: 'doc-1' } })))

    await expect(uploadVisitorDocument('document-base64', draft)).resolves.toEqual({
      code: 0,
      data: { photoId: 'doc-1' },
    })
    const [capabilityUrl, capabilityInit] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(capabilityUrl).toBe('/platform/admittance/visitor-action/capability')
    expect(JSON.parse(capabilityInit.body as string)).toMatchObject({ draftId: 'draft-id', action: 'DOCUMENT_UPLOAD' })
    const [, uploadInit] = fetchMock.mock.calls[1] as [string, RequestInit]
    expect(uploadInit.headers).toMatchObject({ 'X-Visitor-Action-Capability': 'document-capability' })
  })

  it('mints a BLACKLIST_CHECK capability and never sends the draft token to App', async () => {
    fetchMock
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ code: 0, data: { capability: 'blacklist-capability' } })),
      )
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: true })))

    await expect(checkBlackVisitor({ visitorName: '张三', certNo: '110101199001010011', parkId: 1 }, draft))
      .resolves.toEqual({ code: 0, data: true })

    expect(fetchMock).toHaveBeenCalledTimes(2)
    const [capabilityUrl, capabilityInit] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(capabilityUrl).toBe('/platform/admittance/visitor-action/capability')
    expect(JSON.parse(capabilityInit.body as string)).toEqual({ draftId: 'draft-id', action: 'BLACKLIST_CHECK' })

    const [checkUrl, checkInit] = fetchMock.mock.calls[1] as [string, RequestInit]
    expect(checkUrl).toBe('/app/wechat/visit/checkBlackVisitor')
    expect(checkInit.headers).toMatchObject({
      'X-Visitor-Action-Capability': 'blacklist-capability',
      'X-Visitor-Draft-Id': 'draft-id',
    })
    expect(checkInit.headers).not.toMatchObject({ 'X-Visitor-Draft-Token': expect.anything() })
  })
})
