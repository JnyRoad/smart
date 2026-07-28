
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/admin/social/page',
    method: 'get',
    params: query
  })
}

export function addObj (obj) {
  return request({
    url: '/admin/social/save',
    method: 'post',
    data: obj
  })
}

export function getObj (id) {
  return request({
    url: '/admin/social/' + id,
    method: 'get'
  })
}

export function delObj (id) {
  return request({
    url: '/admin/social/' + id,
    method: 'post'
  })
}

export function putObj (obj) {
  return request({
    url: '/admin/social/update',
    method: 'post',
    data: obj
  })
}

/** 显式轮换三方登录密钥，避免普通编辑请求携带或覆盖已有 appSecret。 */
export function rotateSecret (id, appSecret) {
  return request({
    url: '/admin/social/secret/' + id,
    method: 'put',
    data: { appSecret }
  })
}
