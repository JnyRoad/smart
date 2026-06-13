
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/device/page',
    method: 'get',
    params: query
  })
}
export function getObj (id) {
  return request({
    url: '/platform/device/' + id,
    method: 'get'
  })
}
export function addObj (obj) {
  return request({
    url: '/platform/device/save',
    method: 'post',
    data: obj
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/device/update',
    method: 'post',
    data: obj
  })
}
export function delObj (id) {
  return request({
    url: '/platform/device/delete/' + id,
    method: 'get'
  })
}
export function tree () {
  return request({
    url: '/platform/device/area/tree',
    method: 'get'
  })
}
export function parking () {
  return request({
    url: '/platform/parking/getParking',
    method: 'get'
  })
}
export function getDeviceFT (parkId) {
  return request({
    url: `/platform/device/query/deviceFT/${parkId}`,
    method: 'get'
  })
}
export function setDeviceFT (data) {
  return request({
    url: '/platform/device/set/deviceFT',
    method: 'post',
    data: data
  })
}
