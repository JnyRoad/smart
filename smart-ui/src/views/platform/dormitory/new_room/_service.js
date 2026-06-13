
import request from '@/router/axios'

//房间列表查询
export function fetchList (query) {
  return request({
    url: '/platform/dormitory/room/queryRoomVisual',
    method: 'post',
    data: query
  })
}
//房间入住详情
export function bedDetail (roomId) {
  return request({
    url: `/platform/dormitory/room/bedDetail/${roomId}`,
    method: 'post'
  })
}
//查询水电表记录
export function commonsd (query) {
  return request({
    url: '/platform/dormitory/commonsd/page',
    method: 'get',
    params: query
  })
}
//保存水电表
export function commonsdSave (obj) {
  return request({
    url: '/platform/dormitory/commonsd/save',
    method: 'post',
    data: obj
  })
}
//删除水电表
export function commonsdDel (id) {
  return request({
    url: `/platform/dormitory/commonsd/del/${id}`,
    method: 'post'
  })
}
//保存抄表数据
export function meterreadSave (obj) {
  return request({
    url: '/platform/dormitory/commonsd/meterread/save',
    method: 'post',
    data: obj
  })
}
//公摊抄表→抄表记录
export function meterreadList (query) {
  return request({
    url: '/platform/dormitory/commonsd/meterread/page',
    method: 'get',
    params: query
  })
}
//公摊抄表→新增是，回显所选月份信息
export function meterreadDetail (query) {
  return request({
    url: '/platform/dormitory/commonsd/meterread/query',
    method: 'get',
    params: query
  })
}

//删除抄表数据记录
export function meterreadDel (id) {
  return request({
    url: `/platform/dormitory/commonsd/meterread/del/${id}`,
    method: 'post'
  })
}

//批量抄电
export function meterreadBatch (obj) {
  return request({
    url: `/platform/dormitory/meterread/batch`,
    method: 'post',
    data: obj
  })
}
//批量抄电--房间读表数回显
export function meterreadByFloor (floorId, query) {
  return request({
    url: `/platform/dormitory/meterread/byFloor/${floorId}`,
    method: 'get',
    params: query
  })
}
//水电抄表--房间读表数回显
export function meterreadRoom (roomId, query) {
  return request({
    url: `/platform/dormitory/meterread/${roomId}`,
    method: 'get',
    params: query
  })
}
//水电抄表--标记抄表情况 0 未抄表 1部分抄表 2已抄完
export function meterreadStatus (obj) {
  return request({
    url: `/platform/dormitory/meterread/room/status`,
    method: 'post',
    data: obj
  })
}
//水电抄表保存
export function meterreadAdd (obj) {
  return request({
    url: `/platform/dormitory/meterreaddetail/add`,
    method: 'post',
    data: obj
  })
}
//根据楼栋，查询楼层和房间的集合
export function getFloorRoom (dormitoryId) {
  return request({
    url: `/platform/dormitory/room/frlist/${dormitoryId}`,
    method: 'post'
  })
}
//根据楼层ids查询房间的集合
export function getRoomByFloorIds (floorIds) {
  return request({
    url: `/platform/dormitory/room/frlist/byfloors`,
    method: 'post',
    params: floorIds
  })
}
//添加家属
export function addFamily (data) {
  return request({
    url: `/platform/dormitory/staff/family/add`,
    method: 'post',
    data: data
  })
}
//删除家属
export function delFamily (id) {
  return request({
    url: `/platform/dormitory/staff/family/del/${id}`,
    method: 'get'
  })
}
//查询家属
export function getFamily (staffBadge) {
  return request({
    url: `/platform/dormitory/staff/family/query/${staffBadge}`,
    method: 'get'
  })
}
