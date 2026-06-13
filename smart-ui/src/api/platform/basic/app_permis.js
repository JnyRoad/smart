
import request from '@/router/axios'

export function fetchList (query) { //获取权限列表
  return request({
    url: '/platform/appauth/page',
    method: 'get',
    params: query
  })
}
export function delById (id) { //删除权限
  return request({
    url: '/platform/appauth/delete/'+id,
    method: 'post'
  })
}
export function getById (id) { //获取权限详情
  return request({
    url: '/platform/appauth/'+id,
    method: 'get'
  })
}
export function fetchModule () { //获取APP业务模块
  return request({
    url: '/platform/appauth/module/business/simple',
    method: 'get'
  })
}
export function getHrAuth () { //获取HR权限列表
  return request({
    url: '/platform/appauth/hr/auth/list',
    method: 'get'
  })
}
export function putObj (obj) { //修改权限
  return request({
    url: '/platform/appauth/update ',
    method: 'post',
    data: obj
  })
}
export function addObj (obj) { //新增权限
  return request({
    url: '/platform/appauth/save',
    method: 'post',
    data: obj
  })
}

export function getInitFlag (parkId) { //获取权限详情
  return request({
    url: `/platform/appauth/init/flag/${parkId}`,
    method: 'get'
  })
}

export function getList () { //获取HR权限列表
  return request({
    url: '/platform/park/all',
    method: 'get'
  })
}
