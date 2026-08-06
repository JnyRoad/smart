import { request } from '@/lib/api/http'
import type { ProcessNode } from '@/features/dorm-services/process'

interface Envelope<T> {
  code: number
  data?: T
  message?: string
  msg?: string
}

interface PageData<T> {
  records?: T[]
  pages?: number
}

// ===== 生活区（articlesrelease/living）=====

export interface LiveRoom {
  dormitoryId?: string | number
  floorId?: string | number
  roomId?: string | number
  /** 床位主键（旧版选项第四段取的就是 id）。 */
  id?: string | number
  dormitoryName?: string
  floorName?: string
  roomName?: string
  bedNumber?: number
}

/** 生活区物品放行只读取认证主体的房间，避免在 URL 暴露工号。 */
export function getLiveRooms(): Promise<Envelope<LiveRoom[]>> {
	return request({ module: 'platform', url: '/dormitory/staff/me/roomList' })
}

export function saveLiveRelease(data: Record<string, unknown>): Promise<Envelope<unknown>> {
  return request({ module: 'platform', url: '/articlesrelease/living/save', method: 'POST', data })
}

export interface ReleaseListItem {
  id?: string | number
  name?: string
  status?: number
  oaNode?: string
  articlesTypeName?: string
  articlesDesc?: string
  carrier?: string
  createTime?: string
}

export function getLiveReleasePage(params: {
  badge: string
  current: number
  size: number
}): Promise<Envelope<PageData<ReleaseListItem>>> {
  return request({
    module: 'platform',
    url: '/articlesrelease/page',
    params: { ...params, type: 3 },
  })
}

// ===== 详情（生活区 / 办公区 / 返厂共用）=====

export interface ReleaseApplyMain {
  fxqc?: number
  fxqcDesc?: string
  sffc?: number
  sffcDesc?: string
  fxdd?: number
  fxddDesc?: string
  dddd?: number
  ddddDesc?: string
  fxsx?: number
  fxsxDesc?: string
  wpfxlb?: number
  wpfxlbDesc?: string
  sqrjb?: number
  sqrjbDesc?: string
  fjsc?: string
}

/** 详情链人员（xm 在详情数据里是姓名文本，与提交链 xm=人员 id 语义不同）。 */
export interface PersonDetail {
  gh?: string
  xm?: string
  lcsy?: string
  lcrq?: string
  lcsj?: string
}

export interface ThingDetail {
  wpbm?: string
  wpmc?: string
  wpdw?: string
  wpsl?: string | number
  jsdw?: string
  fxrq?: string
  bz?: string
  ysfs?: number
  ysfsDesc?: string
  xm?: string
  cph?: string
}

export interface ReleaseDetail {
  id?: string | number
  status?: number
  statusName?: string
  expire?: boolean
  qrCodePic?: string
  name?: string
  deptName?: string
  carrier?: string
  facePic?: string
  articlesTypeName?: string
  articlesDesc?: string
  dormitoryName?: string
  roomName?: string
  /** 审批侧附加字段：园区是否要求保安上传照片（0 需上传）。 */
  isUploadImg?: number
  parkName?: string
  floorName?: string
  bedName?: string
  phone?: string
  plannedDepartureTime?: string
  licensePlate?: string
  remarks?: string
  oneImg?: string
  twoImg?: string
  threeImg?: string
  applyMain?: ReleaseApplyMain
  personDetailList?: PersonDetail[]
  thingDetailList?: ThingDetail[]
  approvalProcess?: ProcessNode[]
  securityStaff?: string
  departureTime?: string
  remark?: string
  backTime?: string | null
}

export function getReleaseDetail(id: string): Promise<Envelope<ReleaseDetail>> {
  return request({ module: 'platform', url: `/articlesrelease/detail/${id}` })
}

// ===== 办公区（articlesrelease/office）=====

export function saveWorkRelease(data: Record<string, unknown>): Promise<Envelope<unknown>> {
  return request({ module: 'platform', url: '/articlesrelease/office/save', method: 'POST', data })
}

export interface WorkReleaseListItem {
  id?: string | number
  name?: string
  oaNode?: string
  compName?: string
  releaseItemDesc?: string
  createTime?: string
  backStatus?: string
}

export function getWorkReleasePage(params: {
  badge: string
  current: number
  size: number
}): Promise<Envelope<PageData<WorkReleaseListItem>>> {
  return request({
    module: 'platform',
    url: '/articlesrelease/office/page',
    params: { ...params, type: 5 },
  })
}

export interface ReleaseStaffInfo {
  id?: string | number
  name?: string
}

/** 创建受当前认证用户约束的服务端草稿。 */
export function createOfficeReleaseDraft(data: { parkId: number }): Promise<Envelope<{ releaseId?: string | number }>> {
	return request({ module: 'platform', url: '/articlesrelease/office/draft', method: 'POST', data })
}

/** 仅可在已持久化且当前用户有权访问的草稿上查询人员。 */
export function getReleaseStaffInfo(
	releaseId: string | number,
	badge: string,
): Promise<Envelope<ReleaseStaffInfo>> {
	return request({ module: 'platform', url: `/articlesrelease/${releaseId}/staff/lookup`, params: { badge } })
}

// ===== 返厂确认（articlesrelease/back）=====

export interface BackListItem {
  id?: string | number
  name?: string
  backStatus?: string
  deptName?: string
  releaseItemDesc?: string
  createTime?: string
  oaNode?: string
}

export interface BackSearch {
  badge?: string
  name?: string
  startTime?: string
  endTime?: string
}

export function getBackPage(
  params: { approvalStatus: 0 | 1; current: number; size: number } & BackSearch,
): Promise<Envelope<PageData<BackListItem>>> {
  return request({ module: 'platform', url: '/articlesrelease/back/page', params: { ...params } })
}

export function confirmBack(releaseId: string | number): Promise<Envelope<unknown>> {
  return request({ module: 'platform', url: `/articlesrelease/back/confirm/${releaseId}`, method: 'POST' })
}
