
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/out/dormitory/staff/page/list',
    method: 'get',
    params: query
  })
}
export function getById (id) {
  return request({
    url: '/platform/out/dormitory/staff/detail/'+id,
    method: 'get',
    data: id
  })
}