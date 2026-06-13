
import request from '@/router/axios'

export function roleList () {
  return request({
    url: '/admin/role/roleList',
    method: 'get'
  })
}
export function fetchList (query) {
  return request({
    url: '/platform/staff/page',
    method: 'post',
    params: {
      current: query.current,
      size: query.size
    },
    data: query
  })
}
export function getStaffByBadge (badge) {
  return request({
    url: '/platform/staff/define/badge',
    method: 'get',
    params: { badge }
  })
}
// 查询APP权限
export function fetchAppList () {
  return request({
    url: '/platform/appauth/list',
    method: 'get'
  })
}
// 修改员工APP权限
export function editAppList (obj) {
  return request({
    url: '/platform/staff/auth/app/update',
    method: 'post',
    data: obj
  })
}
/**
 *
 * 查询所有设备权限策略展示到select
 */
export function deviceAuthList (query) {
  return request({
    url: '/platform/device/authority/page',
    method: 'get',
    params: query
  })
}


/**
 *
 * 修改员工的通关权限策略
 */
export function upDeviceAuthList (obj,type) {
  return request({
    url: `/platform/staff/device/auth/updateAuth/${type}`,
    method: 'post',
    data: obj
  })
}
/**
 *
 * 权限下发进度查询
 */
 export function issueAuth (query) {
  return request({
    url: `/platform/staff/device/auth/authInfo`,
    method: 'get',
    params: query
  })
}

/**
 *
 * 根据员工照片名称（工号）查询当前员工照片信息
 */
export function getStaffImgInfo (obj) {
  return request({
    url: '/platform/staff/check/facePic',
    method: 'post',
    data: obj
  })
}
export function getAuth (id) {
  return request({
     url: '/platform/staff/getAuthInfo/'+id,
      method: 'get'
  })
}
/**
 *
 * 上传员工照片
 */
export function importImgs (obj) {
  return request({
    url: '/platform/staff/upload/facePic',
    method: 'post',
    data: obj
  })
}
