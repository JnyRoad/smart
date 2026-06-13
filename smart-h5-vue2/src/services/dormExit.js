/**
 * 退宿申请
 */
import http from '@/util/http' // 授权接口
const authorization = true // 密码加密

export const saveDormExit = function (query) {
  return http.request({
    module: 'platform',
    url: `/dor/quit/apply`,
    type: 'POST',
    data: query
  }, {
    authorization: authorization
  })
}

export const getExitList = function (query) {
  return http.request({
    module: 'platform',
    url: `/dor/quit/page`,
    type: 'get',
    data: query
  }, {
    authorization: authorization
  })
}

export const getDetail = function (id) {
  return http.request({
    module: 'platform',
    url: `/dor/quit/detail/${id}`,
    type: 'get'
  }, {
    authorization: authorization
  })
}

export const getScanDetail = function (id) {
  return http.request({
    module: 'platform',
    url: `/dor/quit/list/check/${id}`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
