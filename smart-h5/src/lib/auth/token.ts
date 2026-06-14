/**
 * Token storage compatible with the legacy front-end's localStorage format
 * (key prefix `xc-`, JSON envelope `{ dataType, content, datetime }`), so the
 * old and new apps can share the login session during the gray release.
 */

/*
 * 安全风险说明（access_token 存 localStorage）
 *
 * 为什么用 localStorage：
 *   灰度期间新旧两版 H5 需要共享同一份登录会话，旧版用 `xc-` 前缀的
 *   localStorage 存放 token，本版必须沿用同一存储格式才能无缝复用会话、
 *   避免用户在新旧页面间来回跳转时被反复要求登录。
 *
 * 风险：
 *   localStorage 对同源 JS 完全可读，一旦页面出现 XSS，攻击者脚本即可
 *   读取 access_token / refresh_token 并外传，冒用用户身份调用网关接口。
 *   这是一个已知的中危风险（无法靠 JS 隔离根治）。
 *
 * 后续计划（本轮不做）：
 *   待灰度结束、不再需要与旧版共享会话后，单独立项把 token 迁移到
 *   httpOnly + Secure + SameSite 的 cookie，使其对前端 JS 不可读，从源头
 *   消除 XSS 窃取风险；该改造会牵动鉴权 / CSRF / Bearer 头传递，需独立评估。
 *   本轮仅做零业务破坏的加固（补充本说明 + 上线仅报告模式 CSP 观察违规），
 *   不改动下方任何 token 的实际存取逻辑。
 */
const KEY_PREFIX = 'xc-'

export const INVALID_TOKEN = 'invalid_token'

const SESSION_KEYS = ['access_token', 'refresh_token', 'expires_in'] as const

function write(name: string, content: string | number) {
  localStorage.setItem(
    KEY_PREFIX + name,
    JSON.stringify({ dataType: typeof content, content, datetime: Date.now() }),
  )
}

function read(name: string): string | null {
  const raw = localStorage.getItem(KEY_PREFIX + name)
  if (raw === null) return null
  try {
    const envelope: unknown = JSON.parse(raw)
    if (
      typeof envelope === 'object' &&
      envelope !== null &&
      'content' in envelope &&
      typeof (envelope as { content: unknown }).content === 'string'
    ) {
      return (envelope as { content: string }).content
    }
    return null
  } catch {
    // Legacy getStore falls back to the raw value when it is not JSON.
    return raw
  }
}

export interface AuthSession {
  accessToken: string
  refreshToken?: string
  expiresIn?: number
}

export function saveSession(session: AuthSession): void {
  write('access_token', session.accessToken)
  if (session.refreshToken !== undefined) write('refresh_token', session.refreshToken)
  if (session.expiresIn !== undefined) write('expires_in', session.expiresIn)
}

export function getAccessToken(): string | null {
  return read('access_token')
}

export function hasValidToken(): boolean {
  const token = getAccessToken()
  return token !== null && token !== '' && token !== INVALID_TOKEN
}

/** Legacy behavior on 401 invalid_token: overwrite the token with a marker. */
export function markTokenInvalid(): void {
  write('access_token', INVALID_TOKEN)
}

export function clearSession(): void {
  for (const key of SESSION_KEYS) localStorage.removeItem(KEY_PREFIX + key)
}
