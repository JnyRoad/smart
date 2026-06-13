
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/room/search/room/statistics',
    method: 'post',
    data: query
  })
}
export function fetchList1 (query) {
  return request({
    url: '/platform/dormitory/room/search/room/statistics',
    method: 'get',
    params: query
  })
}
export function exportApi (query) {
  return request({
    url: '/platform/dormitory/room/search/room/statistics/excel',
    method: 'post',
    data: query,
    timeout: 1000*60*5,
    responseType: 'arraybuffer'
  })
}