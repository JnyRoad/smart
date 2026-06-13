
import request from '@/router/axios'

//获取树形数据
export function getTree (type) {
  return request({
    url: '/platform/device/authority/tree/' + type,
    method: 'get'
  })
}
//闸机管理，查询人员列表
export function getPersonList (obj) {
  return request({
    url: '/platform/device/person/list',
    method: 'post',
    data: obj
  })
}
//道闸管理，查询车牌号
export function getCarList (obj) {
  return request({
    url: '/platform/device/vehicle/list',
    method: 'post',
    data: obj
  })
}
//闸机管理/道闸管理，根据卡片号获取绑定设备列表(人员和车辆都使用这个接口)
export function getLimitByCode (obj) {
  return request({
    url: '/platform/device/task/code',
    method: 'post',
    data: obj
  })
}
//闸机管理/道闸管理，删除权限 (人员和车辆都使用这个接口)
export function delObj (obj) {
  return request({
    url: '/platform/device/task/delete',
    method: 'post',
    data: obj
  })
}