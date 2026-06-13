
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/staff/toStaff/page',
    method: 'get',
    params: query
  })
}
export function getById (id) {
  return request({
    url: '/platform/staff/toStaff/'+id,
    method: 'get',
    data: id
  })
}