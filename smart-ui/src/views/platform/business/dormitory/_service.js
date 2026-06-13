
import request from '@/router/axios'


export function fetchList(query, obj) {
  return request({
    url: '/platform/dormitory/administrator/page',
    method: 'post',
    data: obj,
    params: query
  })
}

export function editObj(obj) {
  return request({
    url: '/platform/dormitory/administrator/save',
    method: 'post',
    data: obj
  })
}
