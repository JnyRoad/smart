import request from '@/router/axios'

function cleanParams(params) {
  const cleaned = {}
  Object.keys(params || {}).forEach(key => {
    const value = params[key]
    if (value === '' || value === null || typeof value === 'undefined') {
      return
    }
    if (Array.isArray(value) && value.length === 0) {
      return
    }
    cleaned[key] = value
  })
  return cleaned
}

export function fetchList(query) {
  return request({
    url: '/platform/isc/access/cleanup/page',
    method: 'get',
    params: cleanParams(query)
  })
}

export function fetchSummary(query) {
  return request({
    url: '/platform/isc/access/cleanup/summary',
    method: 'get',
    params: cleanParams(query)
  })
}

export function executeCleanup(data) {
  return request({
    url: '/platform/isc/access/cleanup/execute',
    method: 'post',
    data: cleanParams(data)
  })
}
