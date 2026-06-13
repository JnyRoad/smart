import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/vehicle/not/staff/page',
    method: 'get',
    params: query
  })
}
export function addObj (obj) {
  return request({
    url: '/platform/vehicle/not/staff/save',
    method: 'post',
    data: obj
  })
}
export function delObj (id) {
  return request({
    url: '/platform/vehicle/not/staff/delete/' + id,
    method: 'get'
  })
}
export function getDetails (id) {
  return request({
    url: '/platform/vehicle/not/staff/' + id,
    method: 'get',
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/vehicle/not/staff/update',
    method: 'post',
    data: obj
  })
}
export function plate (obj) {
  return request({
    url: '/platform/vehicle/plate',
    method: 'post',
    data: obj
  })
}