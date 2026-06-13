
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/parking/page',
    method: 'get',
    params: query
  })
}
export function getObj (id) {
  return request({
    url: '/platform/parking/' + id,
    method: 'get'
  })
}
export function saveObj (obj) {
  return request({
    url: '/platform/parking/save',
    method: 'post',
    data: obj
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/parking/update',
    method: 'post',
    data: obj
  })
}
export function delObj (id) {
  return request({
    url: '/platform/parking/delete/' + id,
    method: 'get'
  })
}
