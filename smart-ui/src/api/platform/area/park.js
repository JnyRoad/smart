
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/park/page',
    method: 'get',
    params: query
  })
}
export function addObj (obj) {
  return request({
    url: '/platform/park/addPark',
    method: 'post',
    data: obj
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/park/updatePark',
    method: 'post',
    data: obj
  })
}
export function delObj (id) {
  return request({
    url: '/platform/park/delete/' + id,
    method: 'get'
  })
}

export function allPark () {
  return request({
    url: `/platform/park/app/all`,
    method: 'get'
  })
}
