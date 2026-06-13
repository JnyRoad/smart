import request from '@/router/axios'

export function getById (id) {
  return request({
    url: '/platform/application/'+id,
    method: 'get'
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/application/updateApplicationList',
    method: 'post',
    data: obj
  })
}