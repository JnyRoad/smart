
import request from '@/router/axios'

export function concentratorList (query) {
  return request({
    url: '/platform/ele/meter/concentrator/page',
    method: 'get',
    params: query
  })
}

export function addConcentrator (query) {
  return request({
    url: '/platform/ele/meter/concentrator/save',
    method: 'post',
    data: query
  })
}

export function updateConcentrator (query) {
  return request({
    url: '/platform/ele/meter/concentrator/update',
    method: 'post',
    data: query
  })
}

export function delConcentrator (id) {
  return request({
    url: `/platform/ele/meter/concentrator/del/${id}`,
    method: 'post'
  })
}

export function downloadFile (id) {
  return request({
    url: `/platform/ele/meter/concentrator/issue/${id}`,
    method: 'post'
  })
}
