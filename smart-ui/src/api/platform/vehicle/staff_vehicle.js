
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/vehicle/page',
    method: 'get',
    params: query
  })
}
export function addObj (obj) {
  return request({
    url: '/platform/vehicle',
    method: 'post',
    data: obj
  })
}
export function getObj (id) {
  return request({
    url: '/platform/vehicle/' + id,
    method: 'get'
  })
}
export function delObj (id) {
  return request({
    url: '/platform/vehicle/delete/' + id,
    method: 'get'
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/vehicle/update',
    method: 'post',
    data: obj
  })
}
export function getWelfare () {
  return request({
    url: '/platform/vehicle/welfare/level',
    method: 'get'
  })
}