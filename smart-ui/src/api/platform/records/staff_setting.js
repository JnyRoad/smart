
import request from '@/router/axios'


export function fetchList () {
  return request({
    url: '/platform/ehr/to/staff/set/list',
    method: 'get'
  })
}
export function addList (obj) {
  return request({
    url: '/platform/ehr/to/staff/set/addList',
    method: 'post',
    data: obj
  })
}
