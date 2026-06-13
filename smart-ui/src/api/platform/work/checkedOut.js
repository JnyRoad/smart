
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dor/quit/page',
    method: 'get',
    params: query
  })
}

export function getById (id) {
  return request({
    url: `/platform/dor/quit/detail/${id}`,
    method: 'get'
  })
}
