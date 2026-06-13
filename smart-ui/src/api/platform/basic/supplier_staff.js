
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/supplier/staff/page',
    method: 'get',
    params: query
  })
}
export function update (obj ) {
  return request({
    url: '/platform/supplier/staff/update',
    method: 'post',
    data: obj
  })
}
export function add (obj ) {
  return request({
    url: '/platform/supplier/staff/add',
    method: 'post',
    data: obj
  })
}
export function remove (id) {
  return request({
     url: '/platform/supplier/staff/delete/'+id,
      method: 'get'
  })
}
export function getSupplierList (parkId) {
  return request({
     url: '/platform/supplier/getList/'+parkId,
      method: 'get'
  })
}
