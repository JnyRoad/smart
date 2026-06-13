/**
 * @author qingqing.he <qingqing.he@bjtce.com>
 * @date 2021-08-31
 */
import http from '@/util/http'
const authorization = false // 授权接口

/**
 * 获得入厂申请预约温馨提示
 */
export const getNoticeApi = function (data) {
  return http.request({
    module: 'platform',
    url: `/common/config/admittance/notice`,
    type: 'get',
    params: data
  }, {
    authorization: authorization
  })
}
/**
 * 获得OpenId
 */
export const getOpenId = function (data) {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/get/openId`,
    type: 'get',
    params: data
  }, {
    authorization: authorization
  })
}

/**
 * 查询被访人信息
 */
export const searchReceptionistApi = function (data) {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/app/searchReceptionist`,
    type: 'post',
    data: data
  }, {
    authorization: authorization
  })
}
/**
 * 检测是否在黑名单
 */
export const checkBlackList = function (data) {
  return http.request({
    module: 'app',
    url: `/wechat/visit/checkBlackVisitor`,
    type: 'post',
    data: data
  }, {
    authorization: authorization
  })
}
/**
 * 新增入厂预约
 */
export const applySaveApi = function (data) {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/save/apply`,
    type: 'post',
    data: data
  }, {
    authorization: authorization
  })
}

/**
 * 货车入厂预约
 */
export const carApplySaveApi = function (data) {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/save/car/apply`,
    type: 'post',
    data: data
  }, {
    authorization: authorization
  })
}

/**
 * OA入厂申请事由枚举
 */
export const enumCauseApi = function () {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/enum/cause`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
/**
 * OA入厂申请事由枚举合肥
 */
export const enumCauseApiHefei = function () {
  return http.request({
    module: 'platform',
    url: `/visitor/enum/cause/type`,
    type: 'get'
  }, {
    authorization: authorization
  })
}

/**
 * 货车入厂申请事由枚举
 */
export const truckCauseApi = function () {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/enum/car/cause`,
    type: 'get'
  }, {
    authorization: authorization
  })
}

/**
 * OA人员证件类型枚举
 */
export const enumPersonCertApi = function (data) {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/enum/person/cert`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
/**
 * OA车辆证件类型枚举
 */
export const enumCarCertApi = function () {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/enum/vehicle/cert`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
/**
 * OA入厂申请车辆类型枚举
 */
export const enumCarTypeApi = function () {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/enum/vehicle/type`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
/**
 * OA区域类别
 */
export const enumZoneTypeApi = function (query) {
  return http.request({
    module: 'platform',
    url: `/admittance/area/type/list`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}
/**
 * 携带类型枚举
 */
export const enumCarryApi = function () {
  return http.request({
    module: 'platform',
    // url: `/visitor/enum/carry/type`,
    url: `/admittance/apply/enum/carry`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
/**
 * 是否显示健康码和行程码
 */
export const getIsShowCode = function (id) {
  return http.request({
    module: 'platform',
    url: `/common/config/visitor/health`,
    type: 'get',
    params: {
      parkId: id
    }
  }, {
    authorization: authorization
  })
}
/**
 * 车辆颜色
 */
export const enumcarColorApi = function (data) {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/enum/vehicle/color`,
    type: 'get'
  }, {
    authorization: authorization
  })
}
/**
 * 访客获取二维码
 */
export const getCode = function (id) {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/search/Detail/${id}`,
    type: 'get'
  }, {
    authorization: authorization
  })
}

/**
 * 访客身份证验证
 */
export const validateRepeat = function (data) {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/equal/check`,
    type: 'post',
    data: data
  }, {
    authorization: authorization
  })
}

export const getFactoryList = function () {
  return http.request({
    module: 'platform',
    url: `/admittance/area/type/admittance/factory/list`,
    type: 'get'
  }, {
    authorization: authorization
  })
}

export const getAreaType = function (query) {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/enum/factory/type`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}

export const getAreaOptions = function (query) {
  return http.request({
    module: 'platform',
    url: `/admittance/apply/app/area-options`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}
