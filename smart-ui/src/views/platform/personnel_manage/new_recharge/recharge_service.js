
import request from '@/router/axios'


export function fetchList(query, obj) {
  return request({
    url: '/platform/recharge/page',
    method: 'post',
    data: obj,
    params: query,
    timeout: 3000*60*6
  })
}

export function exportTitle() {
  return request({
    url: '/platform/recharge/excel/title',
    method: 'get'
  })
}

export function toC6(obj) {
  return request({
    url: '/platform/recharge/toC6',
    method: 'post',
    data: obj
  })
}
