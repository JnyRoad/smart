
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/white/job/page',
    method: 'get',
    params: query
  })
}
export function addObj (obj) {
  return request({
    url: '/platform/white/job/add',
    method: 'post',
    data: obj
  })
}
export function delObj (obj) {
  return request({
    url: '/platform/white/job/delete',
    method: 'post',
    data: obj
  })
}
