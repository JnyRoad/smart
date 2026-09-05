
import request from '@/router/axios'

/**
 * 按手动授权对象筛选后端返回的权限组。
 * @param {Array<Object>} authorities 申请单授权选项中的权限组完整列表。
 * @returns {Array<Object>} ISC 人员类型权限组，不修改输入列表。
 */
export function filterManualAuthAuthorities (authorities) {
  return (Array.isArray(authorities) ? authorities : []).filter(item => item.type === 1)
}

/**
 * 构造手动授权请求载荷，避免把客户端日期或员工身份字段发送到接口。
 * @param {Object} options 申请单 ID、人员身份和权限 ID 数组。
 * @returns {Object} 只含后端契约字段的请求 body。
 */
export function buildManualAuthPayload ({ applyId, fellowId, authIds }) {
  const payload = {
    applyId,
    authIds: Array.isArray(authIds) ? authIds.slice() : []
  }
  payload.fellowId = fellowId
  return payload
}

export const xcIncomingRecordApi = {
  getList (query) {
    return request({
      url: '/platform/admittance/apply/page',
      method: 'get',
      params: query
    })
  },
  getDetail(id){
    return request({
      url: `/platform/admittance/apply/search/Detail/${id}`,
      method: 'get'
    })
  },
  getCauseEnum(id){
    return request({
      url: `/platform/admittance/apply/enum/cause`,
      method: 'get'
    })
  },
  reSend(data){
    return request({
      url: `/platform/admittance/apply/repeat/auth`,
      method: 'post',
      params: data
    })
  },
  revoke(data){
    return request({
      url: '/platform/manage/admittance/apply/revoke',
      method: 'post',
      data: data
    })
  },
  /**
   * 查询申请单可手动下发的人员和权限组。
   * @param {string|number} applyId 申请单 ID；请求只读且不改变页面状态。
   * @returns {Promise} 网关返回的申请单授权选项；网络或权限错误由调用方处理。
   */
  getManualAuthOptions (applyId) {
    return request({
      url: '/platform/manage/admittance/apply/device/auth/options',
      method: 'get',
      params: { applyId }
    })
  },
  /**
   * 提交单个访客对象的手动权限下发任务。
   * @param {Object} data 只允许包含申请单、人员身份及权限 ID；不包含日期字段。
   * @returns {Promise} 网关返回的批次号；失败由调用方展示并允许重试。
   */
  submitManualAuth (data) {
    return request({
      url: '/platform/manage/admittance/apply/device/auth',
      method: 'post',
      data
    })
  },
  getFactoryList(){
    return request({
      url: `/platform/admittance/area/type/security/factory/list`,
      method: 'get'
    })
  },
  getAreaType(data){
    return request({
      url: `/platform/admittance/apply/enum/factory/type`,
      method: 'get',
      params: data
    })
  },
}
