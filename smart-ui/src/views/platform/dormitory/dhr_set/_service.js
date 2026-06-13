
import request from '@/router/axios'

/*
  分页记录
*/
export function fetchList (query, parkId) {
  return request({
    url: `/platform/settlement/template/page/${parkId}`,
    method: 'get',
    params: query
  })
}

/*
  删除
*/
export function deleteData (id) {
  return request({
    url: `/platform/settlement/template/${id}`,
    method: 'post'
  })
}

/**
   * 查询用户所有园区
   */
export function allPark (query) {
  return request({
    url: '/platform/park/all',
    method: 'get'
  })
}


/*
  添加、编辑
*/
export function addData (data) {
  return request({
    url: `/platform/settlement/template/edit/template`,
    method: 'post',
    data: data
  })
}

/*
  适用bu/宿舍(批量修改)
*/
export function rangeEdit (data) {
  return request({
    url: `/platform/settlement/template/edit/range`,
    method: 'post',
    data: data
  })
}

/*
  结算规则设置
*/
export function ruleSet (data) {
  return request({
    url: `/platform/common/config/edit/config`,
    method: 'post',
    data: data
  })
}

/*
  获取结算规则设置
*/
export function getRule (data) {
  return request({
    url: `/platform/common/config/leave/settlement`,
    method: 'get',
    params: data
  })
}

/*
  添加、编辑
*/
export function addDeductionData (data) {
  return request({
    url: `/platform/settlement/template/save/item`,
    method: 'post',
    data: data
  })
}

/*
  获取结算规则设置
*/
export function getJche () {
  return request({
    url: `/platform/recruitment/getJche`,
    method: 'get'
  })
}




export function rangeData (id, type) {
  return request({
    url: `/platform/settlement/template/range/${id}/${type}`,
    method: 'get'
  })
}

export function allList (params) {
  return request({
    url: '/platform/settlement/template/range/room/tree',
    method: 'get',
    params: params
  })
}

export function templateItem (id) {
  return request({
    url: `/platform/settlement/template/item/${id}`,
    method: 'get'
  })
}

export function templateItemDel (id) {
  return request({
    url: `/platform/settlement/template/remove/item/${id}`,
    method: 'post'
  })
}
