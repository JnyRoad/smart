/**
 * test
 * @author qingqing.he
 * @date 2021-09-06
 */
import http from '@/util/http' // 授权接口
const authorization = true // 密码加密

/**
 * 获取用户全部信息
 */
export const getFullinfo = function () {
  return http.request({
    module: 'app',
    url: `/employee/fullinfo`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
/**
 * 获取用户基本信息
 */
export const getBaseinfo = function () {
  return http.request({
    module: 'app',
    url: `/employee/baseinfo`,
    type: 'get'
  }, {
    authorization: authorization
  })
}

/**
 * 解除绑定
 */
export const unbind = function () {
  return http.request({
    module: 'app',
    url: `/wechat/xc/unbind`,
    type: 'post'
  }, {
    authorization: authorization
  })
}
