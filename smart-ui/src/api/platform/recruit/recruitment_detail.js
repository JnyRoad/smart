import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/admin/log/page',
    method: 'get',
    params: query
  })
}
export function getById (id) {
  return request({
    url: '/platform/recruitment/getInfo/'+id,
    method: 'get',
    data: id
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/recruitment/updateRecruitment',
    method: 'post',
    data: obj
  })
}