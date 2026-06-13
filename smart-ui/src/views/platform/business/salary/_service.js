
import request from '@/router/axios'


export function fetchList(query, obj) {
  return request({
    url: '/platform/ehr/setup/page',
    method: 'post',
    data: obj,
    params: query
  })
}

export function editObj(obj) {
  return request({
    url: '/platform/ehr/setup/edit',
    method: 'post',
    data: obj
  })
}
