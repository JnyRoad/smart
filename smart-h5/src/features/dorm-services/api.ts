import { request } from '@/lib/api/http'
import type { ProcessNode } from './process'

interface Envelope<T> {
  code: number
  data?: T
  message?: string
  msg?: string
}

export interface EnumItem {
  code: string | number
  desc: string
}

// ===== dorm-repairs =====

export function getRepairRangeEnum(): Promise<Envelope<EnumItem[]>> {
  return request({ module: 'app', url: '/dormitory/repair/enum/range' })
}

export function addRepair(data: {
  rangeType: number
  repairType: number
  dormitoryName: string
  roomName: string
  faultDesc: string
  faultImgs: string[]
  parkId: number
}): Promise<Envelope<unknown>> {
  return request({ module: 'platform', url: '/dormitory/repair/add', method: 'POST', data })
}

export interface RepairRecord {
  id?: number | string
  name?: string
  statusDesc?: string
  status?: number
  rangeTypeDesc?: string
  repairTypeDesc?: string
  dormitoryName?: string
  faultDesc?: string
  createTime?: string
}

export function getRepairRecords(params: {
  current: number
  size: number
}): Promise<Envelope<{ records?: RepairRecord[]; pages?: number }>> {
  return request({ module: 'platform', url: '/dormitory/repair/query/record', params })
}

export interface RepairReply {
  replyStatusDesc?: string
  replyTime?: string
  replyName?: string
  replyDesc?: string
}

export interface RepairDetail extends RepairRecord {
  staffBadge?: string
  compName?: string
  depName?: string
  roomName?: string
  parkName?: string
  /** Legacy detail field. */
  imgs?: string[]
  /** Compatibility for existing mocks and any newer payloads. */
  faultImgs?: string[]
  approvalProcess?: ProcessNode[]
  repairReplyList?: RepairReply[] | null
}

export function getRepairDetail(id: string): Promise<Envelope<RepairDetail>> {
  return request({ module: 'platform', url: `/dormitory/repair/query/detail/${id}` })
}

// ===== dorm-exit =====

export interface MyRoom {
  dormitoryId?: string | number
  roomId?: string | number
  dormitoryName?: string
  roomName?: string
}

/** Note the double-data envelope: the room list lives at res.data.data. */
export function getMyRooms(badge: string): Promise<Envelope<{ data?: MyRoom[] }>> {
  return request({ module: 'app', url: `/appdormitory/roomList/${badge}` })
}

export function saveDormExit(data: Record<string, unknown>): Promise<Envelope<unknown>> {
  return request({ module: 'platform', url: '/dor/quit/apply', method: 'POST', data })
}

export interface DormExitRecord {
  id?: number | string
  name?: string
  statusDesc?: string
  status?: number
  dorDetailStr?: string[]
  quitReasonDesc?: string
  applyLeaveTime?: string
  createTime?: string
}

export function getDormExitPage(params: {
  current: number
  size: number
  badge: string
}): Promise<Envelope<{ records?: DormExitRecord[]; pages?: number }>> {
  return request({ module: 'platform', url: '/dor/quit/page', params })
}

export interface DormExitDetail extends DormExitRecord {
  faceId?: string
  remark?: string
  imgs?: string[]
  qrCode?: string
  approvalProcess?: ProcessNode[]
  /** 审批侧附加字段：时间线在 processRecord 下（与用户侧 approvalProcess 不同名）。 */
  processRecord?: ProcessNode[]
  /** 审批模式开关（true 才显示操作按钮，缺省只读）。 */
  isApprove?: boolean
}

export function getDormExitDetail(id: string): Promise<Envelope<DormExitDetail>> {
  return request({ module: 'platform', url: `/dor/quit/detail/${id}` })
}

export function getDormExitScanDetail(code: string): Promise<Envelope<DormExitDetail>> {
  return request({ module: 'platform', url: `/dor/quit/list/check/${code}` })
}

// ===== check-in =====

export interface Dormitory {
  id?: string | number
  dormitoryName?: string
}

export function queryDormitories(parkId: number): Promise<Envelope<Dormitory[]>> {
  return request({
    module: 'platform',
    url: '/dormitory/queryDormitory',
    method: 'POST',
    data: { parkId, isAccount: true },
  })
}

export interface RoomTypeItem {
  id?: string | number
  typeName?: string
}

export function getRoomTypes(parkId: number, dormitoryId: string | number): Promise<Envelope<RoomTypeItem[]>> {
  return request({
    module: 'platform',
    url: '/dormitory/type/by/park-and-dormitory',
    params: { parkId, dormitoryId },
  })
}

/** Raw shape of GET /staff/define/badge — submit body uses different key names. */
export interface StaffIdentity {
  name?: string
  sex?: string | number
  nation?: string
  certno?: string
  birth?: string
  homeAddress?: string
  validDate?: string
  validDateFm?: string
}

export function getStaffIdentity(badge: string): Promise<Envelope<StaffIdentity>> {
  return request({ module: 'platform', url: '/staff/define/badge', params: { badge } })
}

export function submitCheckIn(data: Record<string, unknown>): Promise<Envelope<unknown>> {
  return request({ module: 'platform', url: '/dormitory/room/autoallot', method: 'POST', data })
}

export interface ParkTreeNode {
  id?: string | number
  label?: string
  children?: ParkTreeNode[]
}

/** Returns park → building → floors; floors live at data[0].children[0].children. */
export function getFloorTree(params: {
  parkId: number
  dormitoryId: string | number
  roomType: string | number
}): Promise<Envelope<ParkTreeNode[]>> {
  return request({ module: 'platform', url: '/park/tree/condition', params })
}

export interface RoomCell {
  roomId?: string | number
  roomName?: string
  roomSex?: number
  freeBedNum?: number
  bedTotal?: number
}

export function searchRooms(params: {
  parkId: number
  dormitoryId: string | number
  roomType: string | number
  floorId: string | number
}): Promise<Envelope<RoomCell[]>> {
  return request({ module: 'platform', url: '/dormitory/room/search/condition', params })
}

export interface BedItem {
  id?: string | number
  bedNumber?: number
  staffBadge?: string | null
  delFlag?: number
}

export function getBedDetail(roomId: string | number): Promise<Envelope<BedItem[]>> {
  return request({ module: 'platform', url: `/dormitory/room/bedDetail/${roomId}`, method: 'POST' })
}

export interface CheckInRecord {
  dormitoryName?: string
  roomName?: string
  bedNumber?: number
  lockPwd?: {
    fingerprintCode?: number
    fingerprintDesc?: string
    dynamicCode?: number
    dynamicDesc?: string
  }
}

export function getCheckInRecords(badge: string): Promise<Envelope<CheckInRecord[]>> {
  return request({ module: 'platform', url: `/dormitory/staff/roomList/${badge}` })
}
