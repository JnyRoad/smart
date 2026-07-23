
import request from '@/router/axios'

export function roleList () {
  return request({
    url: '/admin/role/roleList',
    method: 'get'
  })
}

/**
 * 只保留后台员工列表契约允许的查询字段。
 *
 * 园区、BU 由后端从认证主体获取，个人联系方式和人脸文件标识也不得作为前端
 * 请求字段继续传播。
 */
export function buildAdminStaffPageQuery (query = {}) {
  const {
    name,
    badge,
    badges,
    depId,
    depAbbr,
    jobId,
    jobName,
    jcheId,
    status,
    hasFace,
    startTime,
    endTime
  } = query
  return {
    name,
    badge,
    badges,
    depId,
    depAbbr,
    jobId,
    jobName,
    jcheId,
    status,
    hasFace,
    startTime,
    endTime
  }
}

/** 将响应投影成页面实际消费的最小展示字段，拒绝持久化意外返回的 PII。 */
export function normalizeAdminStaffPageItem (staff = {}) {
  return {
    id: staff.staffId,
    badge: staff.badge,
    name: staff.name,
    compName: staff.compName,
    depAbbr: staff.depAbbr,
    depName: staff.depName,
    jcheName: staff.jcheName,
    jobName: staff.jobName,
    createTime: staff.createTime,
    status: staff.status,
    parkName: staff.parkName,
    hasFace: staff.hasFace,
    deviceAuth: staff.deviceAuth,
    appAuth: staff.appAuth
  }
}

export function fetchList (query) {
  const safeQuery = buildAdminStaffPageQuery(query)
  return request({
    url: '/platform/staff/admin/page',
    method: 'post',
    params: {
      current: query.current,
      size: query.size
    },
    data: safeQuery
  }).then((response) => {
    const page = response && response.data && response.data.data
    if (page && Array.isArray(page.records)) {
      page.records = page.records.map(normalizeAdminStaffPageItem)
    }
    return response
  })
}
// 查询APP权限
export function fetchAppList () {
  return request({
    url: '/platform/appauth/list',
    method: 'get'
  })
}
// 修改员工APP权限
export function editAppList (obj) {
  return request({
    url: '/platform/staff/auth/app/update',
    method: 'post',
    data: obj
  })
}
/**
 *
 * 查询所有设备权限策略展示到select
 */
export function deviceAuthList (query) {
  return request({
    url: '/platform/device/authority/page',
    method: 'get',
    params: query
  })
}


/**
 *
 * 修改员工的通关权限策略
 */
export function upDeviceAuthList (obj,type) {
  return request({
    url: `/platform/staff/device/auth/updateAuth/${type}`,
    method: 'post',
    data: obj
  })
}
/**
 *
 * 权限下发进度查询
 */
 export function issueAuth (query) {
  return request({
    url: `/platform/staff/device/auth/authInfo`,
    method: 'get',
    params: query
  })
}

/**
 *
 * 根据员工照片名称（工号）查询当前员工照片信息
 */
export function getStaffImgInfo (obj) {
  return request({
    url: '/platform/staff/check/facePic',
    method: 'post',
    data: obj
  })
}
export function getAuth (id) {
  return request({
     url: '/platform/staff/getAuthInfo/'+id,
      method: 'get'
  })
}
/**
 *
 * 上传员工照片
 */
export function importImgs (obj) {
  return request({
    url: '/platform/staff/upload/facePic',
    method: 'post',
    data: obj
  })
}
