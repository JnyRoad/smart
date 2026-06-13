
import request from '@/router/axios'


export function fetchList (query) {
  return request({
    url: '/platform/ehr/to/staff/page',
    method: 'get',
    params: query
  })
}
