
import request from '@/router/axios'

export function getById (id) {
  return request({
    url: '/platform/staff/'+id,
    method: 'get',
    data: id
  })
}

export function editPhone (data) {
  return request({
    url: '/platform/staff/updateStaffPhone',
    method: 'post',
    params: data
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
