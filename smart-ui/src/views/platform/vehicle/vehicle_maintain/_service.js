import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/vehicle/xc-page',
    method: 'get',
    params: query
  })
}

export function addObj (obj) {
  return request({
    url: '/platform/vehicle/xc-save',
    method: 'post',
    data: obj
  })
}

export function putObj (obj) {
  return request({
    url: '/platform/vehicle/xc-update',
    method: 'post',
    data: obj
  })
}

export function getObj (id) {
  return request({
    url: '/platform/vehicle/xc/' + id,
    method: 'get'
  })
}

export function delObj (id) {
  return request({
    url: '/platform/vehicle/xc-delete/' + id,
    method: 'get'
  })
}

export function getStaffDetail (badge) {
  return request({
    url: '/platform/vehicle/staff/detail/' + badge,
    method: 'get'
  })
}
export function plate (obj) {
  return request({
    url: '/platform/vehicle/plate',
    method: 'post',
    data: obj
  })
}