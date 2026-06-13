
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/admin/log/page',
    method: 'get',
    params: query
  })
}