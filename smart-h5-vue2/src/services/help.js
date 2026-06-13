/**
 * 我的宿舍
 */
import http from '@/util/http' // 授权接口
const authorization = true // 密码加密

export const getHelpList = function (query) {
  return http.request({
    module: 'app',
    url: `/guide/help/question/list`,
    type: 'get',
    data: query
  }, {
    authorization: authorization
  })
}

export const getDetail = function (id) {
  return http.request({
    module: 'app',
    url: `/guide/help/question/answer/${id}`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
