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

/**
 * 仅表示会话是否变化的递增代际，绝不携带或派生 bearer token。
 * QueryProvider 用它在换人或登出时丢弃旧身份的内存缓存。
 */
let sessionGeneration = 0
const sessionChangeListeners = new Set<(generation: number) => void>()

function notifySessionChanged(): void {
  sessionGeneration += 1
  sessionChangeListeners.forEach((listener) => listener(sessionGeneration))
}

/** 同源其他标签页改写 token 或执行 clear() 时，令本标签页丢弃身份相关缓存。 */
function handleStorageChange(event: StorageEvent): void {
  if (
    event.storageArea === localStorage &&
    (event.key === KEY_PREFIX + 'access_token' || event.key === null)
  ) {
    notifySessionChanged()
  }
}

/** 订阅会话代际变化；回调参数不包含任何认证秘密。 */
export function subscribeToSessionChanges(listener: (generation: number) => void): () => void {
  sessionChangeListeners.add(listener)
  if (sessionChangeListeners.size === 1 && typeof window !== 'undefined') {
    window.addEventListener('storage', handleStorageChange)
  }

  return () => {
    sessionChangeListeners.delete(listener)
    if (sessionChangeListeners.size === 0 && typeof window !== 'undefined') {
      window.removeEventListener('storage', handleStorageChange)
    }
  }
}

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
  const previousAccessToken = getAccessToken()
  write('access_token', session.accessToken)
  if (session.refreshToken !== undefined) write('refresh_token', session.refreshToken)
  if (session.expiresIn !== undefined) write('expires_in', session.expiresIn)

  // 同一登录态刷新附属字段不应丢失正常的短时查询缓存。
  if (previousAccessToken !== session.accessToken) notifySessionChanged()
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
  const previousAccessToken = getAccessToken()
  write('access_token', INVALID_TOKEN)
  if (previousAccessToken !== INVALID_TOKEN) notifySessionChanged()
}

export function clearSession(): void {
  for (const key of SESSION_KEYS) localStorage.removeItem(KEY_PREFIX + key)
  notifySessionChanged()
}
