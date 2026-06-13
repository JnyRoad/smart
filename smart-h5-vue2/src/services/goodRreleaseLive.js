/**
 * test
 * @author qingqing.he
 * @date 2021-09-07
 */
import http from '@/util/http' // 授权接口
const authorization = true // 密码加密
/**
 * 根据工号查询宿舍详情，员工宿舍物品放行申请时用到
 * @param {string} staffBadge - 员工工号
 */
export const getRoomDetail = function (staffBadge) {
  return http.request({
    module: 'app',
    url: `/appdormitory/roomList/${staffBadge}`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
/**
 * 物品放行-提交
 * @param {object} data
 */
export const saveApi = function (data) {
  return http.request({
    module: 'platform',
    url: `/articlesrelease/living/save`,
    type: 'post',
    data: data
  }, {
    authorization: authorization
  })
}
/**
 * 物品放行-列表
 * @param {object} query
 * @param {string} query.type - 3：生活区 、4：办公区
 */
export const getPageApi = function (query) {
  return http.request({
    module: 'platform',
    url: `/articlesrelease/page`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}
/**
 * 物品放行-详情
 * @param {string} id
 */
export const getDetailApi = function (id) {
  return http.request({
    module: 'platform',
    url: `/articlesrelease/detail/${id}`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
