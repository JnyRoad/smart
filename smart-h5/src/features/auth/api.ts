import { request } from '@/lib/api/http'
import { getTenantConfig } from '@/lib/config/tenant'

/**
 * Response of the WeChat code-for-token exchange. On success the token fields
 * are set; when the WeChat account is not yet bound to an employee badge the
 * gateway returns `{ code: 1, data: '账号未绑定工号，请先绑定' }`.
 */
export interface WxTokenResponse {
  access_token?: string
  refresh_token?: string
  expires_in?: number
  code?: number
  data?: string
  message?: string
}

export function exchangeWechatCode(code: string): Promise<WxTokenResponse> {
  return request<WxTokenResponse>({
    module: 'auth',
    url: '/wx/public/token',
    method: 'POST',
    auth: 'basic',
    data: { code, type: 'F' },
  })
}

export interface GatewayResult {
  code: number
  message?: string
}

/** Binds the current WeChat openId (carried by the OAuth code) to an employee badge. */
export function bindEmployeeBadge(input: {
  code: string
  badge: string
  lastCertNum: string
}): Promise<GatewayResult> {
  return request<GatewayResult>({
    module: 'app',
    url: '/wechat/xc/banging/badge',
    method: 'POST',
    auth: 'none',
    data: { parkId: getTenantConfig().parkId, ...input },
  })
}

export function sendSmsCode(mobile: string): Promise<GatewayResult> {
  return request<GatewayResult>({
    module: 'app',
    url: `/sms/send/getCode/${mobile}`,
  })
}
