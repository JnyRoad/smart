import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/snap/vehicle/searchVehicleAccess',
    method: 'get',
    params: query
  })
}
// 地点
export function tree () {
  return request({
    url: '/platform/device/area/tree',
    method: 'get'
  })
}