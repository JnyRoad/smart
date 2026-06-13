
import request from '@/router/axios'

// export function fetchList (obj) {
//   return request({
//     url: '/platform/appmsg/query/page',
//     method: 'post',
//     data: obj
//   })
// }
export function fetchList (query,obj) {
  return request({
    url: '/platform/appmsg/query/page',
    method: 'post',
    data: obj,
    params: query
  })
}
export function getById (id) {
  return request({
    url: '/platform/appmsg/query/'+id,
    method: 'get'
  })
}
export function getAllTemplate () {
  return request({
    url: '/platform/appmsg/template/all',
    method: 'get'
  })
}
export function getAllState () {
  return request({
    url: '/platform/appmsg/send/state',
    method: 'get'
  })
}
