import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/recruitment/page',
    method: 'get',
    params: query
  })
}
export function addObj (obj) {
  return request({
    url: '/platform/recruitment',
    method: 'post',
    data: obj
  })
}
export function getById (id) {
  return request({
    url: '/platform/recruitment/'+id,
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
export function delObj (id) {
  return request({
    url: '/platform/recruitment/' + id,
    method: 'post'
  })
}
export function refreshJob (obj) {
  return request({
    url: '/platform/recruitment/updateIsUp',
    method: 'get',
    params: obj
  })
}