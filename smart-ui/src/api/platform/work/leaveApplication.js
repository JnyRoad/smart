
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/leave/application/page',
    method: 'get',
    params: query
  })
}
export function getById (id) {
  return request({
    url: '/platform/leave/application/detail/'+id,
    method: 'get'
  })
}
