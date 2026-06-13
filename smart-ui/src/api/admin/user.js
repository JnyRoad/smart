
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/admin/user/page',
    method: 'get',
    params: query
  })
}

export function addObj (obj) {
  return request({
    url: '/admin/user/save',
    method: 'post',
    data: obj
  })
}

export function getObj (id) {
  return request({
    url: '/admin/user/' + id,
    method: 'get'
  })
}

export function delObj (id) {
  return request({
    url: '/admin/user/' + id,
    method: 'post'
  })
}

export function putObj (obj) {
  return request({
    url: '/admin/user/update',
    method: 'post',
    data: obj
  })
}

export function getDetails (obj) {
  return request({
    url: '/admin/user/details/' + obj,
    method: 'get'
  })
}
