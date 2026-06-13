
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/supplier/page',
    method: 'get',
    params: query
  })
}
export function add (obj ) {
  return request({
    url: '/platform/supplier/add',
    method: 'post',
    data: obj
  })
}
export function edit (obj ) {
  return request({
    url: '/platform/supplier/update',
    method: 'post',
    data: obj
  })
}
export function remove (id) {
  return request({
     url: '/platform/supplier/delete/'+id,
      method: 'get'
  })
}
