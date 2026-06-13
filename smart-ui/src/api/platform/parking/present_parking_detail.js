
import request from '@/router/axios'

export function fetchDetail (id) {
  return request({
    url: '/platform/snap/vehicle/' + id,
    method: 'get'
  })
}