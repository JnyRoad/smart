import request from '@/router/axios'

export function addObj (obj) {
  return request({
    url: '/',
    method: 'post',
    data: obj
  })
}

export function loggedCount () {
  return request({
    url: '/admin/user/logged/count',
    method: 'get'
  })
}