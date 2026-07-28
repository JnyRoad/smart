
import request from '@/router/axios'

/**
 * 查询当前管理员园区范围内的员工最小详情。
 */
export function getAdminStaffDetail (staffId) {
  return request({
    url: '/platform/staff/admin/' + staffId,
    method: 'get'
  })
}

export function fetchIscStaffCards (staffId) {
  return request({
    url: '/platform/isc/staff/card/list',
    method: 'get',
    params: { staffId }
  })
}

export function saveIscStaffCard (data) {
  return request({
    url: '/platform/isc/staff/card/edit',
    method: 'post',
    data
  })
}

export function deleteIscStaffCard (id) {
  return request({
    url: '/platform/isc/staff/card/' + id,
    method: 'post'
  })
}

export function fetchIscParkConfigs (params) {
  return request({
    url: '/platform/isc/park/config/page',
    method: 'get',
    params
  })
}
