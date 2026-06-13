
import request from '@/router/axios'

export function concentratorList (query) {
  return request({
    url: '/platform/water/meter/concentrator/page',
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

export function fetchRoomList (query) {
  return request({
    url: '/platform/dormitory/room/list',
    method: 'get',
    params: query
  })
}

export function getTagList () {
  return request({
    url: '/platform/device/tag/list',
    method: 'get'
  })
}

export function addObj (data) {
  return request({
    url: '/platform/water/meter/save',
    method: 'post',
    data: data
  })
}

export function putObj (data) {
  return request({
    url: '/platform/water/meter/update',
    method: 'post',
    data: data
  })
}

export function changeObj (data) {
  return request({
    url: '/platform/water/meter/device/change',
    method: 'post',
    data: data
  })
}

export function delObj (meterId) {
  return request({
    url: `/platform/water/meter/del//${meterId}`,
    method: 'post'
  })
}

export function batchDelete (data) {
  return request({
    url: `/platform/water/meter/del`,
    method: 'post',
    data: data
  })
}

export function fetchList (query) {
  return request({
    url: '/platform/water/meter/page',
    method: 'get',
    params: query
  })
}

export function recordList (query) {
  return request({
    url: '/platform/water/meter/history/page',
    method: 'get',
    params: query
  })
}

export function logList (query) {
  return request({
    url: '/platform/operate/log/list',
    method: 'get',
    params: query
  })
}

export function putValve (data) { // 水表阀门
  return request({
    url: '/platform/water/meter/valve/in/status',
    method: 'get',
    params: data
  })
}

export function setTagApi (data) {
  return request({
    url: '/platform/water/meter/tag/edit',
    method: 'post',
    data: data
  })
}

export function meterRead (data) { // 批量抄表
  return request({
    url: '/platform/water/meter/reading',
    method: 'POST',
    data: data
  })
}

export function reDownload(data) { // 批量下载档案
  return request({
    url: '/platform/water/meter/re/download',
    method: 'POST',
    data: data
  })
}

/*
  导入
*/
export function supplierImport (data) {
  return request({
    url: '/platform/water/meter/excel/import',
    method: 'post',
    data: data,
    headers: { 'content-type': 'multipart/form-data' },
    responseType: 'arraybuffer',
    timeout: 1000*60*5
  })
}

/**
 * 获取导入总条数，以及剩余条数
 */
export function flushProgress() {
  return request({
    url: '/platform/water/meter/flushProgress',
    method: 'get'
  })
}
