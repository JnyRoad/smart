/**
 * test
 * @author qingqing.he
 * @date 2021-09-06
 */
import http from '@/util/http' // 授权接口
const authorization = true // 密码加密

/**
 * 获取服务模块
 */
export const getServiceModule = function () {
  return http.request({
    module: 'app',
    url: `/service/module/list`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
/**
 * 获取公告列表
 */
export const getBbsList = function (query) {
  return http.request({
    module: 'app',
    url: `/home/bbs/list`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}
/**
 * 获取公告详情
 */
export const getBbsDetail = function (id) {
  return http.request({
    module: 'app',
    url: `/home/bbs/detail/${id}`,
    type: 'get'
  }, {
    authorization: authorization
  })
}

/**
 * 获取公告详情
 */
export const getWeather = function (query) {
  return http.request({
    module: 'app',
    url: `/common/weather`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}
