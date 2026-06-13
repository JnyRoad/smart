
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/admin/client/page',
    method: 'get',
    params: query
  })
}

export function addObj (obj) {
  return request({
    url: '/admin/client/save',
    method: 'post',
    data: obj
  })
}

export function getObj (id) {
  return request({
    url: '/admin/client/' + id,
    method: 'get'
  })
}

export function delObj (id) {
  return request({
    url: '/admin/client/' + id,
    method: 'post'
  })
}

export function putObj (obj) {
  return request({
    url: '/admin/client/update',
    method: 'post',
    data: obj
  })
}
