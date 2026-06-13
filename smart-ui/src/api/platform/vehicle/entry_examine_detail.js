
import request from '@/router/axios'

export function fetchDetail (id) {
  return request({
    url: '/platform/vehicle/apply/' + id,
    method: 'get'
  })
}
export function plate (obj) {
  return request({
    url: '/platform/vehicle/plate',
    method: 'post',
    data: obj
  })
}
