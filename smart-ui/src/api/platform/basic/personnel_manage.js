
/**
 * 人员管理服务接口
 * @author yang.chuan <yang.chuan@bjtce.com>
 * @date 2020-10-09
 */

import request from '@/router/axios'

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
    return {
      "badge": item.jobNumber,
      "certno": item.identity,
      "depName": item.department,
      "jcheName": item.rank,
      "jobName": item.post,
      "name": item.name,
      "phone": item.phone,
      "entryTime": item.entryTime,
      "dispatch": item.dispatch
    }
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
      data: {
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
      }
    })
  }
  return request({
    url: '/platform/staff/addTempStaff',
    method: 'POST',
    data: {
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
    }
  })
}

/**
 * 查询临时员工
 */
export function getStaff({
  staffId
}) {
  return request({
    url: '/platform/staff/admin/temporary/' + staffId,
    method: 'get',
  }).then(response => normalizeTemporaryStaffResponse(response))
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
  isFace
}) {
  const sendData = Object.assign({}, query)
  ;[
    ['depId', depId],
    ['badge', badge],
    ['name', name],
    ['isFace', isFace]
  ].forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      sendData[key] = value
    }
  })
  return request({
    url: '/platform/staff/admin/temporary/page',
    method: 'post',
    data: sendData
  }).then(response => normalizeTemporaryStaffResponse(response))
}

/**
 * 临时人员接口只在前端保留页面编辑和组织展示所需字段。
 *
 * 即使服务端将来错误附带身份证、手机号或人脸，客户端也不能把它们带入页面状态。
 */
export function normalizeTemporaryStaff(staff) {
  const normalized = { id: staff.staffId }
  ;['badge', 'name', 'sex', 'jobName', 'depId', 'depName', 'jcheId', 'jcheName', 'status', 'entryTime', 'dispatch']
    .forEach(key => {
      if (staff[key] !== undefined) {
        normalized[key] = staff[key]
      }
    })
  return normalized
}

function normalizeTemporaryStaffResponse(response) {
  const body = response && response.data ? response.data : {}
  const data = body.data
  if (data && Array.isArray(data.records)) {
    return Object.assign({}, response, {
      data: Object.assign({}, body, {
        data: Object.assign({}, data, { records: data.records.map(normalizeTemporaryStaff) })
      })
    })
  }
  return Object.assign({}, response, {
    data: Object.assign({}, body, { data: data ? normalizeTemporaryStaff(data) : data })
  })
}



/**
 * 将后台员工查询响应投影为页面选择控件所需的最小字段。
 *
 * 这里不能透传接口新增字段，避免证件号、手机号等敏感数据重新进入管理端内存。
 */
export function normalizeLookup(staff) {
  return {
    id: staff.staffId,
    badge: staff.badge,
    name: staff.name,
    departmentName: staff.departmentName
  }
}

/**
 * 搜索主管列表。
 *
 * 后端只返回当前管理员园区范围内的最小员工信息。
 */
export function getSearchStaff({
  badge
}) {
  return request({
    url: '/platform/staff/lookup',
    method: 'get',
    params: {
      badge: badge
    }
  }).then(response => {
    const body = response && response.data ? response.data : {}
    const staffList = Array.isArray(body.data) ? body.data : []
    return Object.assign({}, response, {
      data: Object.assign({}, body, {
        data: staffList.map(normalizeLookup)
      })
    })
  })
}

/**
 * 批量离职前查询当前管理员园区内的临时员工。
 *
 * 园区范围由后端认证主体确定，前端不能再传入可伪造的 compId，也不会保留
 * 身份证、手机号等历史响应字段。
 */
export function getTemporaryStaffByBadgeBatch(obj) {
  return request({
    url: `/platform/staff/admin/temporary/by-badges`,
    method: 'get',
    params: {
      badges: obj.badges
    }
  }).then(response => {
    const body = response && response.data ? response.data : {}
    const records = Array.isArray(body.data) ? body.data : []
    return Object.assign({}, response, {
      data: Object.assign({}, body, {
        data: records.map(staff => ({
          id: staff.staffId,
          badge: staff.badge,
          name: staff.name
        }))
      })
    })
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
  const { status, ...temporaryQuery } = data
  return request({
    url: `/platform/staff/admin/temporary/page`,
    method: 'post',
    data: temporaryQuery
  }).then(response => normalizeTemporaryStaffResponse(response))
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
