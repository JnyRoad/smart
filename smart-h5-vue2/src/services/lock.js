import http from '@/util/http' // 授权接口
const authorization = true // 密码加密

/**
 * 获取门锁动态码
 */
export const getPwd = function (query) {
  return http.request({
    module: 'platform',
    url: `/dormitory/staff/get/pwd`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}

/**
 * 修改门锁动态码
 */
export const editPwd = function (query) {
  return http.request({
    module: 'platform',
    url: `/dormitory/staff/update/lock/pwd`,
    type: 'post',
    data: query
  }, {
    authorization: authorization
  })
}
