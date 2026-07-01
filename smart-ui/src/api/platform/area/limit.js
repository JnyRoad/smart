
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/device/authority/page',
    method: 'get',
    params: query
  })
}
export function getTree (type,parkId) {
  return request({
    url: '/platform/device/authority/tree/' + type+'/'+parkId,
    method: 'get'
  })
}
export function getTreePerson (parkId) {
  return request({
    url: `/platform/device/authority/person/tree/${parkId}`,
    method: 'get'
  })
}
export function getTreePersonNew (parkId, areaType) {
  return request({
    url: `/platform/device/authority/person/tree/${parkId}/${areaType}`,
    method: 'get'
  })
}
export function getDetailPage (query) {
  return request({
    url: '/platform/device/authority/detail/page',
    method: 'post',
    params: {
      current: query.current,
      size: query.size
    },
    data: query
  })
}
export function batchDel (obj) {
  return request({
    url: '/platform/device/authority/relation/del',
    method: 'post',
    data: obj,
    timeout: 1000*60
  })
}
export function clearAll (id) {
  return request({
    url: '/platform/device/authority/relation/clear/' + id,
    method: 'post',
    timeout: 1000*60
  })
}
export function addObj (obj) {
  return request({
    url: '/platform/device/authority/save',
    method: 'post',
    data: obj,
    timeout: 1000*60*5
  })
}
export function getObj (id) {
  return request({
    url: '/platform/device/authority/' + id,
    method: 'get'
  })
}
export function putObj (obj) {
  return request({
    url: '/platform/device/authority/update',
    method: 'post',
    data: obj,
    timeout: 1000*60*5
  })
}
export function delObj (id) {
  return request({
    url: '/platform/device/authority/delete/' + id,
    method: 'get'
  })
}

export function batchAdd (data) {
  return request({
    url: '/platform/device/authority/relation/add',
    method: 'post',
    data: data
  })
}

// 变更通关权限性质（公共区域/保密区域），只改 area_type 这一个字段
export function switchAreaType (data) {
  return request({
    url: '/platform/device/authority/areaType/switch',
    method: 'post',
    data: data
  })
}
