
import request from '@/router/axios'

export function waterRecordList (query) {
  return request({
    url: '/platform/water/meter/change/page',
    method: 'get',
    params: query
  })
}

export function electricRecordList (query) {
  return request({
    url: '/platform/ele/meter/change/page',
    method: 'get',
    params: query
  })
}
