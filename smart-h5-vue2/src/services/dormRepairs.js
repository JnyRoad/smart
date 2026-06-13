/**
 * 宿舍报修
 */
import http from '@/util/http' // 授权接口
const authorization = true // 密码加密

export const dormRepairRange = function () {
  return http.request({
    module: 'app',
    url: `/dormitory/repair/enum/range`,
    type: 'get'
  }, {
    authorization: authorization
  })
}

export const saveRepairRange = function (data) {
  return http.request({
    module: 'platform',
    url: `/dormitory/repair/add`,
    type: 'post',
    data: data
  }, {
    authorization: authorization
  })
}

export const getRepairList = function (query) {
  return http.request({
    module: 'platform',
    url: `/dormitory/repair/query/record`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}

export const getDetail = function (id) {
  return http.request({
    module: 'platform',
    url: `/dormitory/repair/query/detail/${id}`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
