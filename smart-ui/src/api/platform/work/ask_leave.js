
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/application/askLeave/page/list',
    method: 'get',
    params: query
  })
}
export function getById (id) {
  return request({
    url: '/platform/application/askLeave/list/detail/'+id,
    method: 'get',
    data: id
  })
}
