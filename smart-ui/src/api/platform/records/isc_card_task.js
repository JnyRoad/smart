import request from '@/router/axios'

export function fetchList(query) {
  return request({
    url: '/platform/isc/card/task/page',
    method: 'get',
    params: query
  })
}
