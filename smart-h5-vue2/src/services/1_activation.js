/**
 * test
 * @author yang.chuan
 * @date 2020-11-25
 */
import http from '@/util/http' // 授权接口
import encryption from '@/util/encryption'
const authorization = false // 密码加密

/**
 * 手机号发验证码
 */
export const getCodesApi = function (data) {
  return http.request({
    module: 'admin',
    url: `/mobile/${data.mobile}`,
    type: 'GET'
  }, {
    authorization: authorization
  })
}
/**
 * 激活页面校验
 */
export const validUrlApi = function (data) {
  return http.request({
    module: 'admin',
    url: `/user/password/validUrl`,
    type: 'GET',
    data: data
  }, {
    authorization: authorization
  })
}
/**
 * 验证码校验
 */
export const validApi = function (data) {
  return http.request({
    module: 'admin',
    url: `/mobile/captcha/valid`,
    type: 'POST',
    data: data
  }, {
    authorization: authorization
  })
}
/**
 * 设置密码
 */
export const resetPwdApi = function (data) {
  const user = encryption({
    data: data,
    param: ['password']
  })
  const user2 = encryption({
    data: user,
    param: ['confirmPassword']
  })
  return http.request({
    module: 'admin',
    url: `/user/password/reset`,
    type: 'POST',
    data: user2
  }, {
    authorization: authorization
  })
}
