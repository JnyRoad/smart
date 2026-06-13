
import request from '@/router/axios'

//公摊水电表按类型分页记录
export function categroyList(query) {
  return request({
    url: '/platform/dormitory/commonsd/meterread/catePage',
    method: 'get',
    params: query
  })
}
export function updateStayInfo(obj,mrId) {
  return request({
    url: `/platform/dormitory/staff/statementdetail/updateStayInfo/${mrId}`,
    method: 'post',
    data: obj
  })
}
export function queryStayInfo(query) {
  return request({
    url: '/platform/dormitory/staff/statementdetail/queryStayInfo',
    method: 'get',
    params: query
  })
}
export function queryStayModify(mrId) {
  return request({
    url: `/platform/dormitory/staff/statementdetail/queryStayModify/${mrId}`,
    method: 'get'
  })
}
