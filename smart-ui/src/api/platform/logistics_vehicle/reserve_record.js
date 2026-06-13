import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/logistics/appointment/page',
    method: 'get',
    params: query
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/logistics/appointment/update',
    method: 'post',
    data: obj
  })
}
export function getDetails (id) {
  return request({
    url: '/platform/logistics/appointment/' + id,
    method: 'get',
  })
}
export function manualEnter (id) {
  return request({
    url: '/platform/logistics/appointment/manualEnter/' + id,
    method: 'get'
  })
}
export function goOrder (id) {
  return request({
    url: '/platform/logistics/appointment/goOrder/' + id,
    method: 'get'
  })
}
export function manualLeave (id) {
  return request({
    url: '/platform/logistics/appointment/manualLeave/' + id,
    method: 'get'
  })
}
export function cancelOrder (id) {
  return request({
    url: '/platform/logistics/appointment/cancelOrder/' + id,
    method: 'get'
  })
}
export function goIn (id) {
  return request({
    url: '/platform/logistics/appointment/goIn/' + id,
    method: 'get'
  })
}