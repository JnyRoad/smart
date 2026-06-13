/**
 * 宿舍申请
 */
import http from '@/util/http' // 授权接口
const authorization = true // 密码加密
/**
  * 根据园区ID查询宿舍信息
  */
export const getDormitoryDetail = function (query) {
  return http.request({
    module: 'platform',
    url: `/dormitory/queryDormitory`,
    type: 'post',
    data: query
  }, {
    authorization: authorization
  })
}

export const getRoomType = function (query) {
  return http.request({
    module: 'platform',
    url: `/dormitory/type/by/park-and-dormitory`,
    type: 'get',
    data: query
  }, {
    authorization: authorization
  })
}

// condition
export const getConditionData = function (query) {
  return http.request({
    module: 'platform',
    url: `/park/tree/condition`,
    type: 'get',
    data: query
  }, {
    authorization: authorization
  })
}

export const getRoomData = function (query) {
  return http.request({
    module: 'platform',
    url: `/dormitory/room/search/condition`,
    type: 'get',
    data: query
  }, {
    authorization: authorization
  })
}

export const getBedData = function (roomId) {
  return http.request({
    module: 'platform',
    url: `/dormitory/room/bedDetail/${roomId}`,
    type: 'post'
  }, {
    authorization: authorization
  })
}

export const getUserData = function (query) {
  return http.request({
    module: 'platform',
    url: `/staff/define/badge`,
    type: 'get',
    data: query
  }, {
    authorization: authorization
  })
}

export const autoallot = function (query) {
  return http.request({
    module: 'platform',
    url: `/dormitory/room/autoallot`,
    type: 'post',
    data: query
  }, {
    authorization: authorization
  })
}

export const getRoomDetail = function (staffBadge) {
  return http.request({
    module: 'platform',
    url: `/dormitory/staff/roomList/${staffBadge}`,
    type: 'get'
    // data: {
    //   staffBadge: staffBadge
    // }
  }, {
    authorization: authorization
  })
}
