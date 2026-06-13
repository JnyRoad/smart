
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/device/task/person/page',
    method: 'get',
    params: query
  })
}
export function fetchISCList (query) {
  return request({
    url: '/platform/device/task/isc/person/page',
    method: 'get',
    params: query
  })
}
export function getTree (type) {
  return request({
    url: '/platform/task/down/park/' + type,
    method: 'get'
  })
}
export function updateTaskStauts (query) {
  return request({
    url: '/platform/device/task/updateStatus',
    method: 'get',
    params: query
  })
}
