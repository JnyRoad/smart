
import request from '@/router/axios'
export const returnApi = {
  getList (data) {
    return request({
      url: '/platform/articlesrelease/back/page',
      method: 'get',
      params: data
    })
  },
  getDetail(id){
    return request({
      url: `/platform/articlesrelease/detail/${id}`,
      method: 'get'
    })
  },
  confirm(data){
    return request({
      url: `/platform/articlesrelease/back/confirm/${data.releaseId}`,
      method: 'post',
      data: data
    })
  }
}

// export function getList (query) {
//   return request({
//     url: '/articlesrelease/back/page',
//     method: 'get',
//     params: query
//   })
// }
