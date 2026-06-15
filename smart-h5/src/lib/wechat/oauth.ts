import { getTenantConfig } from '@/lib/config/tenant'

/** sessionStorage 中保存 OAuth state 的 key，用于回调时比对，防御登录 CSRF（会话嫁接）。 */
export const WX_OAUTH_STATE_KEY = 'wx-oauth-state'

/**
 * 生成加密随机 state。用 crypto.getRandomValues 取 16 字节并转 hex，
 * 保证不可预测——攻击者无法伪造一个能通过回调校验的 state。
 */
function generateOAuthState(): string {
  const bytes = new Uint8Array(16)
  crypto.getRandomValues(bytes)
  return Array.from(bytes, (byte) => byte.toString(16).padStart(2, '0')).join('')
}

/**
 * 构造微信 snsapi_base 静默授权 URL。微信会带 `?code=` 与 `?state=`
 * 重定向回 `redirectPath`。
 *
 * 跳转前生成随机 state 并写入 sessionStorage，供回调比对：URL 里的 state
 * 必须与本地持久化的 state 一致才放行，否则视为伪造请求拒绝。
 */
export function buildWechatOAuthUrl(redirectPath: string): string {
  const { wxAppId } = getTenantConfig()
  const redirectUri = encodeURIComponent(`${window.location.origin}${redirectPath}`)
  const state = generateOAuthState()
  // 持久化随机 state，回调时取出比对；写失败会抛错，宁可快速失败也不静默降级。
  sessionStorage.setItem(WX_OAUTH_STATE_KEY, state)
  return (
    'https://open.weixin.qq.com/connect/oauth2/authorize' +
    `?appid=${wxAppId}&redirect_uri=${redirectUri}` +
    `&response_type=code&scope=snsapi_base&state=${state}#wechat_redirect`
  )
}

/**
 * 校验回调 URL 携带的 state 是否与本地持久化的 state 一致，并消费（清除）该 state。
 *
 * fail closed：URL 缺少 state、本地无 state、两者不一致、或 sessionStorage 不可用，
 * 一律返回 false（拒绝登录）。无论成功失败都清除已存的 state，避免重放与多次尝试。
 *
 * @returns 校验通过返回 true，否则 false。
 */
export function consumeWechatOAuthState(urlState: string | null | undefined): boolean {
  let storedState: string | null = null
  try {
    storedState = sessionStorage.getItem(WX_OAUTH_STATE_KEY)
    // 一次性使用：取出后立即清除，无论后续比对结果如何。
    sessionStorage.removeItem(WX_OAUTH_STATE_KEY)
  } catch {
    // sessionStorage 不可用（隐私模式 / 被禁用）时无法校验，fail closed。
    return false
  }
  if (!urlState || !storedState) return false
  return urlState === storedState
}

export const WECHAT_CALLBACK_PATH = '/login/wechat/callback'
export const BADGE_BINDING_PATH = '/login/badge'

export function redirectToWechatOAuth(redirectPath: string = WECHAT_CALLBACK_PATH): void {
  window.location.href = buildWechatOAuthUrl(redirectPath)
}
