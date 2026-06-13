/**
 * test
 * @author qingqing.he
 * @date 2021-09-06
 */
import http from '@/util/http' // 授权接口
const authorization = false // 密码加密

// export const API_LOGIN = '/auth/oauth/token' // 账号密码登录
// export const API_LOGINBYCODE = '/auth/mobile/token/sms' // 短信验证码登录
// export const API_REFRESH_TOKEN = '/auth/oauth/token' // 刷新token
// export const API_LOGIN_FACE = '/auth/ocr/token/face' // 人脸识别登录
// export const API_LOGIN_OUT = '/auth/token/logout' // 退出登录

/**
 * 账号密码登录
 */
export const loginByPwd = function (data) {
  return http.request({
    url: `/auth/oauth/token`,
    type: 'POST',
    data: data
  }, {
    authorization: authorization
  })
}
/**
 * 微信绑定，根据code获取工号
 */
export const getBadgeByCode = function (query) {
  return http.request({
    module: 'app',
    url: `/wechat/getBadge`,
    type: 'POST',
    params: query
  }, {
    authorization: authorization
  })
}
/**
 * 微信绑定，根据code获取token
 */
export const getTokenByCode = function (query) {
  return http.request({
    module: 'auth',
    url: `/wx/public/token`,
    type: 'POST',
    data: query
  }, {
    authorization: true
  })
}
/**
 * 绑定微信openId与工号关系
 */
export const wechatBadge = function (query) {
  return http.request({
    module: 'app',
    url: `/wechat/xc/banging/badge`,
    type: 'POST',
    data: query
  }, {
    authorization: authorization
  })
}
