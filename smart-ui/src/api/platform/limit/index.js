
import request from '@/router/axios'

export function fetchMenuTree (query) {
  return request({
    url: '/admin/menu/tree',
    method: 'get',
    params: query
  })
}
