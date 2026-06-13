
import request from '@/router/axios'
export function getDetails (id) {
  return request({
    url: '/platform/vehicle/' + id,
    method: 'get',
  })
}
export function addObj (obj) {
  return request({
    url: '/platform/vehicle/save',
    method: 'post',
    data: obj
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/vehicle/update',
    method: 'post',
    data: obj
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

export function authorityList (parkId) {
  return request({
    url: '/platform/device/authority/list/3/' +  parkId,
    method: 'get'
  })
}
