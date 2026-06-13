
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/vehicle/black/page',
    method: 'get',
    params: query
  })
}
export function getObj (id) {
  return request({
    url: '/platform/vehicle/black/' + id,
    method: 'get'
  })
}
export function saveObj (obj) {
  return request({
    url: '/platform/vehicle/black/save',
    method: 'post',
    data: obj
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/vehicle/black/update',
    method: 'post',
    data: obj
  })
}
export function delObj (id) {
  return request({
    url: '/platform/vehicle/black/delete/' + id,
    method: 'get'
  })
}
export function plate (obj) {
  return request({
    url: '/platform/vehicle/black/plate',
    method: 'post',
    data: obj
  })
}
export function batch (obj) {
  return request({
    url: '/platform/vehicle/black/delete/batch',
    method: 'post',
    data: obj
  })
}
