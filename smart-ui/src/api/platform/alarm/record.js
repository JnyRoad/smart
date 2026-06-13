
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/alarm/record/page',
    method: 'get',
    params: query
  })
}
export function tree () {
  return request({
    url: '/platform/device/area/tree',
    method: 'get'
  })
}