import request from '@/router/axios'

export function fetchBatchList(query) {
  return request({
    url: '/platform/isc/card/import/batch/page',
    method: 'get',
    params: query
  })
}

export function fetchDetailList(query) {
  return request({
    url: '/platform/isc/card/import/detail/page',
    method: 'get',
    params: query
  })
}
