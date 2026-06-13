
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/staff/lock/device/page',
    method: 'get',
    params: query
  })
}


export function recordList (query) {
  return request({
    url: '/platform/dormitory/staff/lock/task/record/page',
    method: 'get',
    params: query
  })
}
