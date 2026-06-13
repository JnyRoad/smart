
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/staff/lock/permission/page',
    method: 'get',
    params: query
  })
}

export const authApi = {
  getLockData (query) {
    return request({
      url: '/platform/dormitory/staff/lock/device/page',
      method: 'get',
      params: query
    })
  },
  checkPerson (queryName) {
    return request({
      url: `/platform/dormitory/staff/lock/person/search/${queryName}`,
      method: 'get'
    })
  },
  addObjBatch (obj) {
    return request({
      url: '/platform/dormitory/staff/lock/permission/batch',
      method: 'post',
      data: obj
    })
  },
  putObj (obj) {
    return request({
      url: '/platform/dormitory/staff/lock/permission/edit',
      method: 'post',
      data: obj
    })
  },
  reAuth(obj){
    return request({
      url: '/platform/dormitory/staff/lock/permission/reAuth',
      method: 'post',
      data: obj
    })
  },
  delObj (id) {
    return request({
      url: '/platform/dormitory/staff/lock/permission/del/' + id,
      method: 'post'
    })
  },
  cancelAuth (id) {
    return request({
      url: `/platform/dormitory/staff/lock/permission/cancelAuth/${id}`,
      method: 'post'
    })
  },
}
