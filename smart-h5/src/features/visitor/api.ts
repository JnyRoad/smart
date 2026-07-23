import { request } from '@/lib/api/http'

/**
 * Visitor-flow gateway APIs. 访客人脸裁剪不是裸匿名接口：微信 code 换得的短时草稿
 * 会话只能换取一次性裁剪能力；已登录员工则走独立的 Bearer 认证入口。
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

/** 微信授权后由服务端签发的短时访客草稿会话。 */
export interface VisitorFaceDraft {
  draftToken: string
  draftId: string
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

/** Visitor-side OAuth: exchanges the WeChat code for openId/unionId and a short-lived face draft session. */
export function getVisitorOpenId(code: string) {
  return request<
    Envelope<{ openId?: string; unionId?: string; visitorDraftToken?: string; visitorDraftId?: string }>
  >({
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
    url: '/sms/visitor/send',
    method: 'POST',
    data: { mobile },
    auth: 'none',
  })
}

export function verifyVisitorSms(mobile: string, smsCode: string) {
  return request<Envelope<unknown>>({
    module: 'app',
    url: '/sms/visitor/verify',
    method: 'POST',
    data: { mobile, smsCode },
    auth: 'none',
  })
}

type VisitorAction = 'DOCUMENT_UPLOAD' | 'BLACKLIST_CHECK'

interface VisitorFaceCropResult {
  imageData?: string
  uploadCapability?: string
}

/** 访客草稿换取一次性动作票据；票据只在请求头传给 App，草稿 token 永不离开 Platform。 */
async function issueVisitorActionCapability(
  visitorDraft: VisitorFaceDraft,
  action: VisitorAction,
  payloadHash?: string,
) {
  const capability = await request<Envelope<{ capability?: string }>>({
    module: 'platform',
    url: '/admittance/visitor-action/capability',
    method: 'POST',
    data: { draftId: visitorDraft.draftId, action, payloadHash },
    auth: 'none',
    headers: { 'X-Visitor-Draft-Token': visitorDraft.draftToken },
  })
  if (capability.code !== 0 || !capability.data?.capability) {
    throw new Error(capability.message ?? '访客操作授权已失效，请重新进入申请流程')
  }
  return capability.data.capability
}

async function sha256(value: string): Promise<string> {
  if (!globalThis.crypto?.subtle) throw new Error('访客操作授权已失效，请重新进入申请流程')
  const digest = await globalThis.crypto.subtle.digest('SHA-256', new TextEncoder().encode(value))
  return Array.from(new Uint8Array(digest), (byte) => byte.toString(16).padStart(2, '0')).join('')
}

/** 与 App 使用完全一致的黑名单查询摘要：姓名和证件号去空白、证件号大写后用分隔符拼接。 */
function blacklistPayload(data: { visitorName: string; certNo: string; parkId: number }): string {
  return [data.visitorName.replace(/\s+/g, ''), data.certNo.replace(/\s+/g, '').toUpperCase(), String(data.parkId)].join('|')
}

/** Blacklist check; `data === false` means the visitor is blocked. */
export async function checkBlackVisitor(
  data: { visitorName: string; certNo: string; parkId: number },
  visitorDraft: VisitorFaceDraft,
) {
  const capability = await issueVisitorActionCapability(visitorDraft, 'BLACKLIST_CHECK', await sha256(blacklistPayload(data)))
  return request<Envelope<boolean>>({
    module: 'app',
    url: '/wechat/visit/checkBlackVisitor',
    method: 'POST',
    data,
    auth: 'none',
    headers: {
      'X-Visitor-Action-Capability': capability,
      'X-Visitor-Draft-Id': visitorDraft.draftId,
    },
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

/** 访客人脸裁剪只允许草稿会话；草稿缺失必须重新 OAuth，绝不降级到员工端点。 */
export async function cropVisitorFace(imageData: string, visitorDraft: VisitorFaceDraft) {
  const capability = await request<Envelope<{ capability?: string }>>({
    module: 'platform',
    url: '/admittance/visitor-face/capability',
    method: 'POST',
    data: { draftId: visitorDraft.draftId },
    auth: 'none',
    headers: { 'X-Visitor-Draft-Token': visitorDraft.draftToken },
  })
  if (capability.code !== 0 || !capability.data?.capability) {
    throw new Error(capability.message ?? '访客人脸授权已失效，请重新进入申请流程')
  }
  return request<Envelope<VisitorFaceCropResult>>({
    module: 'platform',
    url: '/admittance/visitor-face/crop',
    method: 'POST',
    data: { draftId: visitorDraft.draftId, imageData },
    auth: 'none',
    headers: { 'X-Visitor-Face-Capability': capability.data.capability },
  })
}

/** 已登录员工裁剪，只走 Bearer 认证路径。 */
export function cropEmployeeFace(imageData: string) {
  return request<Envelope<string>>({
    module: 'app',
    url: '/employee/face/crop',
    method: 'POST',
    data: { imageData },
  })
}

type PhotoUploadEnvelope = Envelope<string | number | { photoId?: string | number }> & {
  resultData?: { base64?: string }
}

/** 把已签发的访客上传 capability 与其对应图片送往兼容 App 路径。 */
export function uploadVisitorPhoto(visitorPhoto: string, visitorDraft: VisitorFaceDraft, capability: string) {
  return request<
    PhotoUploadEnvelope
  >({
    module: 'app',
    url: '/wechat/visit/checkFace',
    method: 'POST',
    data: { visitorPhoto },
    auth: 'none',
    headers: {
      'X-Visitor-Action-Capability': capability,
      'X-Visitor-Draft-Id': visitorDraft.draftId,
    },
  })
}

/** 访客车辆等文档图片必须使用绑定图片摘要的 DOCUMENT_UPLOAD capability。 */
export async function uploadVisitorDocument(visitorPhoto: string, visitorDraft: VisitorFaceDraft) {
  const capability = await issueVisitorActionCapability(visitorDraft, 'DOCUMENT_UPLOAD', await sha256(visitorPhoto))
  return uploadVisitorPhoto(visitorPhoto, visitorDraft, capability)
}

/** 已登录员工图片上传不再复用匿名访客路由。 */
export function checkFace(visitorPhoto: string) {
  return request<PhotoUploadEnvelope>({
    module: 'app',
    url: '/employee/photo/upload',
    method: 'POST',
    data: { visitorPhoto },
  })
}
