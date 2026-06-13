
import request from '@/router/axios'

/*
  导入供应商
*/
export function supplierImport (data) {
  return request({
    url: '/platform/securityarea/supplier/upload',
    method: 'post',
    data: data
  })
}

/*
  查询所有供应商
*/
export function supplierList (data) {
  return request({
    url: '/platform/securityarea/supplier/list',
    method: 'get',
    params: data
  })
}

/*
  批量设置供应商授权项目
*/
export function supplierBatchAuth (data) {
  return request({
    url: '/platform/securityarea/supplier/batch/author',
    method: 'post',
    data: data
  })
}

/*
  保存保密区通知设置
*/
export function saveNotifyConfig (data) {
  return request({
    url: '/platform/securityarea/supplier/notify/config',
    method: 'post',
    data: data
  })
}

/*
  根据园区id查询保密区通知设置
*/
export function getNotifyConfigByParkId (parkId) {
  return request({
    url: `/platform/securityarea/supplier/notify/config/${parkId}`,
    method: 'get'
  })
}

/*
  批量删除供应商人员
*/
export function delPersonBatch (data) {
  return request({
    url: '/platform/securityarea/supplier/person/del/batch',
    method: 'post',
    data: data
  })
}

/*
  导出供应商
*/
export function exportExcel (data) {
  return request({
    url: '/platform/securityarea/supplier/excel',
    method: 'get',
    params: data,
    responseType: 'arraybuffer'
  })
}
