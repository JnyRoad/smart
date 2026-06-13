/**
 * 我的宿舍
 */
import http from '@/util/http' // 授权接口
const authorization = true // 密码加密

export const getWaterElecData = function (query) {
  return http.request({
    module: 'platform',
    url: `/dormitory/staff/statementdetail/record`,
    type: 'get',
    data: query
  }, {
    authorization: authorization
  })
}

export const getStaffFace = function (data) {
  return http.request({
    module: 'platform',
    url: `/dormitory/staff/face/compare`,
    type: 'post',
    data: data
  }, {
    authorization: authorization
  })
}

export const updatePwd = function (data) {
  return http.request({
    module: 'platform',
    url: `/dormitory/staff/update/pwd`,
    type: 'post',
    data: data
  }, {
    authorization: authorization
  })
}
