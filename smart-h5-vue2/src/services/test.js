/**
 * test
 * @author yang.chuan <yang.chuan@bjtce.com>
 * @date 2020-11-25
 */
import http from '@/util/http'
const authorization = true // 授权接口

/**
 * 接口测试
 */
export const getTest = function () {
  return http.request({
    module: 'visitor',
    url: `/access/article/type/list`,
    type: 'GET'
  }, {
    authorization: authorization
  })
}
