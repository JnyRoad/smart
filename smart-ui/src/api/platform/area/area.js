
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/area/page',
    method: 'get',
    params: query
  })
}
export function putObj (query) {
  return request({
    url: '/platform/area/update',
    method: 'post',
    data: query
  })
}
export function addObj (query) {
  return request({
    url: '/platform/area/save',
    method: 'post',
    data: query
  })
}
export function delObj (id) {
  return request({
    url: '/platform/area/delete/' + id,
    method: 'get'
  })
}
export function allObj () {
  return request({
    url: '/platform/area/all',
    method: 'get'
  })
}
