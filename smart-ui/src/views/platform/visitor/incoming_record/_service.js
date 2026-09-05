
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
