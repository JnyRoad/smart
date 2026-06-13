import request from '@/router/axios'


export function getDetails (id) {
  return request({
    url: '/platform/articlesrelease/detail/' + id,
    method: 'get',
    params: id
  })
}
