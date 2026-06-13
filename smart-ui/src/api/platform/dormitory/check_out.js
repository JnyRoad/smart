
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/staff/history/page',
    method: 'get',
    params: query
  })
}
export function delObj (query) {
  return request({
    url: '/platform/dormitory/staff/history/delete/' + query,
    method: 'post',
  })
}
export function allList () {
  return request({
    url: '/platform/park/allList',
    method: 'post'
  })
}
//修改退宿时间
export function updateCheckOutTime (query) {
  return request({
    url: '/platform/dormitory/staff/history/update',
    method: 'post',
    data: query
  })
}
