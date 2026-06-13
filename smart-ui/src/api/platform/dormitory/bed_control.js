
import request from '@/router/axios'

export function fetchType (query) {
  return request({
    url: '/platform/dormitory/room/count/jche',
    method: 'post',
    data: query
  })
}
export function fetchFloor (query) {
  return request({
    url: '/platform/dormitory/room/count/floor',
    method: 'post',
    data: query
  })
}
export function fetchFree (query) {
  return request({
    url: '/platform/dormitory/room/count/free',
    method: 'post',
    data: query
  })
}
export function fetchSex (query) {
  return request({
    url: '/platform/dormitory/room/count/sex',
    method: 'post',
    data: query
  })
}
