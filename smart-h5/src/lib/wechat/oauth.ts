import { getTenantConfig } from '@/lib/config/tenant'

/**
 * Builds the WeChat snsapi_base silent-authorization URL. WeChat redirects
 * back to `redirectPath` with a `?code=` parameter appended.
 */
export function buildWechatOAuthUrl(redirectPath: string): string {
  const { wxAppId } = getTenantConfig()
  const redirectUri = encodeURIComponent(`${window.location.origin}${redirectPath}`)
  return (
    'https://open.weixin.qq.com/connect/oauth2/authorize' +
    `?appid=${wxAppId}&redirect_uri=${redirectUri}` +
    '&response_type=code&scope=snsapi_base&state=123#wechat_redirect'
  )
}

export const WECHAT_CALLBACK_PATH = '/login/wechat/callback'
export const BADGE_BINDING_PATH = '/login/badge'

export function redirectToWechatOAuth(redirectPath: string = WECHAT_CALLBACK_PATH): void {
  window.location.href = buildWechatOAuthUrl(redirectPath)
}
