
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/vehicle/apply/page',
    method: 'get',
    params: query
  })
}
export function getObj (id) {
  return request({
    url: '/platform/vehicle/apply/' + id,
    method: 'get'
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/vehicle/apply/update',
    method: 'post',
    data: obj
  })
}
