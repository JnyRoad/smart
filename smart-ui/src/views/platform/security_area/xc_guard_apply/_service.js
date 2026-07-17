
import request from '@/router/axios'
export const xcGuardApplyApi = {
  getList (page, query) {
    return request({
      url: '/platform/security/auth/apply/page',
      method: 'post',
      params: page,
      data: query
    })
  },
  doSend(id){
    return request({
      url: `/platform/security/auth/apply/${id}/dispatch`,
      method: 'post'
    })
  },
  getDispatchProgress(id, batchId){
    return request({
      url: `/platform/security/auth/apply/${id}/dispatch/${batchId}`,
      method: 'get'
    })
  },
  getZoneList (query) {
    return request({
      url: '/platform/oa/area/auth/name',
      method: 'get',
      params: query
    })
  },
  getAuthList (query) {
    return request({
      url: '/platform/oa/area/list/auth',
      method: 'get',
      params: query
    })
  },
  getAuthForFactory(query){
    return request({
      url: '/platform/security/auth/apply/enum/factory/type',
      method: 'get',
      params: query
    })
  },
  getStaffAgain(data){
    return request({
      url: '/platform/security/zone/query/Staff',
      method: 'post',
      data: data
    })
  },
  queryRemark(data){
    return request({
      url: '/platform/security/zone/query/remark',
      method: 'post',
      data: data
    })
  },
  doApply(data){
    return request({
      url: '/platform/security/auth/apply/save',
      method: 'post',
      data: data
    })
  },
  getApplyDetail(id){
    return request({
      url: `/platform/security/auth/apply/${id}`,
      method: 'get'
    })
  },
  getApplyDetailPerson(applyId){
    return request({
      url: `/platform/security/task/details/list/${applyId}`,
      method: 'get'
    })
  },
  reUploadImg(data){
    return request({
      url: `/platform/security/task/details/reload`,
      method: 'post',
      data: data
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
