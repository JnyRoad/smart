
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/staff/statementdetail/page',
    method: 'get',
    params: query
  })
}
