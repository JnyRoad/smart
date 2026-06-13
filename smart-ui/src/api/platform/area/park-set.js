import request from '@/router/axios'

//-----START------园区配置-组织关系Api-----------
 /**
  *  查看组织关系
  * @param {*} parkId 园区ID
  */
export function viewOrgInfo (parkId) {
  return request({
    url: '/platform/orgset/view/' + parkId,
    method: 'get'
  })
}
 /**
  * 保存组织关系
  * @param {*} body 消息体
  */
export function saveOrgInfo (body) {
  return request({
    url: '/platform/orgset',
    method: 'post',
    data: body
  })
}
//-----END------园区配置-组织关系Api-----------

//-----START------园区配置-访客通知开关Api-----------
/**
 * 获取指定园区开关
 * @param {*} parkId 园区ID
 */
export function getSwitcheList (parkId) {
  return request({
    url: '/platform/parknoticeswitch/list/switch/' + parkId,
    method: 'get'
  })
}
/**
 * 批量新增或修改开关
 * @param {*} parkId  园区ID
 * @param {*} body 消息体
 */
export function saveSwitch (parkId,body) {
  return request({
    url: '/platform/parknoticeswitch/batchSave/' + parkId,
    method: 'post',
    data: body
  })
}
//-----END------园区配置-访客通知开关Api-----------

//-----START------园区配置-招聘配置Api-----------
/**
 * 获取招聘配置信息
 * @param {*} parkId 园区ID
 */
export function getRecruitSetInfo (parkId) {
  return request({
    url: '/platform/recruitmentsetting/list/' + parkId,
    method: 'get'
  })
}
/**
 * 获取工作地点列表
 */
export function getWorkBaseList (body) {
  return request({
    url: '/platform/recruitmentsetting/list/workbase',
    method: 'post',
    data: body
  })
}
/**
 * 获取可签约Bu列表
 */
export function getCompList (body) {
  return request({
    url: '/platform/recruitmentsetting/list/comp',
    method: 'post',
    data: body
  })
}
/**
 * 获取可签约单位列表
 */
export function getConComanyList (body) {
  return request({
    url: '/platform/recruitmentsetting/list/concomany',
    method: 'post',
    data: body
  })
}
/**
 * 保存聘配置信息
 */
export function saveRecruitSetInfo (body) {
  return request({
    url: '/platform/recruitmentsetting/save',
    method: 'post',
    data: body
  })
}
//-----END------园区配置-招聘配置Api-----------

export function editAuth (obj) {
  return request({
    url: '/platform/business/auth/batch/edit',
    method: 'post',
    data: obj,
    timeout: 1000*60*5
  })
}

export function fetchAuthList (query) {
  return request({
    url: '/platform/device/authority/page',
    method: 'get',
    params: query
  })
}

export function fetchAuthId (query) {
  return request({
    url: '/platform/business/auth/list',
    method: 'get',
    params: query
  })
}

export function buAuthList (parkId) {
  return request({
    url: `/platform/business/auth/bu/dept/tree/${parkId}`,
    method: 'get'
  })
}

export function getAuthList (type, parkId) {
  return request({
    url: `/platform/device/authority/list/${type}/${parkId}`,
    method: 'get'
  })
}

export function saveAuthList (deptId, data) {
  return request({
    url: `/platform/business/auth/dept/save/${deptId}`,
    method: 'post',
    data: data
  })
}

export function getBuList (parkId) {
  return request({
    url: `/platform/security/bu/get/${parkId}`,
    method: 'get'
  })
}

export function saveBuEdit (data) {
  return request({
    url: `/platform/security/bu/edit`,
    method: 'post',
    data: data
  })
}
