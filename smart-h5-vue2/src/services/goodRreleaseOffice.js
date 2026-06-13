/**
 * test
 * @author qingqing.he
 * @date 2021-09-07
 */
import http from '@/util/http' // 授权接口
const authorization = true // 密码加密
/**
 * 物品放行-提交
 * @param {object} data
 */
export const saveApi = function (data) {
  return http.request({
    module: 'platform',
    url: `/articlesrelease/office/save`,
    type: 'post',
    data: data
  }, {
    authorization: authorization
  })
}

export const getPageApi = function (query) {
  return http.request({
    module: 'platform',
    url: `/articlesrelease/office/page`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}

export const getPersonData = function (badge) {
  return http.request({
    module: 'platform',
    url: `/articlesrelease/oa/staff/info/${badge}`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
// /articlesrelease/oa/staff/info/
/**
 * 物品放行-详情-当前和生活区详情用的同一个
 * @param {string} id
 */
// export const getDetailApi = function (id) {
//   return http.request({
//     module: 'platform',
//     url: `/articlesrelease/office/detail/${id}`,
//     type: 'get'
//   }, {
//     authorization: authorization
//   })
// }
