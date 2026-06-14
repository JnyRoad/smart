import { request } from '@/lib/api/http'

interface GatewayEnvelope<T> {
  code: number
  data?: T
  message?: string
}

export interface ServiceModule {
  moduleIcon?: string
  moduleName?: string
  moduleUrl?: string
}

export function getServiceModules(): Promise<GatewayEnvelope<{ serviceModule?: ServiceModule[] }>> {
  return request({ module: 'app', url: '/service/module/list' })
}

export interface BbsItem {
  bbsId?: number | string
  bbsTitle?: string
  bbsImg?: string
  bbsUrl?: string
  /** 1 = external link, 2 = rich text, 4 = PDF */
  contentLinkType?: number
}

export interface BbsPage {
  total?: number
  records?: BbsItem[]
}

export function getBbsList(params: {
  current: number
  size: number
  parkId?: number
  /** Legacy quirk: the list page sends the parkId under `badge`. */
  badge?: number
}): Promise<GatewayEnvelope<BbsPage>> {
  return request({ module: 'app', url: '/home/bbs/list', params })
}

export interface BbsDetail {
  bbsTitle?: string
  bbsContent?: string
  previewUrl?: string
}

export function getBbsDetail(id: string): Promise<GatewayEnvelope<BbsDetail>> {
  return request({ module: 'app', url: `/home/bbs/detail/${id}` })
}

export interface WeatherInfo {
  city?: string
  /** Temperature in °C. */
  wendu?: string
  forecast?: { type?: string; fengxiang?: string }[]
}

export function getWeather(city: string): Promise<GatewayEnvelope<WeatherInfo>> {
  return request({ module: 'app', url: '/common/weather', params: { city } })
}

export interface WechatSignature {
  timestamp: number | string
  nonceStr: string
  signature: string
}

export function getWechatSignature(params: { url: string }): Promise<GatewayEnvelope<WechatSignature>> {
  return request({ module: 'app', url: '/wechat/sign', params })
}

/** Pending good-release approvals (recordType 3 = 宿舍物品放行). */
export function getGoodReleaseApprovalCount(): Promise<GatewayEnvelope<{ total?: number }>> {
  return request({
    module: 'platform',
    url: '/approve/list/new/page',
    params: { recordType: 3, recordState: 0, current: 1, size: 10 },
  })
}

/** Pending repair approvals (recordType 5 = 园区报修). */
export function getRepairsApprovalCount(): Promise<GatewayEnvelope<{ total?: number }>> {
  return request({
    module: 'platform',
    url: '/approve/list/repairs/list',
    params: { recordType: 5, recordState: 0, current: 1, size: 10 },
  })
}

/** Pending dorm-exit approvals. */
export function getDormExitApprovalCount(input: {
  parkId: number
  isSecurityGuard?: number | boolean
}): Promise<GatewayEnvelope<{ total?: number }>> {
  return request({
    module: 'platform',
    url: '/dor/quit/list/approval',
    method: 'POST',
    data: { status: 0, current: 1, size: 10, ...input },
  })
}
