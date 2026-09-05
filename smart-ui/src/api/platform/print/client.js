import request from '@/router/axios'

/** 独立检查打印接口的 HTTP 与业务结果，避免旧拦截器把失败当成保存成功。 */
export async function printRequest(options) {
  const response = await request({ ...options, printDomain: true, url: `/platform/print/v1${options.url}` })
  const body = response.data || {}
  if (response.status < 200 || response.status >= 300 || body.error || (body.code !== undefined && ![0, '0', 200, '200'].includes(body.code))) {
    const problem = body.error || {}
    const error = new Error(problem.message || body.msg || '打印服务请求失败')
    error.code = problem.code || body.code || response.status
    error.details = problem.details || {}
    throw error
  }
  return body.data
}

/** 一次操作生成一次键；网络失败重试复用该键，修改请求内容时必须换键。 */
export function newIdempotencyKey() {
  const bytes = new Uint8Array(16)
  window.crypto.getRandomValues(bytes)
  return Array.from(bytes, value => value.toString(16).padStart(2, '0')).join('')
}

/** 分页及列表接口统一转为列表，但不把错误响应当成空列表。 */
export function recordsOf(data) { return Array.isArray(data) ? data : (data && data.records) || [] }
