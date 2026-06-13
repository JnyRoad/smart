import request from "@/router/axios";

export function fetchList (query) {
  return request({
    url: '/platform/feed/back/page',
    method: 'get',
    params: query
  })
}
export function getById (id) {
  return request({
    url: '/platform/feed/back/detail/'+id,
    method: 'get'
  })
}
export function putObj (Obj) {
  return request({
    url: '/platform/feed/back/update',
    method: 'post',
    data: Obj
  })
}
