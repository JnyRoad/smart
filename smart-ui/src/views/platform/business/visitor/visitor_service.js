
import request from '@/router/axios'


export function fetchList(param) {
  return request({
    url: '/platform/visit/limit/page',
    method: 'get',
    params: param
  })
}

export function getJcheId(param) {
  return request({
    url: '/platform/visit/limit/list/jche',
    method: 'get',
    params: param
  })
}

export function delObj(id) {
  return request({
    url: '/platform/badge/config/' + id,
    method: 'post'
  })
}

//查询邮件推送设置信息
export function fetchListEmail (query) {
  return request({
    url: '/platform/visitor/push/email/searchAll',
    method: 'get',
    params: query
  })
}
//添加 邮件推送设置信息
export function addObj (obj) {
  return request({
    url: '/platform/visitor/push/email/add',
    method: 'post',
    data: obj
  })
}
//修改 邮件推送设置信息
export function editObj (obj) {
  return request({
    url: '/platform/visitor/push/email/update',
    method: 'post',
    data: obj
  })
}

export function fetchAuthList (query) {
  return request({
    url: '/platform/device/authority/page',
    method: 'get',
    params: query
  })
}

export function fetchAuthId (query) {
  return request({
    url: '/platform/business/auth/id',
    method: 'get',
    params: query
  })
}

export function editJche (obj) {
  return request({
    url: '/platform/visit/limit/edit',
    method: 'post',
    data: obj
  })
}

export function editAuth (obj) {
  return request({
    url: '/platform/business/auth/edit',
    method: 'post',
    data: obj
  })
}
/**
 * 批量编辑配置项
 * @param {Object[]} arr -
 * @param {number} arr[].businessType - 预约类型[1:访客预约,2:入厂申请]
 * @param {number} arr[].configType - 配置类型[1:访客邀约,2:访客提示,7:H5常用区域]
 * @param {string} arr[].config - 配置类型 json字符串 --访客邀约:"{\"needApproval\":1}"/访客提示:"{\"isNeedNotice\":1,\"content\":\"test\"}"
 * @param {number} arr[].parkId - 园区id
 */
export function saveBatchConfig (arr) {
  return request({
    url: '/platform/common/config/batch/edit/config',
    method: 'post',
    data: arr
  })
}
/**
 * 根据类型获取配置
 * @param {Object} obj -
 * @param {array} obj.businessTypes - 预约类型[1:访客预约,2:入厂申请]
 * @param {array} obj.configTypes - 配置类型[1:访客邀约,2:访客提示,7:H5常用区域]
 * @param {number} obj.parkId - 园区id
 */
export function getBatchConfig (obj) {
  return request({
    url: '/platform/common/config/getByType',
    method: 'post',
    data: obj
  })
}
/**
 * 疫情管控配置-合肥
 */
export function saveEpide (obj) {
  return request({
    url: '/platform/common/config/edit/config',
    method: 'post',
    data: obj
  })
}
