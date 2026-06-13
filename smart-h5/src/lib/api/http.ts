import { getAccessToken, hasValidToken } from '@/lib/auth/token'

/** Gateway module prefixes, identical to the legacy reverse-proxy layout. */
// NOTE: the gateway's 'visitor' prefix is deliberately absent — it collides
// with this app's /visitor/* pages and no new-frontend API lives under it.
export type ApiModule =
  | 'platform'
  | 'app'
  | 'auth'
  | 'admin'
  | 'algorithm'
  | 'workbench'
  | 'file'

export type AuthMode = 'bearer' | 'basic' | 'none'

export interface ApiRequestOptions {
  module: ApiModule
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  /** Query-string parameters; undefined values are skipped. */
  params?: Record<string, string | number | boolean | undefined>
  /** JSON body (non-GET only). */
  data?: unknown
  /** Defaults to 'bearer'. OAuth code exchange uses 'basic'. */
  auth?: AuthMode
  /** Extra request headers; built-in headers (Authorization etc.) win on conflict. */
  headers?: Record<string, string>
  /** When set, a new call aborts the previous in-flight call with the same key. */
  dedupeKey?: string
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status?: number,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

/** Legacy OAuth client credentials (`smart:smart`) used for the code exchange. */
const BASIC_CREDENTIALS = 'c21hcnQ6c21hcnQ='

let invalidTokenHandler: () => void = () => {}

/**
 * Installed once at app bootstrap: marks the token invalid and redirects to
 * WeChat OAuth, mirroring the legacy 401 invalid_token behavior.
 */
export function setInvalidTokenHandler(handler: () => void): void {
  invalidTokenHandler = handler
}

const inflight = new Map<string, AbortController>()

const HTML_ENTITIES: Record<string, string> = {
  '&#40;': '(',
  '&#41;': ')',
  '&#39;': "'",
  '&lt;': '<',
  '&gt;': '>',
}

const ENTITY_PATTERN = /&#40;|&#41;|&#39;|&lt;|&gt;/g

function unescapeEntities(value: unknown): unknown {
  if (typeof value === 'string') {
    return value.replace(ENTITY_PATTERN, (entity) => HTML_ENTITIES[entity] ?? entity)
  }
  if (Array.isArray(value)) return value.map(unescapeEntities)
  if (typeof value === 'object' && value !== null) {
    return Object.fromEntries(
      Object.entries(value as Record<string, unknown>).map(([k, v]) => [k, unescapeEntities(v)]),
    )
  }
  return value
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

export async function request<T = unknown>(options: ApiRequestOptions): Promise<T> {
  const {
    module,
    url,
    method = 'GET',
    params,
    data,
    auth = 'bearer',
    headers: extraHeaders,
    dedupeKey,
  } = options

  const query = new URLSearchParams()
  if (params) {
    for (const [key, value] of Object.entries(params)) {
      if (value !== undefined) query.set(key, String(value))
    }
  }
  // Legacy convention: POST pagination fields are promoted to the query string
  // (the gateway reads them there) while the body keeps them untouched.
  if (method === 'POST' && isRecord(data) && data.current != null && data.size != null) {
    query.set('current', String(data.current))
    query.set('size', String(data.size))
  }

  const headers: Record<string, string> = {
    ...extraHeaders,
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest',
  }
  if (auth === 'bearer') {
    if (!hasValidToken()) {
      throw new ApiError('未登录或登录已失效')
    }
    headers.Authorization = `Bearer ${getAccessToken()}`
  } else if (auth === 'basic') {
    headers.Authorization = `Basic ${BASIC_CREDENTIALS}`
  }

  let signal: AbortSignal | undefined
  if (dedupeKey !== undefined) {
    inflight.get(dedupeKey)?.abort()
    const controller = new AbortController()
    inflight.set(dedupeKey, controller)
    signal = controller.signal
  }

  const queryString = query.toString()
  const fullUrl = `/${module}${url}${queryString ? `?${queryString}` : ''}`

  let response: Response
  try {
    response = await fetch(fullUrl, {
      method,
      headers,
      body: method !== 'GET' && data !== undefined ? JSON.stringify(data) : undefined,
      signal,
    })
  } finally {
    if (dedupeKey !== undefined && !signal?.aborted) inflight.delete(dedupeKey)
  }

  // Legacy semantics (axios validateStatus: () => true): the body is processed
  // for every HTTP status — gateways return business envelopes with 4xx too
  // (e.g. invalid_token comes as HTTP 401 + JSON body). Only an unparseable
  // non-empty body or an empty failed response becomes a transport error.
  const text = await response.text()
  if (text === '') {
    if (response.ok) return undefined as T
    throw new ApiError(`请求失败：HTTP ${response.status}`, response.status)
  }
  let body: unknown
  try {
    body = unescapeEntities(JSON.parse(text))
  } catch {
    throw new ApiError(`请求失败：HTTP ${response.status}`, response.status)
  }

  if (isRecord(body) && body.code === 401 && body.msg === 'invalid_token') {
    invalidTokenHandler()
    throw new ApiError('登录已失效，请重新授权', 401)
  }

  return body as T
}
