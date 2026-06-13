
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/level/page',
    method: 'get',
    params: query
  })
}