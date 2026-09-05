import request from '@/router/axios'

/**
 * 从 axios 响应头读取不区分大小写的值，兼容 AxiosHeaders 和普通对象。
 * @param {Object} headers axios 响应头对象。
 * @param {string} name 要读取的响应头名称。
 * @returns {string} 响应头值，不存在时返回空字符串。
 */
function getResponseHeader(headers, name) {
  if (!headers) {
    return ''
  }
  if (typeof headers.get === 'function') {
    return headers.get(name) || headers.get(name.toLowerCase()) || ''
  }
  const targetName = name.toLowerCase()
  const headerKey = Object.keys(headers).find(key => key.toLowerCase() === targetName)
  return headerKey ? headers[headerKey] : ''
}

/**
 * 将 ArrayBuffer 或 TypedArray 统一转成 UTF-8 字节视图。
 * @param {ArrayBuffer|ArrayBufferView} data axios 二进制响应。
 * @returns {Uint8Array|null} 可解码字节；输入不是二进制时返回 null。
 */
function toUint8Array(data) {
  if (typeof ArrayBuffer === 'undefined') {
    return null
  }
  if (Object.prototype.toString.call(data) === '[object ArrayBuffer]') {
    return new Uint8Array(data)
  }
  if (ArrayBuffer.isView(data) || (data && data.buffer && typeof data.byteLength === 'number')) {
    return new Uint8Array(data.buffer, data.byteOffset, data.byteLength)
  }
  return null
}

/**
 * 解码导出接口的 UTF-8 文本，失败时返回空字符串让调用方保留原始二进制。
 * @param {string|ArrayBuffer|ArrayBufferView} data 服务端响应体。
 * @returns {string} 解码后的文本。
 */
function decodeResponseText(data) {
  if (typeof data === 'string') {
    return data
  }
  const bytes = toUint8Array(data)
  if (!bytes) {
    return ''
  }
  if (typeof TextDecoder !== 'undefined') {
    return new TextDecoder('utf-8').decode(bytes)
  }
  let encodedText = ''
  for (let index = 0; index < bytes.length; index += 1) {
    encodedText += `%${bytes[index].toString(16).padStart(2, '0')}`
  }
  try {
    return decodeURIComponent(encodedText)
  } catch (error) {
    return ''
  }
}

/**
 * 将导出接口的 JSON 错误 ArrayBuffer 解码为对象，成功 CSV 保持原始二进制。
 * @param {string|ArrayBuffer|ArrayBufferView|Object} data axios 响应体。
 * @param {Object} headers axios 响应头，用于判断 JSON 错误体。
 * @returns {Object|ArrayBuffer|ArrayBufferView|string} JSON 错误对象或原始响应体。
 */
export function transformExportResponse(data, headers) {
  if (data === null || data === undefined || (typeof data === 'object' && !toUint8Array(data))) {
    return data
  }
  const contentType = getResponseHeader(headers, 'content-type')
  const text = decodeResponseText(data)
  const normalizedText = text.replace(/^\uFEFF/, '').trim()
  const looksLikeJson = /json/i.test(contentType) || normalizedText.charAt(0) === '{'
  if (!looksLikeJson) {
    return data
  }
  try {
    const parsed = JSON.parse(normalizedText)
    return parsed && typeof parsed === 'object' ? parsed : data
  } catch (error) {
    return data
  }
}

/**
 * 查询保密区自动删权审计记录，筛选和园区数据范围由服务端校验。
 * @param {Object} query 分页参数与报表筛选条件。
 * @returns {Promise} 管理端分页接口响应。
 */
export function fetchPage(query) {
  return request({
    url: '/platform/security/auth/delete/log/page',
    method: 'get',
    params: query
  })
}

/**
 * 下载当前筛选条件下的 CSV 报表，文件编码与记录上限由服务端保证。
 * @param {Object} query 报表筛选条件，不包含客户端园区列表或分页大小。
 * @returns {Promise} CSV 二进制响应。
 */
export function exportLogs(query) {
  return request({
    url: '/platform/security/auth/delete/log/export',
    method: 'get',
    params: query,
    responseType: 'arraybuffer',
    timeout: 1000 * 60 * 5,
    transformResponse: [transformExportResponse]
  })
}

/**
 * 查询单条审计记录关联的设备任务，后端会先验证审计记录归属园区。
 * @param {string|number} id 审计记录主键，使用字符串兼容大整数。
 * @returns {Promise} 任务明细接口响应。
 */
export function fetchTasks(id) {
  return request({
    url: `/platform/security/auth/delete/log/${encodeURIComponent(String(id))}/tasks`,
    method: 'get'
  })
}
