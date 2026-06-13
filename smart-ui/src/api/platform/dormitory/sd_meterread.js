
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/meterread/page',
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

export function generateSDStatementDetail (data) {
  return request({
    url: '/platform/dormitory/meterread/generateStatement',
    method: 'post',
    timeout: 1000*60*5,
    params: data
  })
}

/**
 * 获取上月的抄表数据
 */
export function getPerMonthDetail (query) {
  return request({
    url: '/platform/dormitory/meterreaddetail/premonth/query',
    method: 'get',
    params:query
  })
}
