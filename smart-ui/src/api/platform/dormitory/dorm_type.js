
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/type/page',
    method: 'get',
    params: query
  })
}
export function addObj (query) {
  return request({
    url: '/platform/dormitory/type/addDormitoryType',
    method: 'post',
    data: query
  })
}
export function putObj (query) {
  return request({
    url: '/platform/dormitory/type/updateDormitoryType',
    method: 'post',
    data: query
  })
}
export function delObj (id) {
  return request({
    url: '/platform/dormitory/type/'+id,
    method: 'post'
  })
}