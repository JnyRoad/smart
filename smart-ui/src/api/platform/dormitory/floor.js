
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/floor/page',
    method: 'get',
    params: query
  })
}
export function queryDormitory (params) {
  return request({
    url: '/platform/dormitory/queryDormitory',
    method: 'post',
    data:params
  })
}
export function addFloor (params) {
  return request({
    url: '/platform/dormitory/floor/addFloor',
    method: 'post',
    data:params
  })
}
export function delFloor (id) {
  return request({
    url: '/platform/dormitory/floor/'+id,
    method: 'post'
  })
}
export function getFloor (id) {
  return request({
    url: '/platform/dormitory/floor/'+id,
    method: 'get'
  })
}
export function updateDormitoryFloor (params) {
  return request({
    url: '/platform/dormitory/floor/updateDormitoryFloor',
    method: 'post',
    data:params
  })
}
export function getFloorStartNum (dormitoryId) {
  return request({
    url: '/platform/dormitory/floor/getFloorStartNum/'+dormitoryId,
    method: 'get'
  })
}
