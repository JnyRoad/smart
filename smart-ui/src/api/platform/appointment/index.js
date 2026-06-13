import request from '@/router/axios'

export function addObj (obj) {
  return request({
    url: '/admin/menu',
    method: 'post',
    data: obj
  })
}
export function delObj (id) {
  return request({
    url: '/admin/dept/' + id,
    method: 'post'
  })
}