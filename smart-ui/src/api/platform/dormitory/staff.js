
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/staff/page',
    method: 'get',
    params: query
  })
}
export function allList () {
  return request({
    url: '/platform/park/allList',
    method: 'post'
  })
}
//修改入住时间
export function updateDormitoryStaff (query) {
  return request({
    url: '/platform/dormitory/staff/updateDormitoryStaff',
    method: 'post',
    data: query
  })
}
