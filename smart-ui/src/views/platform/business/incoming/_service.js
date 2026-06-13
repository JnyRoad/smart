import request from '@/router/axios'
export const inComingApi = {
  getAuthConfig(query){
    return request({
      url: `/platform/admittance/auth/list`,
      method: 'get',
      params: query
    })
  },
  saveAuthConfig(data){
    return request({
      url: `/platform/admittance/auth/edit`,
      method: 'post',
      data: data
    })
  },
  getAllAuthes(parkId){
    return request({
      url: `/platform/device/authority/list/1/${parkId}`,
      method: 'get'
    })
  },


  editWhiteListObj (data) {
    return request({
      url: `/platform/security/auth/delete`,
      method: 'post',
      data: data
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
      url: `/platform/admittance/area/type/sync/${2}`,
      method: 'get'
    })
  },
  getAreaType (query) {
    return request({
      url: `/platform/admittance/apply/enum/factory/type`,
      method: 'get',
      params: query
    })
  },
  getAreaOptions (query) {
    return request({
      url: `/platform/admittance/apply/app/area-options`,
      method: 'get',
      params: query
    })
  },
  getJcheId(param) {
    return request({
      url: '/platform/visit/limit/admittance/list/jche',
      method: 'get',
      params: param
    })
  },
  editJche (obj) {
    return request({
      url: '/platform/visit/limit/admittance/edit',
      method: 'post',
      data: obj
    })
  }
}
