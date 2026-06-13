import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/application/page',
    method: 'get',
    params: query
  })
}
export function getProcess (id) {
  return request({
    url: '/platform/application/getProcess/'+id,
    method: 'get'
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/application/updateApplicationList',
    method: 'post',
    data: obj
  })
}
export function putObjToStaff (obj) {
  return request({
    url: '/platform/application/updateApplicationToStaff',
    method: 'post',
    data: obj
  })
}
