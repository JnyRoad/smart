
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/device/person/page',
    method: 'get',
    params: query
  })
}
export function delObj (obj) {
  return request({
    url: '/platform/device/task/delete',
    method: 'post',
    data: obj
  })
}
