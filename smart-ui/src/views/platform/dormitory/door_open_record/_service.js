
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/staff/lock/open-door/record/page',
    method: 'get',
    params: query
  })
}

export function exportShareData (query) {
  return request({
    url: `/platform/dormitory/staff/lock/open-door/record/export`,
    method: 'get',
    params: query
  })
}

// exportShareData
