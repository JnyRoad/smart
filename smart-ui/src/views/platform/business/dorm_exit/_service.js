
import request from '@/router/axios'


export function fetchList(param) {
  return request({
    url: `/platform/approval/list/page`,
    method: 'get',
    params: param
  })
}

//员工详情
export function getStaffDetail (badge) {
  return request({
    url: '/platform/vehicle/staff/detail/' + badge,
    method: 'get'
  })
}

//添加 园区
export function addObj (obj) {
  return request({
    url: '/platform/approval/list',
    method: 'post',
    data: obj
  })
}

//修改 设置
export function editObj (obj, queryData) {
  return request({
    url: `/platform/approval/node/update`,
    method: 'post',
    data: obj,
    params: queryData
  })
}

//修改 回显
export function getObj (id) {
  return request({
    url: `/platform/approval/node/list/${id}`,
    method: 'get'
  })
}

//短信模板
export function getMsg () {
  return request({
    url: `/platform/message/template/all`,
    method: 'get'
  })
}

//物品类型
export function getGoodType () {
  return request({
    url: `/platform/smtapprovalcondition/type/list`,
    method: 'get'
  })
}

//触发条件
export function triggerCondition () {
  return request({
    url: `/platform/smtapprovalcondition/type/list`,
    method: 'get'
  })
}

//维修范围
export function getRepairRange () {
  return request({
    url: `/platform/dormitory/repair/enum/range`,
    method: 'get'
  })
}

//维修类型
export function getRepairsType () {
  return request({
    url: `/platform/dormitory/repair/enum/repairs/type`,
    method: 'get'
  })
}
