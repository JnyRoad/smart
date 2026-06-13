/**
 * test
 * @author qingqing.he
 * @date 2021-09-06
 */
import http from '@/util/http' // 授权接口
const authorization = true // 密码加密

// 审批公用接口
// export const API_PROCESS_PREVIEW = '/app/process/preview' // 预览审批流程
// export const API_APPROVE_RECORD = '/app/approve/record' // 待审批离职请求列表
// export const API_APPLICATION_APPROVE_NORMAL = '/app/application/approve/normal' // 普通审批操作

/**
 * 获取待审批列表（待审批、已审批）【物品放行-生活区，和其他通用】
 */
export const getBackLogList = function (data) {
  return http.request({
    module: 'platform',
    url: `/approve/list/new/page`,
    type: 'get',
    params: data
  }, {
    authorization: authorization
  })
}
/**
 * 获取待审批列表【仅物品放行-办公区】
 */
export const getBackLogListGoodReleaseWork = function (query) {
  return http.request({
    module: 'platform',
    url: `/articlesrelease/office/page`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}
/**
 * 保安放行
 */
export const securityStatusUpdate = function (data) {
  return http.request({
    module: 'platform',
    url: `/articlesrelease/status/security/update`,
    type: 'post',
    data: data
  }, {
    authorization: authorization
  })
}
/**
 * 物品放行审批（生活区）
 */
export const staffStatusUpdate = function (query) {
  return http.request({
    module: 'platform',
    url: `/articlesrelease/status/update`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}
/**
 * 微信扫一扫，获取配置信息
 */
export const getWxSignature = function (query) {
  return http.request({
    module: 'app',
    url: `/wechat/sign`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}

/**
 * 园区报修-列表
 */
export const getDormRepairsList = function (query) {
  return http.request({
    module: 'platform',
    url: `/approve/list/repairs/list`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}

/**
 * 园区报修-接单
 */
export const confirmDormRepairs = function (query) {
  return http.request({
    module: 'platform',
    url: `/dormitory/repair/status/update`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}

/**
 * 园区报修-维修结果
 */
export const _confirmDormRepairs = function (query) {
  return http.request({
    module: 'platform',
    url: `/dormitory/repair/reply`,
    type: 'post',
    data: query
  }, {
    authorization: authorization
  })
}

/**
 * 退宿申请-列表
 */
export const getDormExitList = function (query) {
  return http.request({
    module: 'platform',
    url: `/dor/quit/list/approval`,
    type: 'post',
    data: query
  }, {
    authorization: authorization
  })
}

/**
 * 退宿申请-审批
 */
export const confirmDormExit = function (query) {
  return http.request({
    module: 'platform',
    url: `/dor/quit/status/update`,
    type: 'get',
    params: query
  }, {
    authorization: authorization
  })
}
