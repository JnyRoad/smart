
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/page',
    method: 'get',
    params: query
  })
}
export function putDormObj (query) {
  return request({
    url: '/platform/dormitory/updateDormitory',
    method: 'post',
    data: query
  })
}
export function addObj (query) {
  return request({
    url: '/platform/dormitory/addDormitory',
    method: 'post',
    data: query
  })
}
export function delDormObj (id) {
  return request({
    url: '/platform/dormitory/' + id,
    method: 'post'
  })
}
export function getDormObj (id) {
  return request({
    url: '/platform/dormitory/' + id,
    method: 'get'
  })
}