
import request from '@/router/axios'
export function fetchList (query) {
  return request({
    url: '/platform/vehicle/page',
    method: 'get',
    params: query
  })
}
export function addObj (id) {
  return request({
    url: '/platform/vehicle/' + id,
    method: 'post'
  })
}
export function delObj (id) {
  return request({
    url: '/platform/vehicle/delete/' + id,
    method: 'get'
  })
}