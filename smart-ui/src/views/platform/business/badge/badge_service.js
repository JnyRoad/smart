
import request from '@/router/axios'


export function fetchList(param) {
  return request({
    url: '/platform/badge/config/page',
    method: 'get',
    params: param
  })
}

export function editObj(obj) {
  return request({
    url: '/platform/badge/config/edit',
    method: 'post',
    data: obj
  })
}

export function getObj(id) {
  return request({
    url: '/platform/badge/config/' + id,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: '/platform/badge/config/' + id,
    method: 'post'
  })
}
