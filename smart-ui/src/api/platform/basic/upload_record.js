
import request from '@/router/axios'

export function roleList () {
  return request({
    url: '/admin/role/roleList',
    method: 'get'
  })
}
export function fetchList (query) {
  return request({
    url: '/platform/staff/photo/upload/page',
    method: 'get',
    params: query
  })
}
export function getById (id) {
  return request({
    url: '/platform/staff/photo/upload/detail/'+id,
    method: 'get',
    data: id
  })
}
