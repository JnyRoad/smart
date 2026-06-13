
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/securityarea/supplier/page',
    method: 'get',
    params: query
  })
}

export function addRecord (params) {
  return request({
    url: '/platform/securityarea/supplier/save',
    method: 'post',
    data: params,
    timeout: 1000*60*5
  })
}

export function recordDetail (id) {
  return request({
    url: `/platform/securityarea/supplier/query/${id}`,
    method: 'get'
  })
}

export function delRecord (id) {
  return request({
    url: '/platform/securityarea/supplier/del/'+id,
    method: 'post'
  })
}

/**
 * 获供应商部门树形
 */
export function getDeptTree() {
  return request({
    url: '/platform/ext/dept/tree',
    method: 'get'
  })
}
