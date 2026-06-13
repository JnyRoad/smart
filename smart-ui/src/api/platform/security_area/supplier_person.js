
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/securityarea/supplier/person/page',
    method: 'get',
    params: query
  })
}

export function addRecord (params) {
  return request({
    url: '/platform/securityarea/supplier/person/save',
    method: 'post',
    data:params
  })
}

export function addUploadRecord (params) {
  return request({
    url: '/platform/securityarea/supplier/person/uploadSave',
    method: 'post',
    data:params
  })
}

export function delRecord (id) {
  return request({
    url: '/platform/securityarea/supplier/person/'+id,
    method: 'post'
  })
}


export function getSupplierList () {
  return request({
    url: '/platform/securityarea/supplier/list',
    method: 'get'
  })
}

export function getParkSupplierList (query) {
  return request({
    url: '/platform/securityarea/supplier/list',
    method: 'get',
    params: query
  })
}

export function visitorFind (data) {
  return request({
    url: '/platform/securityarea/supplier/person/visitor/find',
    method: 'post',
    data: data
  })
}
