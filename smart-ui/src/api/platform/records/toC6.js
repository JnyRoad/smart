
import request from '@/router/axios'


export function fetchList (query) {
  return request({
    url: '/platform/toC6/page',
    method: 'get',
    params: query
  })
}
