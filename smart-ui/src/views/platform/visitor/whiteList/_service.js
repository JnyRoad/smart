
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/visitor/white/page',
    method: 'get',
    params: query
  })
}
export function addObj (obj) {
  return request({
    url: '/platform/visitor/white/save',
    method: 'post',
    data: obj
  })
}
export function delObj (obj) {
  return request({
    url: '/platform/visitor/white/batch/del',
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