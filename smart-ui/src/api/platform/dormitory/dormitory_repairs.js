
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/repair/page',
    method: 'get',
    params: query
  })
}

export function fetchListYuto (query) {
  return request({
    url: '/platform/dormitory/repair/page/yuto',
    method: 'get',
    params: query
  })
}

export function fetchStatementList (query) {
  return request({
    url: '/platform/dormitory/meterread/statmentpage',
    method: 'get',
    params: query
  })
}

export function queryDormitory (params) {
  return request({
    url: '/platform/dormitory/queryDormitory',
    method: 'post',
    data:params
  })
}

export function queryFloor (query) {
  return request({
    url: '/platform/dormitory/floor/queryFloor',
    method: 'post',
    data: query
  })
}

export function queryRoom (query) {
  return request({
    url: '/platform/dormitory/room/queryRoomList',
    method: 'post',
    data: query
  })
}

export function addRecord (params) {
  return request({
    url: '/platform/dormitory/meterread/add',
    method: 'post',
    data:params
  })
}
export function delTemplate (id) {
  return request({
    url: '/platform/dormitory/sd/'+id,
    method: 'post'
  })
}
export function updateDormitorySDTemplate (params) {
  return request({
    url: '/platform/dormitory/sd/update',
    method: 'post',
    data:params
  })
}
export function getFloorStartNum (dormitoryId) {
  return request({
    url: '/platform/dormitory/floor/getFloorStartNum/'+dormitoryId,
    method: 'get'
  })
}
export function addRoomSdData (params) {
  return request({
    url: '/platform/dormitory/meterreaddetail/add',
    method: 'post',
    data:params
  })
}
export function getRoomMeterReadDetail (mrId) {
  return request({
    url: '/platform/dormitory/meterreaddetail/query/'+mrId,
    method: 'get'
  })
}

export function generateSDStatementDetail () {
  return request({
    url: '/platform/dormitory/meterread/generateStatement',
    method: 'post'
  })
}

export function replyRepair (params) {
  return request({
    url: '/platform/dormitory/repair/reply',
    method: 'post',
    data:params
  })
}

export function checkDetail (id) {
  return request({
    url: '/platform/dormitory/repair/query/detail/'+id,
    method: 'get'
  })
}
