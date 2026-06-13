
import request from '@/router/axios'

export function valveList (query) {
  return request({
    url: '/platform/water/meter/valve/page',
    method: 'get',
    params: query
  })
}

export function addValve (query) {
  return request({
    url: '/platform/water/meter/valve/save',
    method: 'post',
    data: query
  })
}

export function updateValve (query) {
  return request({
    url: '/platform/water/meter/valve/edit',
    method: 'post',
    data: query
  })
}

export function concentratorList (query) {
  return request({
    url: '/platform/water/meter/valve/concentrator/page',
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

export function putValve (query) {
  return request({
    url: '/platform/water/meter/valve/out/status',
    method: 'get',
    params: query
  })
}

export function changeRemoteStatus (query) {
  return request({
    url: '/platform/water/meter/valve/out/remote-status',
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
