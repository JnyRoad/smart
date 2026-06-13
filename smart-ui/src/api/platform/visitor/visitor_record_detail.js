
import request from '@/router/axios'

export function getDetails (id) {
  return request({
    url: '/platform/visitor/searchVisitorDetail/' + id,
    method: 'get',
    params: id
  })
}
export function addBlackObj (obj) {
  return request({
    url: '/platform/black/visitor/add',
    method: 'post',
    data: obj
  })
}