import request from '@/router/axios'

export function fetchList(param) {
  return request({
    url: '/platform/isc/park/config/page',
    method: 'get',
    params: param
  })
}

export function editObj(obj) {
  return request({
    url: '/platform/isc/park/config/edit',
    method: 'post',
    data: obj
  })
}

export function delObj(id) {
  return request({
    url: '/platform/isc/park/config/' + id,
    method: 'post'
  })
}

export function startCardImportDryRun(obj) {
  return request({
    url: '/platform/isc/card/import/dry-run',
    method: 'post',
    data: obj
  })
}

export function startCardImport(obj) {
  return request({
    url: '/platform/isc/card/import/import',
    method: 'post',
    data: obj
  })
}
