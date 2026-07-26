import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { readFileSync } from 'node:fs'
import {
  admittanceNoticeHtml,
  checkBlackVisitor,
	checkApplyEqual,
  cropVisitorFace,
  getCauseEnum,
  getAreaOptions,
  getTruckCauseEnum,
  getVisitorOpenId,
  saveVisitorApply,
  saveTruckApply,
  searchReceptionist,
  uploadVisitorDocument,
  uploadVisitorPhoto,
  verifyTruckSms,
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
    expect(JSON.parse(capabilityInit.body as string)).toEqual({
      draftId: 'draft-id',
      action: 'BLACKLIST_CHECK',
      payloadHash: '5b012e396a3e0bc4c43b600a31d30d68b79a1f34f19aadf667e141a3a7c2440c',
    })

    const [checkUrl, checkInit] = fetchMock.mock.calls[1] as [string, RequestInit]
    expect(checkUrl).toBe('/app/wechat/visit/checkBlackVisitor')
    expect(checkInit.headers).toMatchObject({
      'X-Visitor-Action-Capability': 'blacklist-capability',
      'X-Visitor-Draft-Id': 'draft-id',
    })
    expect(checkInit.headers).not.toMatchObject({ 'X-Visitor-Draft-Token': expect.anything() })
  })
})

describe('visitor entry capability contract', () => {
  const draft: VisitorFaceDraft = { draftToken: 'draft-token', draftId: 'draft-id' }

  it('exchanges the OAuth code in a POST body and only retains the opaque draft credential', async () => {
    fetchMock.mockResolvedValueOnce(
      new Response(JSON.stringify({ code: 0, data: { visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id' } })),
    )

    await expect(getVisitorOpenId('wx-code')).resolves.toEqual({
      code: 0,
      data: { visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id' },
    })

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toBe('/platform/admittance/apply/get/openId')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body as string)).toEqual({ code: 'wx-code' })
  })

  it('binds receptionist search and visitor submission to one-time draft capabilities', async () => {
    fetchMock
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: { capability: 'search-ticket' } })))
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: { receptionistBadge: '8031249' } })))
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: { capability: 'submit-ticket' } })))
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: {} })))

    await expect(searchReceptionist({ parkId: 1, receptionistName: ' 张 三 ', receptionistPhone: '138 0000 0000' }, draft))
      .resolves.toMatchObject({ code: 0 })
    await expect(saveVisitorApply({ parkId: 1, visitorPhone: '13800000000' }, draft)).resolves.toMatchObject({ code: 0 })

    const [searchCapabilityUrl, searchCapabilityInit] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(searchCapabilityUrl).toBe('/platform/admittance/visitor-action/capability')
    expect(searchCapabilityInit.headers).toMatchObject({ 'X-Visitor-Draft-Token': 'draft-token' })
    expect(JSON.parse(searchCapabilityInit.body as string)).toMatchObject({ draftId: 'draft-id', action: 'RECEPTIONIST_SEARCH' })

    const [searchUrl, searchInit] = fetchMock.mock.calls[1] as [string, RequestInit]
    expect(searchUrl).toBe('/platform/admittance/visitor-entry/receptionist')
    expect(searchInit.headers).toMatchObject({
      'X-Visitor-Action-Capability': 'search-ticket',
      'X-Visitor-Draft-Id': 'draft-id',
    })

    const [, submitCapabilityInit] = fetchMock.mock.calls[2] as [string, RequestInit]
    expect(JSON.parse(submitCapabilityInit.body as string)).toMatchObject({
      draftId: 'draft-id',
      action: 'APPLY_SUBMIT',
      payloadHash: expect.stringMatching(/^[0-9a-f]{64}$/),
    })
    const [submitUrl, submitInit] = fetchMock.mock.calls[3] as [string, RequestInit]
    expect(submitUrl).toBe('/platform/admittance/visitor-entry/apply')
    expect(submitInit.headers).toMatchObject({
      'X-Visitor-Action-Capability': 'submit-ticket',
      'X-Visitor-Draft-Token': 'draft-token',
      'X-Visitor-Draft-Id': 'draft-id',
    })
  })

  it('uses the stable cross-service payload digest for application submission', async () => {
    fetchMock
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: { capability: 'submit-ticket' } })))
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: {} })))

    await saveVisitorApply({ parkId: 1 }, draft)

    const [, capabilityInit] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(JSON.parse(capabilityInit.body as string).payloadHash).toBe(
      'a2d63db45041e64c23f057e4ee196e8dc3e860efebc22a61bd1e32d205d373b0',
    )

    fetchMock
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: { capability: 'submit-ticket' } })))
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: {} })))
    await saveVisitorApply({ parkId: 1, startTime: '2026-07-25 10:30:00' }, draft)
    const [, timedCapabilityInit] = fetchMock.mock.calls[2] as [string, RequestInit]
    expect(JSON.parse(timedCapabilityInit.body as string).payloadHash).toBe(
      '3fffbee7d8bf442ce98aa4789e0433c5efa80b30deff2066a679b9fe49d520d4',
    )
  })

	it('moves identity precheck behind a payload-bound draft capability', async () => {
		fetchMock
			.mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: { capability: 'precheck-ticket' } })))
			.mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: true })))

		await checkApplyEqual({ visitorName: '王五', fellowList: [] }, draft)

		const [ticketUrl, ticketInit] = fetchMock.mock.calls[0] as [string, RequestInit]
		expect(ticketUrl).toBe('/platform/admittance/visitor-action/capability')
		expect(JSON.parse(ticketInit.body as string)).toMatchObject({
			draftId: 'draft-id', action: 'APPLY_PRECHECK', payloadHash: expect.stringMatching(/^[0-9a-f]{64}$/),
		})
		const [precheckUrl, precheckInit] = fetchMock.mock.calls[1] as [string, RequestInit]
		expect(precheckUrl).toBe('/platform/admittance/visitor-entry/precheck')
		expect(precheckInit.headers).toMatchObject({
			'X-Visitor-Action-Capability': 'precheck-ticket',
			'X-Visitor-Draft-Token': 'draft-token',
			'X-Visitor-Draft-Id': 'draft-id',
		})
	})

  it('contains no browser-side openId, unionId, or legacy unauthenticated apply route', () => {
    const source = readFileSync('src/features/visitor/api.ts', 'utf8')
    expect(source).not.toContain('openId?:')
    expect(source).not.toContain('unionId?:')
    expect(source).not.toContain("url: '/admittance/apply/save/apply'")
    expect(source).not.toContain("url: '/admittance/apply/app/searchReceptionist'")
		expect(source).not.toContain("url: '/admittance/apply/equal/check'")
  })

  it('reads visitor options only through the OAuth-draft protected entry routes', async () => {
    fetchMock
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: [] })))
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: {} })))

    await getCauseEnum(draft)
    await getAreaOptions(1, draft)

    const [causeUrl, causeInit] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(causeUrl).toBe('/platform/admittance/visitor-entry/options/cause')
    expect(causeInit.headers).toMatchObject({
      'X-Visitor-Draft-Token': 'draft-token',
      'X-Visitor-Draft-Id': 'draft-id',
    })
    const [areaUrl, areaInit] = fetchMock.mock.calls[1] as [string, RequestInit]
    expect(areaUrl).toBe('/platform/admittance/visitor-entry/options/area-options?parkId=1')
    expect(areaInit.headers).toMatchObject({
      'X-Visitor-Draft-Token': 'draft-token',
      'X-Visitor-Draft-Id': 'draft-id',
    })
  })
})

describe('truck visitor SMS proof contract', () => {
  it('uses one SMS proof for truck cause options and application submission', async () => {
    fetchMock
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: { proof: 'truck-proof' } })))
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: [{ code: '1', desc: '送货' }] })))
      .mockResolvedValueOnce(new Response(JSON.stringify({ code: 0, data: {} })))

    await expect(verifyTruckSms('13800000000', '123456')).resolves.toEqual({
      code: 0,
      data: { proof: 'truck-proof' },
    })
    await getTruckCauseEnum('truck-proof')
    await saveTruckApply({ visitorName: '货车司机' }, 'truck-proof')

    const [verifyUrl, verifyInit] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(verifyUrl).toBe('/platform/admittance/visitor-truck/verify-sms')
    expect(verifyInit.method).toBe('POST')
    expect(JSON.parse(verifyInit.body as string)).toEqual({ mobile: '13800000000', smsCode: '123456' })

    const [optionsUrl, optionsInit] = fetchMock.mock.calls[1] as [string, RequestInit]
    expect(optionsUrl).toBe('/platform/admittance/visitor-truck/options/cause')
    expect(optionsInit.headers).toMatchObject({ 'X-Visitor-Truck-Sms-Proof': 'truck-proof' })

    const [applyUrl, applyInit] = fetchMock.mock.calls[2] as [string, RequestInit]
    expect(applyUrl).toBe('/platform/admittance/visitor-truck/apply')
    expect(applyInit.method).toBe('POST')
    expect(applyInit.headers).toMatchObject({ 'X-Visitor-Truck-Sms-Proof': 'truck-proof' })
    expect(JSON.parse(applyInit.body as string)).toEqual({ visitorName: '货车司机' })
  })

  it('contains no legacy anonymous truck option or application route', () => {
    const source = readFileSync('src/features/visitor/api.ts', 'utf8')
    expect(source).not.toContain("url: '/admittance/apply/enum/car/cause'")
    expect(source).not.toContain("url: '/admittance/apply/save/car/apply'")
  })

  it('refuses to call a truck endpoint without an SMS proof', () => {
    expect(() => getTruckCauseEnum('')).toThrow('短信验证已失效')
    expect(fetchMock).not.toHaveBeenCalled()
  })
})
