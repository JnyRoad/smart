
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/task/down/vehicle/page',
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