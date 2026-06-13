
import request from '@/router/axios'



export function fetchList (query) {
  return request({
    url: '/platform/social/security/page',
    method: 'get',
    params: query
  })
}
export function delObj (id) {
  return request({
    url: '/platform/social/security/delete/'+id,
    method: 'get'
  })
}
export function addObj (obj) {
  return request({
    url: '/platform/social/security/add',
    method: 'post',
    data: obj
  })
}
export function updateObj (obj) {
  return request({
    url: '/platform/social/security/update',
    method: 'post',
    data: obj
  })
}
