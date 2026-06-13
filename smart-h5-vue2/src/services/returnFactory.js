/**
 * test
 * @author qingqing.he
 * @date 2021-09-09
 */
import http from '@/util/http' // 授权接口
const authorization = true // 密码加密

/**
 * 获取待确认列表
 */
export const getBackList = function (query) {
  return http.request({
    module: 'platform',
    url: `/articlesrelease/back/page`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}
/**
 * 确认返厂
 */
export const backConfirm = function (releaseId) {
  return http.request({
    module: 'platform',
    url: `/articlesrelease/back/confirm/${releaseId}`,
    type: 'post'
  }, {
    authorization: authorization
  })
}
