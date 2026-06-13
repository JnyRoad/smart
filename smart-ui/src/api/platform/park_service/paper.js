import request from "@/router/axios";

export function fetchList (query) {
  return request({
    url: '/platform/paper/page',
    method: 'get',
    params: query
  })
}
export function getById (id) {
  return request({
    url: '/platform/paper/detail/'+id,
    method: 'get'
  })
}
export function addObj (Obj) {
  return request({
    url: '/platform/paper/add',
    method: 'post',
    data: Obj
  })
}
export function putObj (Obj) {
  return request({
     url: '/platform/paper/update',
    method: 'post',
    data: Obj
  })
}
export function delObj (id) {
  return request({
    url: '/platform/paper/delete/' + id,
    method: 'get'
  })
}
export function getCompsObj (id) {
  return request({
    url: '/platform/paper/getBu/'+id,
    method: 'get'
  })
}
export function statisticsApi (id) {
  return request({
    url: '/platform/paper/record/statistics/' + id,
    method: 'get'
  })
}
export function exportApi (id) {
  return request({
    url: '/platform/paper/record/export/' + id,
    method: 'get',
    timeout: 1000*60*5,
    responseType: 'arraybuffer'
  })
}
