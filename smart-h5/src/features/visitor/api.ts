import { request } from '@/lib/api/http'

/**
 * Visitor-flow gateway APIs. The whole flow is unauthenticated
 * (`auth: 'none'`), matching the legacy app where every visitor request
 * was sent without a bearer token.
 */
interface Envelope<T> {
  code: number
  data?: T
  message?: string
}

export interface EnumItem {
  code: string | number
  desc: string
}

export interface AdmittanceNotice {
  isNeedNotice?: number
  /** Current backend field: ConfigVisitorNoticeDTO.content. */
  content?: string
  /** Compatibility with existing Smart H5 mocks. */
  noticeContent?: string
}

export function admittanceNoticeHtml(notice: AdmittanceNotice | undefined): string {
  if (notice?.isNeedNotice !== 1) return ''
  return (notice.content ?? notice.noticeContent ?? '').trim()
}

export function getAdmittanceNotice(parkId: number) {
  return request<Envelope<AdmittanceNotice>>({
    module: 'platform',
    url: '/common/config/admittance/notice',
    params: { parkId },
    auth: 'none',
  })
}

/** Visitor-side OAuth: exchanges the WeChat code for openId/unionId (not a login token). */
export function getVisitorOpenId(code: string) {
  return request<Envelope<{ openId?: string; unionId?: string }>>({
    module: 'platform',
    url: '/admittance/apply/get/openId',
    params: { code },
    auth: 'none',
  })
}

export function searchReceptionist(data: {
  parkId: number
  receptionistName: string
  receptionistPhone: string
}) {
  return request<
    Envelope<{ receptionistBadge?: string; receptionistName?: string; receptionistPhone?: string }>
  >({
    module: 'platform',
    url: '/admittance/apply/app/searchReceptionist',
    method: 'POST',
    data,
    auth: 'none',
  })
}

export function getPersonCertEnum() {
  return request<Envelope<EnumItem[]>>({
    module: 'platform',
    url: '/admittance/apply/enum/person/cert',
    auth: 'none',
  })
}

export function getCauseEnum() {
  return request<Envelope<EnumItem[]>>({
    module: 'platform',
    url: '/admittance/apply/enum/cause',
    auth: 'none',
  })
}

export function getVehicleCertEnum() {
  return request<Envelope<EnumItem[]>>({
    module: 'platform',
    url: '/admittance/apply/enum/vehicle/cert',
    auth: 'none',
  })
}

export function getTruckCauseEnum() {
  return request<Envelope<EnumItem[]>>({
    module: 'platform',
    url: '/admittance/apply/enum/car/cause',
    auth: 'none',
  })
}

export function getAreaTypeList() {
  return request<Envelope<EnumItem[]>>({
    module: 'platform',
    url: '/admittance/area/type/list',
    params: { type: 2 },
    auth: 'none',
  })
}

/** Normalized factory config used across the visitor area UI. */
export interface FactoryAreaConfig {
  factoryType?: string
  factoryName?: string
  /** 1 = new factory, 0 = old factory */
  areaFlag?: number
  /** Max chips shown inline on the info page (default 4). */
  inlineAreaLimit?: number
  areas?: { code: string; name: string; isCommon?: number }[]
}

/** Raw shape returned by /admittance/apply/app/area-options (data is an object). */
export interface AreaOptionsResponse {
  parkId?: number
  inlineAreaLimit?: number
  factories?: {
    factoryType?: string | number
    factoryName?: string
    areaFlag?: number
    sort?: number
    areas?: { areaCode?: string | number; areaName?: string; isCommon?: boolean; sort?: number }[]
  }[]
}

export function getAreaOptions(parkId: number) {
  return request<Envelope<AreaOptionsResponse>>({
    module: 'platform',
    url: '/admittance/apply/app/area-options',
    params: { parkId },
    auth: 'none',
  })
}

export function getFactoryTypeEnum(flag: 0 | 1) {
  return request<Envelope<EnumItem[]>>({
    module: 'platform',
    url: '/admittance/apply/enum/factory/type',
    params: { flag },
    auth: 'none',
  })
}

/** Duplicate-application / identity consistency check before the SMS step. */
export function checkApplyEqual(data: Record<string, unknown>) {
  return request<Envelope<unknown>>({
    module: 'platform',
    url: '/admittance/apply/equal/check',
    method: 'POST',
    data,
    auth: 'none',
  })
}

export function sendVisitorSms(mobile: string) {
  return request<Envelope<unknown>>({
    module: 'app',
    url: `/sms/send/getCode/${mobile}`,
    auth: 'none',
  })
}

export function verifyVisitorSms(mobile: string, smsCode: string) {
  return request<Envelope<unknown>>({
    module: 'app',
    url: '/sms/verify',
    params: { mobile, smsCode },
    auth: 'none',
  })
}

/** Blacklist check; `data === false` means the visitor is blocked. */
export function checkBlackVisitor(data: { visitorName: string; certNo: string; parkId: number }) {
  return request<Envelope<boolean>>({
    module: 'app',
    url: '/wechat/visit/checkBlackVisitor',
    method: 'POST',
    data,
    auth: 'none',
  })
}

export function saveVisitorApply(data: Record<string, unknown>) {
  return request<Envelope<unknown>>({
    module: 'platform',
    url: '/admittance/apply/save/apply',
    method: 'POST',
    data,
    auth: 'none',
  })
}

export function saveTruckApply(data: Record<string, unknown>) {
  return request<Envelope<unknown>>({
    module: 'platform',
    url: '/admittance/apply/save/car/apply',
    method: 'POST',
    data,
    auth: 'none',
  })
}

export interface ApplyDetail {
  delFlag?: number
  qrCode?: string
  smsCode?: string
  parkName?: string
  causeDesc?: string
  receptionistName?: string
  receptionistPhone?: string
  visitorName?: string
  visitorPhone?: string
  permitFactoryTypeDesc?: string
  areaType?: Array<string | number> | string
  permitArea?: string
  permitOldArea?: string
  startTime?: string
  endTime?: string
}

export function getApplyDetail(id: string) {
  return request<Envelope<ApplyDetail>>({
    module: 'platform',
    url: `/admittance/apply/search/Detail/${id}`,
    auth: 'none',
  })
}

/**
 * Face detection + crop. Request body is `{ imageData: base64 }`; the response
 * `data` is the cropped image as a raw base64 string (no dataURL prefix).
 */
export function faceCut(imageData: string) {
  return request<Envelope<string>>({
    module: 'algorithm',
    url: '/out/face/cut',
    method: 'POST',
    data: { imageData },
    auth: 'none',
  })
}

/**
 * Uploads a photo (face or document). The usual response returns `data.photoId`;
 * `resultData.base64` is only an optional gateway variant.
 */
export function checkFace(visitorPhoto: string) {
  // 网关把照片 id 嵌在 data.photoId（旧版 res.data.photoId）；部分场景直接给字符串。
  return request<
    Envelope<string | number | { photoId?: string | number }> & { resultData?: { base64?: string } }
  >({
    module: 'app',
    url: '/wechat/visit/checkFace',
    method: 'POST',
    data: { visitorPhoto },
    auth: 'none',
  })
}
