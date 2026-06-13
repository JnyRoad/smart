
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/snap/vehicle/page',
    method: 'get',
    params: query
  })
}
export function getObj (id) {
  return request({
    url: '/platform/parking/count/' + id,
    method: 'get'
  })
}
export function parking () {
  return request({
    url: '/platform/parking/getParking',
    method: 'get'
  })
}
export function check (obj) {
  return request({
    url: '/platform/parking/correction/saveorupdate',
    method: 'post',
    data: obj
  })
}