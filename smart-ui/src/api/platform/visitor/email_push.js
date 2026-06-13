
import request from '@/router/axios'
//查询邮件推送设置信息
export function fetchList (query) {
  return request({
    url: '/platform/visitor/push/email/searchAll',
    method: 'get',
    params: query
  })
}
//添加 邮件推送设置信息
export function addObj (obj) {
  return request({
    url: '/platform/visitor/push/email/add',
    method: 'post',
    data: obj
  })
}
//修改 邮件推送设置信息
export function editObj (obj) {
  return request({
    url: '/platform/visitor/push/email/update',
    method: 'post',
    data: obj
  })
}