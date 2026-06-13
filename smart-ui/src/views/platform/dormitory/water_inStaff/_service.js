
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/staff/statementdetail/new-page',
    method: 'get',
    params: query
  })
}
export function exportData (query) {
  return request({
    url: `/platform/dormitory/staff/statementdetail/by-dor`,
    method: 'get',
    params: query
  })
}
export function exportShareData (query) {
  return request({
    url: `/platform/dormitory/staff/statementdetail/export/detail`,
    method: 'get',
    params: query
  })
}