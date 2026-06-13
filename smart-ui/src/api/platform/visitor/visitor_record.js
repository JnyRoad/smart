
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/visitor/page',
    method: 'get',
    params: query
  })
}

export function delAuth (data) {
  return request({
    url: '/platform/admittance/apply/del/auth',
    method: 'post',
    params: data
  })
}

export function reSend (data) {
  return request({
    url: '/platform/admittance/apply/repeat/auth',
    method: 'post',
    params: data
  })
}

export function reSend1 (data) {
  return request({
    url: '/platform/visitor/repeat/visitor/auth',
    method: 'post',
    params: data
  })
}
