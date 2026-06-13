
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/admin/dict/page',
    method: 'get',
    params: query
  })
}

export function addObj (obj) {
  return request({
    url: '/admin/dict/save',
    method: 'post',
    data: obj
  })
}

export function getObj (id) {
  return request({
    url: '/admin/dict/' + id,
    method: 'get'
  })
}

export function delObj (row) {
  return request({
    url: '/admin/dict/' + row.id + '/' + row.type,
    method: 'post'
  })
}

export function putObj (obj) {
  return request({
    url: '/admin/dict/update',
    method: 'post',
    data: obj
  })
}

export function remote (type) {
  return request({
    url: '/admin/dict/type/' + type,
    method: 'get'
  })
}
