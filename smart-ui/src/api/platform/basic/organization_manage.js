/**
 * 组织管理服务接口
 * @author yang.chuan <yang.chuan@bjtce.com>
 * @date 2020-10-09
 */

import request from '@/router/axios'

/**
 * 获取园区列表
 */
export function getParkAll() {
  return request({
    url: '/platform/park/app/all',
    method: 'get'
  })
}

/**
 * 获取园区列表
 */
export function getUserParkAll() {
  return request({
    url: '/platform/park/all',
    method: 'get'
  })
}

/**
 * 获取组织列表
 * @param {*} query
 * @param {*} obj
 */
export function getDataList(query, obj) {
  const q = Object.assign({}, query, obj)
  return request({
    url: '/platform/org/relation/page',
    method: 'get',
    params: q
  })
}

/**
 * 移除
 * @param {*} data
 */
export function delOrg({ id }) {
  return request({
    url: `/platform/org/relation/${id}`,
    method: 'post'
  })
}

/**
 * 保存组织信息
 * @param {*} data
 */
export function postSaveOrg({
  compName,
  id,
  parkId,
  password,
  source,
  userId,
  userName,
  compType
}) {
  if (id) {
    return request({
      url: `/platform/org/relation/update`,
      method: 'POST',
      data: {
        compName,
        id,
        parkId,
        password,
        source,
        userId,
        userName,
        compType
      }
    })
  }
  return request({
    url: `/platform/org/relation/save`,
    method: 'POST',
    data: {
      compName,
      parkId,
      password,
      source,
      userId,
      userName,
      compType
    }
  })
}

/**
 * 保存组织信息--裕同许昌
 * @param {*} data
 */
export function postSaveOrgXuCh({
                              compName,
                              id,
                              parkId,
                              password,
                              source,
                              userId,
                              userName,
                              compType,
                              deviceAuthId
                            }) {
  if (id) {
    return request({
      url: `/platform/org/relation/update`,
      method: 'POST',
      data: {
        compName,
        id,
        parkId,
        password,
        source,
        userId,
        userName,
        compType,
        deviceAuthId
      }
    })
  }
  return request({
    url: `/platform/org/relation/save`,
    method: 'POST',
    data: {
      compName,
      parkId,
      password,
      source,
      userId,
      userName,
      compType,
      deviceAuthId
    }
  })
}


/**
 * 详情
 * @param {*} data
 */
export function getDetails({ id }) {
  return request({
    url: `/platform/org/relation/${id}`,
    method: 'GET'
  })
}
