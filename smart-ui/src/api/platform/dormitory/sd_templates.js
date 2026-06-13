
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/sd/page',
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
export function addTemplate (params) {
  return request({
    url: '/platform/dormitory/sd/add',
    method: 'post',
    data:params
  })
}
export function delTemplate (id) {
  return request({
    url: '/platform/dormitory/sd/'+id,
    method: 'post'
  })
}
export function updateDormitorySDTemplate (params) {
  return request({
    url: '/platform/dormitory/sd/update',
    method: 'post',
    data:params
  })
}
export function addSDTemplateRule (params) {
  return request({
    url: '/platform/dormitory/sdrule/add',
    method: 'post',
    data:params
  })
}
export function querySDTemplateRule (id) {
  return request({
    url: '/platform/dormitory/sdrule/'+id,
    method: 'get'
  })
}

export function queryJobJchenList () {
  return request({
    url: '/platform/dormitory/sd/jchenList',
    method: 'get'
  })
}
