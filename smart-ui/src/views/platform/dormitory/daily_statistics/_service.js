
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/meterread/daily/byFloor/new',
    method: 'get',
    params: query
  })
}