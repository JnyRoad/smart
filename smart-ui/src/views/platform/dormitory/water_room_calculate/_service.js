
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: `/platform/dormitory/meterread/byFloor/new`,
    method: 'get',
    params: query
  })
}
export function exportData (query) {
  return request({
    url: `/platform/dormitory/meterread/byDormitory`,
    method: 'get',
    params: query
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

//批量保存修改
export function resetSdDetail (obj) {
  return request({
    url: `/platform/dormitory/meterreaddetail/reset-sd-detail`,
    method: 'post',
    data: obj,
    timeout: 1000*60*5
  })
}

//下载模板
export function getTemplate (data) {
  return request({
    url: `/platform/dormitory/meterread/get-template`,
    method: 'get',
    params: data,
    responseType: 'arraybuffer',
  })
}

//导入水电数据
export function importMeterData (data) {
  return request({
    url: `/platform/dormitory/meterread/import`,
    method: 'post',
    data: data,
    headers: { 'content-type': 'multipart/form-data' },
    responseType: 'arraybuffer',
    timeout: 1000*60*5
  })
}
