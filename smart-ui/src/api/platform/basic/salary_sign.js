
import request from '@/router/axios'



export function fetchList (query) {
  return request({
    url: '/platform/wage/sign/page',
    method: 'get',
    params: query
  })
}
//详情
export function getById (id) {
  return request({
    url: '/platform/wage/sign/'+id,
    method: 'get',
    data: id
  })
}
