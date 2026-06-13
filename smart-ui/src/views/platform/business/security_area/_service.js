import request from '@/router/axios'
export function fetchList(param) {
  return request({
    url: `/platform/security/auth/delete/getPage`,
    method: 'get',
    params: param
  })
}
export const bsSecurityAreaApi = {
  editWhiteListObj (data) {
    return request({
      url: `/platform/security/auth/delete`,
      method: 'post',
      data: data
    })
  },
  getWhiteListObj (query) {
    return request({
      url: `/platform/security/auth/delete/get`,
      method: 'get',
      params: query
    })
  },
  editAreaListObj (data) {
    return request({
      url: `/platform/oa/area`,
      method: 'post',
      data: data
    })
  },
  getOaArea(query){
    return request({
      url: `/platform/oa/area/list`,
      method: 'get',
      params: query
    })
  },
  doSyncTask () {
    return request({
      url: `/platform/admittance/area/type/sync/${1}`,
      method: 'get'
    })
  },
  getPersonAuthes(parkId, query){
    return request({
      url: `/platform/device/authority/list/1/${parkId}`,
      method: 'get',
      params: query
    })
  },
}