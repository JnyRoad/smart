
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/message/template/all',
    method: 'get',
    params: query
  })
}
