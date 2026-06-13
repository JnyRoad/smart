
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/wechat/banding/page',
    method: 'post',
    data: query
  })
}

export function authDelete (id) {
  return request({
    url: `/platform/wechat/banding/${id}`,
    method: 'post'
  })
}
