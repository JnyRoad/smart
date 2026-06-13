
import request from '@/router/axios'

/*
  分页记录
*/
export function fetchList (query) {
  return request({
    url: '/platform/articlesrelease/office/page',
    method: 'get',
    params: query
  })
}

export function getDetail (id) {
  return request({
    url: `/platform/articlesrelease/detail/${id}`,
    method: 'get'
  })
}

// url: `/platform/articlesrelease/detail/${id}`,
// getDetail
