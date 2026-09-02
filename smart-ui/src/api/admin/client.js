
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/admin/client/page',
    method: 'get',
    params: query
  })
}

/**
 * 获取由后端维护的 OAuth capability scope 目录。
 * 前端只用于展示和选择，创建/更新时仍由服务端再次校验。
 * @returns {Promise} data 为 scope 目录数组
 */
export function fetchScopes () {
  return request({
    url: '/admin/client/scopes',
    method: 'get'
  })
}

export function addObj (obj) {
  return request({
    url: '/admin/client/save',
    method: 'post',
    data: obj
  })
}

export function getObj (id) {
  return request({
    url: '/admin/client/' + id,
    method: 'get'
  })
}

export function delObj (id) {
  return request({
    url: '/admin/client/' + id,
    method: 'post'
  })
}

export function putObj (obj) {
  return request({
    url: '/admin/client/update',
    method: 'post',
    data: obj
  })
}

/**
 * 重置指定 App 的 App Secret。
 * 后端生成新明文并落库编码后的值，响应 data 是一次性明文，仅本次可见。
 * @param {string} clientId App ID
 * @returns {Promise} data 为新的明文 App Secret
 */
export function resetSecret (clientId) {
  return request({
    url: '/admin/client/secret/' + clientId,
    method: 'put'
  })
}

/**
 * additional_information 列是整段 JSON 文本，除本页维护的 allowedParkIds（授权园区 id 数组）外，
 * 可能还存有其他业务写入的键。保存时必须防御性 merge：只覆盖 allowedParkIds，其余键原样保留，
 * 避免把别的功能写进去的数据静默冲掉。
 *
 * @param {string|null|undefined} rawAdditionalInformation 原始 additional_information 字符串
 * @param {number[]} allowedParkIds 本次要写入的授权园区 id 数组
 * @returns {{ text: string, parseError: boolean }}
 *   text：merge 后应写回 additional_information 的 JSON 字符串；
 *   parseError：原字符串非空但解析失败时为 true，调用方需要提示用户而不是静默覆盖旧数据。
 */
export function mergeAllowedParkIds (rawAdditionalInformation, allowedParkIds) {
  let base = {}
  let parseError = false

  if (rawAdditionalInformation) {
    try {
      const parsed = JSON.parse(rawAdditionalInformation)
      // 只有解析结果是普通对象才能安全 merge；数组/基本类型/null 视为异常格式
      if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
        base = parsed
      } else {
        parseError = true
      }
    } catch (e) {
      parseError = true
    }
  }

  // 解析失败时，绝不能用 { allowedParkIds } 直接覆盖，否则会把原有的其他键静默冲掉；
  // 这里保留一份「除 allowedParkIds 外为空」的对象，只承载本次要写入的值，
  // 具体是否继续保存由调用方根据 parseError 提示用户后决定。
  const merged = { ...base, allowedParkIds: allowedParkIds || [] }
  return { text: JSON.stringify(merged), parseError }
}
