
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/out/src/apply/page',
    method: 'get',
    params: query
  })
}

/**
 * 导入excel申请
 */
export function postImport(data) {
  const sendData = data.map(item => {
    return {
      "badge": item.jobNumber,
      "certno": item.identity,
      "depName": item.department,
      "jcheName": item.rank,
      "jobName": item.post,
      "name": item.name,
      "phone": item.phone,
      "entryTime": item.entryTime,
      "dispatch": item.dispatch
    }
  })
  return request({
    url: '/platform/out/src/apply/excel/import',
    method: 'POST',
    data: sendData
  })
}

export function checkDetail(applyId) {
  return request({
    url: `/platform/out/src/apply/detail/${applyId}`,
    method: 'POST',
  })
}

export function checkPage(query, applyId) {
  return request({
    url: `/platform/out/src/apply/detail/page/${applyId}`,
    method: 'get',
    params: query
  })
}

export function passOrRefuse(data) {
  return request({
    url: `/platform/out/src/apply/passOrRefuse`,
    method: 'POST',
    data: data
  })
}
