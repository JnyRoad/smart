
/**
 * 人员管理服务接口
 * @author yang.chuan <yang.chuan@bjtce.com>
 * @date 2020-10-09
 */

import request from '@/router/axios'

/**
 * 人员资料会被下发到 ISC，所有文本字段必须在提交前去除首尾空白。
 */
const normalizePersonnelPayload = payload => Object.keys(payload).reduce((normalized, key) => {
  const value = payload[key]
  normalized[key] = typeof value === 'string' ? value.trim() : value
  return normalized
}, {})

/**
 * 获得APP权限列表
 */
export function getAppauth() {
  return request({
    url: '/platform/appauth/page',
    method: 'get'
  })
}

/**
 * 获得职层列表
 */
export function getRecruitment() {
  return request({
    url: '/platform/recruitment/getJche',
    method: 'get'
  })
}

/**
 * 获得部门树形
 */
export function getDeptTree() {
  return request({
    url: '/platform/ext/dept/tree',
    method: 'get'
  })
}

/**
 * 获得部门所有列表
 */
export function getDeptList() {
  return request({
    url: '/platform/ext/dept/list',
    method: 'get'
  })
}

/**
 * 获得c6部门
 */
 export function getC6DeptList() {
  return request({
    url: '/platform/c6/dept/tree',
    method: 'get'
  })
}


/**
 * 获得部门详情
 */
export function getDeptDetails({
  id
}) {
  return request({
    url: `/platform/ext/dept/${id}`,
    method: 'get'
  })
}

/**
 * 移除部门
 */
export function delDept({
  id
}) {
  return request({
    url: `/platform/ext/dept/${id}`,
    method: 'post'
  })
}

/**
 * 保存部门
 */
export function postDeptSave({
  compId,
  deptName,
  director,
  directorName,
  id,
  parentDept,
  c6DeptNo
}) {
  if (id) {
    return request({
      url: '/platform/ext/dept/save',
      method: 'POST',
      data: {
        compId,
        deptName,
        director,
        directorName,
        id,
        parentDept,
        c6DeptNo
      }
    })
  }
  return request({
    url: '/platform/ext/dept/save',
    method: 'POST',
    data: {
      compId,
      deptName,
      director,
      directorName,
      parentDept,
      c6DeptNo
    }
  })
}

/**
 * 根据部门id获得部门主管工号+姓名
 * @param {*} data
 */
export function getDirector({
  id
}) {
  return request({
    url: `/platform/ext/dept/director/${id}`,
    method: 'get'
  })
}


/**
 * 导入临时员工
 */
export function postImportStaff(data) {
  const sendData = data.map(item => {
    return normalizePersonnelPayload({
      "badge": item.jobNumber,
      "certno": item.identity,
      "depName": item.department,
      "jcheName": item.rank,
      "jobName": item.post,
      "name": item.name,
      "phone": item.phone,
      "entryTime": item.entryTime,
      "dispatch": item.dispatch
    })
  })
  return request({
    url: '/platform/staff/addBatchTempStaff',
    method: 'POST',
    data: sendData
  })
}

/**
 * 保存临时员工
 */
export function postAddStaff({
  appAuth,
  badge,
  certno,
  depId,
  depName,
  faceImg,
  id,
  jcheId,
  jcheName,
  jobName,
  name,
  phone,
  sex,
  entryTime,
  dispatch,
  status
}) {
  if (id) {
    return request({
      url: '/platform/staff/updateTempStaff',
      method: 'POST',
      data: normalizePersonnelPayload({
        appAuth,
        badge,
        certno,
        depId,
        depName,
        faceImg,
        id,
        jcheId,
        jcheName,
        jobName,
        name,
        phone,
        sex,
        entryTime,
        dispatch,
        status
      })
    })
  }
  return request({
    url: '/platform/staff/addTempStaff',
    method: 'POST',
    data: normalizePersonnelPayload({
      appAuth,
      badge,
      certno,
      depId,
      depName,
      faceImg,
      jcheId,
      jcheName,
      jobName,
      name,
      phone,
      sex,
      entryTime,
      dispatch,
      status
    })
  })
}

/**
 * 查询临时员工
 */
export function getStaff({
  staffId
}) {
  return request({
    url: '/platform/staff/getTempById',
    method: 'get',
    params: {
      staffId: staffId
    },
  })
}

/**
 * 移除
 * @param {*} param0
 */
export function postDelStaff({
  staffId
}) {
  return request({
    url: '/platform/staff/deleteTempStaff',
    method: 'POST',
    params: {
      staffId: staffId
    },
  })
}


/**
 * 临时人员列表
 * @param {*} data
 */
export function getStaffPage(query, {
  depId,
  badge,
  name,
  isFace,
  status
}) {
  const sendData = Object.assign({}, query, {
    depId,
    badge,
    name,
    isFace,
    status
  })
  return request({
    url: '/platform/staff/getTempList',
    method: 'post',
    data: sendData,
    params: sendData
  })
}



/**
 * 搜索主管列表
 */
export function getSearchStaff({
  badge
}) {
  return request({
    url: '/platform/staff/simple/badge',
    method: 'get',
    params: {
      badge: badge
    }
  })
}

/**
 * 批量离职，根据工号查询员工信息
 */
export function getStaffByBadge(obj) {
  return request({
    url: `/platform/staff/staffByBadge`,
    method: 'get',
    params: {
      badge: obj.badge,
      compId: obj.compId
    }
  })
}

/**
 * 批量离职，根据多个工号 批量查询员工信息
 */
 export function getStaffByBadgeBatch(obj) {
  return request({
    url: `/platform/staff/staffByBadges`,
    method: 'get',
    params: {
      badges: obj.badges,
      compId: obj.compId
    }
  })
}

/**
 * 批量离职
 */
export function delStaffBatch(data) {
  const sendData = data.map(item => {
    return item.id
  })
  return request({
    url: `/platform/staff/deleteBatchTempStaff`,
    method: 'post',
    data: {
      ids: sendData
    }
  })
}

/**
 * 批量查询离职员工
 */
 export function searchPersonList (data) {
  return request({
    url: `/platform/staff/getTempList`,
    method: 'post',
    data: data
  })
}

export function reinstatementSave (data) {
  return request({
    url: `/platform/staff/update/temp/status`,
    method: 'post',
    data: data
  })
}

/**
 * 复职保存
 */
// reinstatement
