
import request from '@/router/axios'


export function fetchList(query, obj) {
  return request({
    url: '/platform/badge/loss/page',
    method: 'post',
    data: obj,
    params: query
  })
}

export function exportList(obj) {
  return request({
    url: '/platform/badge/loss/excel',
    method: 'post',
    data: obj
  })
}
