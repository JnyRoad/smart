
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/snap/vehicle/page',
    method: 'get',
    params: query
  })
}