
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/room/page',
    method: 'get',
    params: query
  })
}
export function fetchRoomList (query) {
  return request({
    url: '/platform/dormitory/room/list',
    method: 'get',
    params: query
  })
}
export function allList () {
  return request({
    url: '/platform/park/allList',
    method: 'post'
  })
}
export function floorList () {
  return request({
    url: '/platform/park/dormTreeNonRoom',
    method: 'get'
  })
}
export function putObj (query) {
  return request({
    url: '/platform/dormitory/room/updateRoom',
    method: 'post',
    data:query
  })
}
// export function putBatchObj (query) {
//   return request({
//     url: '/platform/dormitory/room/batchUpdateAttr',
//     method: 'post',
//     data:query
//   })
// }

export function putBatchObj (query) {
  return request({
    url: '/platform/dormitory/room/batchUpdateAttrByIds',
    method: 'post',
    data:query
  })
}
export function putSDBatchObj (query) {
  return request({
    url: '/platform/dormitory/room/batchUpdateSDTemp',
    method: 'post',
    data:query
  })
}

export function delObj (id) {
  return request({
    url: '/platform/dormitory/room/'+id,
    method: 'post'
  })
}
export function countList (query) {
  return request({
    url: '/platform/dormitory/room/count/list',
    method: 'get',
    params: query
  })
}
export function countBuilding (query) {
  return request({
    url: '/platform/dormitory/room/count/building',
    method: 'get',
    params: query
  })
}
export function countFloor (query) {
  return request({
    url: '/platform/dormitory/room/count/floor',
    method: 'get',
    params: query
  })
}
export function getFloor (query) {
  return request({
    url: '/platform/dormitory/floor/queryFloor',
    method: 'post',
    data: query
  })
}
//获取跟园区相关的宿舍分类
export function dormTypeApi (query) {
  return request({
    url: '/platform/dormitory/type/by/park',
    method: 'get',
    params: query
  })
}
//获取所有宿舍分类
export function allDormitoryType () {
  return request({
    url: '/platform/dormitory/type/all',
    method: 'get'
  })
}
//根据职层获取
export function getBedNum (id) {
  return request({
    url: '/platform/dormitory/type/'+id,
    method: 'get'
  })
}
//获取跟园区相关的水电模板
export function fetchSDTempList (parkId) {
  return request({
    url: '/platform/dormitory/sd/parkId/' + parkId,
    method: 'get'
  })
}
export function roomVisual (query) {
  return request({
    url: '/platform/dormitory/room/roomVisual',
    method: 'post',
    data: query
  })
}