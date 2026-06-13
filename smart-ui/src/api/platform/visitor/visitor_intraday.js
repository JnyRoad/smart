
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/visitor/searchTodayVisitor',
    method: 'get',
    params: query
  })
}
export function searchNewSnapVisitor () {
  return request({
    url: '/platform/visitor/searchNewSnapVisitor',
    method: 'get'
  })
}