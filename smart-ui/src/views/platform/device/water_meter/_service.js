
import request from '@/router/axios'

export function concentratorList (query) {
  return request({
    url: '/platform/water/meter/concentrator/page',
    method: 'get',
    params: query
  })
}

export function addConcentrator (query) {
  return request({
    url: '/platform/water/meter/concentrator/save',
    method: 'post',
    data: query
  })
}

export function updateConcentrator (query) {
  return request({
    url: '/platform/water/meter/concentrator/update',
    method: 'post',
    data: query
  })
}

export function delConcentrator (id) {
  return request({
    url: `/platform/water/meter/concentrator/del/${id}`,
    method: 'post'
  })
}
