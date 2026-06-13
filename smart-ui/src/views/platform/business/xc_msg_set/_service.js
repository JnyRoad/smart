import request from '@/router/axios'
export function saveData(data) {
  return request({
    url: `/platform/message/temp/save`,
    method: 'post',
    data: data
  })
}
export function getList() {
  return request({
    url: `/platform/message/temp/list`,
    method: 'get'
  })
}