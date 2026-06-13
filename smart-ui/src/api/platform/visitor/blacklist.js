
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/black/visitor/page',
    method: 'get',
    params: query
  })
}

export function fetchHrList (query) {
  return request({
    url: '/platform/black/visitor/hr/page',
    method: 'get',
    params: query
  })
}
export function addObj (obj) {
  return request({
    url: '/platform/black/visitor/add',
    method: 'post',
    data: obj
  })
}
export function delObj (id) {
  return request({
    url: '/platform/black/visitor/delete/' + id,
    method: 'get'
  })
}

export function supplierImport (parkId,data) {
  return request({
    url: '/platform/black/visitor/import/' + parkId,
    method: 'post',
    data: data
  })
}
