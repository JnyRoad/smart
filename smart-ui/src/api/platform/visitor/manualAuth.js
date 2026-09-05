import request from '@/router/axios'

/**
 * 查询申请单可手动下发的人员和权限组。
 * @param {string|number} applyId 申请单 ID。
 * @returns {Promise} 网关返回的申请单授权选项。
 */
export function getManualAuthOptions (applyId) {
  return request({
    url: '/platform/manage/admittance/apply/device/auth/options',
    method: 'get',
    params: { applyId }
  })
}

/**
 * 提交单个人员的手动权限下发任务。
 * @param {Object} data 申请单、人员身份和权限 ID 组成的请求 body。
 * @returns {Promise} 网关返回的任务批次号。
 */
export function submitManualAuth (data) {
  return request({
    url: '/platform/manage/admittance/apply/device/auth',
    method: 'post',
    data
  })
}
