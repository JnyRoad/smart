
import request from '@/router/axios'

export function concentratorList (query) {
  return request({
    url: '/platform/ele/meter/concentrator/page',
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
    url: '/platform/ele/meter/save',
    method: 'post',
    data: data
  })
}

export function putObj (data) {
  return request({
    url: '/platform/ele/meter/update',
    method: 'post',
    data: data
  })
}

export function changeObj (data) {
  return request({
    url: '/platform/ele/meter/device/change',
    method: 'post',
    data: data
  })
}

export function delObj (meterId) {
  return request({
    url: `/platform/ele/meter/del/${meterId}`,
    method: 'post'
  })
}

export function batchDelete (data) {
  return request({
    url: `/platform/ele/meter/del`,
    method: 'post',
    data: data
  })
}
// batchDelete

export function fetchList (query) {
  return request({
    url: '/platform/ele/meter/page',
    method: 'get',
    params: query
  })
}

export function recordList (query) {
  return request({
    url: '/platform/ele/meter/history/page',
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

export function setTagApi (data) {
  return request({
    url: '/platform/ele/meter/tag/edit',
    method: 'post',
    data: data
  })
}


export function putValve (data) { // 电表闸门
  return request({
    url: '/platform/ele/meter/brake/change',
    method: 'get',
    params: data
  })
}

export function putValves (data) { // 批量电表闸门
  return request({
    url: '/platform/ele/meter/brake/batch/change',
    method: 'POST',
    data: data
  })
}

export function meterRead (data) { // 批量抄表
  return request({
    url: '/platform/ele/meter/reading',
    method: 'POST',
    data: data
  })
}

export function reDownload(data) { // 批量下载档案
  return request({
    url: '/platform/ele/meter/re/download',
    method: 'POST',
    data: data
  })
}

// putValve

/*
  导入
*/
export function supplierImport (data) {
  return request({
    url: '/platform/ele/meter/excel/import',
    method: 'post',
    data: data,
    headers: {
      'content-type': 'multipart/form-data'
    },
    responseType: "arraybuffer",
    timeout: 1000*60*5
  })
}

/**
 * 获取导入总条数，以及剩余条数
 */
export function flushProgress() {
  return request({
    url: '/platform/ele/meter/flushProgress',
    method: 'get'
  })
}
