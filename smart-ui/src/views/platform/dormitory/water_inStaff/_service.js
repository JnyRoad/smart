
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/staff/statementdetail/new-page',
    method: 'get',
    params: query
  })
}
// 这两个导出接口是全量联表查询，实测稳定要 45-50 秒，超过 axios.js 里 30 秒的全局超时，
// 按 axios.js 里的约定单独放宽超时（而不是改全局默认值）。2 分钟留了余量：网关/nginx
// 侧允许到 600 秒，不会被卡在别处。
const EXPORT_TIMEOUT_MS = 120000

export function exportData (query) {
  return request({
    url: `/platform/dormitory/staff/statementdetail/by-dor`,
    method: 'get',
    params: query,
    timeout: EXPORT_TIMEOUT_MS
  })
}
export function exportShareData (query) {
  return request({
    url: `/platform/dormitory/staff/statementdetail/export/detail`,
    method: 'get',
    params: query,
    timeout: EXPORT_TIMEOUT_MS
  })
}