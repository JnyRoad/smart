import request from '@/router/axios'

  export function fetchList (query) {
    return request({
      url: '/platform/articlesrelease/page',
      method: 'get',
      params: query
    })
  }
  export function updateStatus (obj) {
    return request({
      url: '/platform/articlesrelease/status/security/update',
      method: 'post',
      data: obj
    })
  }
