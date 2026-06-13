/**
 * Token storage compatible with the legacy front-end's localStorage format
 * (key prefix `xc-`, JSON envelope `{ dataType, content, datetime }`), so the
 * old and new apps can share the login session during the gray release.
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
