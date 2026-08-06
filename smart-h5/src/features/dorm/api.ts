import { request } from '@/lib/api/http'
import type { CateInfo } from './water-elec-rules'

interface Envelope<T> {
  code: number
  data?: T
  message?: string
  /** The dormitory endpoints use `msg` for error text (legacy gateway quirk). */
  msg?: string
}

export interface StatementRecord {
  staffName?: string
  staffBadge?: string
  statementDate?: string
  meterMonth?: string
  cateInfos?: CateInfo[]
  totalFee?: number
}

export function getWaterElecRecords(params: {
  current: number
  size: number
  statementMonth: string
}): Promise<Envelope<{ records?: StatementRecord[]; pages?: number }>> {
  return request({
    module: 'platform',
    url: '/dormitory/staff/statementdetail/record',
    params,
  })
}

/** Returns the door-lock dynamic code as HEX ciphertext (empty = not checked in). */
export function getLockPwd(badge: string): Promise<Envelope<string>> {
  return request({ module: 'platform', url: '/dormitory/staff/get/pwd', params: { badge } })
}

/** newPwd is sent in plaintext — verified against the legacy edit-code.vue. */
export function updateLockPwd(data: { badge: string; newPwd: string }): Promise<Envelope<unknown>> {
  return request({
    module: 'platform',
    url: '/dormitory/staff/update/lock/pwd',
    method: 'POST',
    data,
  })
}

/** Regenerates the lock code after a face check; facePic is the uploaded base64 image. */
export function refreshLockPwd(data: { badge: string; facePic: string }): Promise<Envelope<unknown>> {
  return request({ module: 'platform', url: '/dormitory/staff/update/pwd', method: 'POST', data })
}
