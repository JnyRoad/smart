/**
 * 其他接口
 *
 */
import http from '@/util/http'

/**
 * 通过手机号发送短信验证码
 * @param {String} mobile - 手机号
 */
export const getPhoneMessage = function (mobile) {
  return http.request({
    module: 'app',
    url: `/sms/send/getCode/${mobile}`,
    type: 'GET'
  }, {
    authorization: false
  })
}
/**
 * 校验短信验证码
 * @param {Object} query
 * @param {String} query.mobile - 手机号
 * @param {String} query.smsCode - 验证码
 */
export const verifyPhoneMessage = function (query) {
  return http.request({
    module: 'app',
    url: `/sms/verify`,
    type: 'GET',
    params: query
  }, {
    authorization: false
  })
}
