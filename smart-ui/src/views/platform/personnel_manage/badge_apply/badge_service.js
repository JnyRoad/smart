
import request from '@/router/axios'


export function fetchList(query, obj) {
  return request({
    url: '/platform/badge/apply/page',
    method: 'post',
    data: obj,
    params: query
  })
}

export function editObj(obj) {
  return request({
    url: '/platform/badge/apply/update',
    method: 'post',
    data: obj
  })
}

export function getObj(id) {
  return request({
    url: '/platform/badge/apply/detail',
    method: 'get',
    params:id
  })
}

export function exportList(obj) {
  return request({
    url: '/platform/badge/apply/excel',
    method: 'post',
    data: obj
  })
}

export function getOperaStatus() {
  return request({
    url: '/platform/badge/apply/enum/status',
    method: 'get'
  })
}
