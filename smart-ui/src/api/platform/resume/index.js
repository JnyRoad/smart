import request from '@/router/axios'

/**
 * 获取关系字典
 */
export function getRelations () {
  return request({
    url: '/platform/regist/relation/list',
    method: 'get'
  })
}
/**
 * 获取学位字典
 */
export function getDegrees () {
  return request({
    url: '/platform/regist/degree/type',
    method: 'get'
  })
}
/**
 * 获取学历字典
 */
export function getEducations () {
  return request({
    url: '/platform/regist/education/type',
    method: 'get'
  })
}
/**
 * 证件正面和反面识别
 */
export function ocrRead (param) {
  return request({
    url: '/platform/regist/ocrRead',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 保存识别的身份证信息
 */
export function saveIdentification (param) {
  return request({
    url: '/platform/regist/save/identification',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 上传人脸
 */
export function addFace (param) {
  return request({
    url: '/platform/regist/face/add',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 发送短信
 */
export function sendMsg (phone) {
  return request({
    url: '/platform/regist/send/' + phone,
    method: 'get'
  })
}
/**
 * 绑定手机
 */
export function bindMobile(param) {
  return request({
    url: '/platform/regist/bind/mobile',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 添加紧急联系人
 */
export function addEmergency(param) {
  return request({
    url: '/platform/regist/addApplicationEmergency',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 查询紧急联系人
 */
export function getEmergency (id) {
  return request({
    url: '/platform/regist/getEmployee/' + id,
    method: 'get'
  })
}
/**
 * 修改紧急联系人
 */
export function editEmergency(param) {
  return request({
    url: '/platform/regist/updateEmergency',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 删除紧急联系人
 */
export function delEmergency(id) {
  return request({
    url: '/platform/regist/deleteEmergency/' + id,
    method: 'get'
  })
}
/**
 * 添加教育经历
 */
export function addEducation(param) {
  return request({
    url: '/platform/regist/addApplicationeEducation',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 查询教育经历
 */
export function getEducation(id) {
  return request({
    url: '/platform/regist/education/list/' + id,
    method: 'get'
  })
}
/**
 * 修改教育经历
 */
export function editEducation(param) {
  return request({
    url: '/platform/regist/updateApplicationeEducation',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 删除教育经历
 */
export function delEducation(id) {
  return request({
    url: '/platform/regist/deleteEducation/' + id,
    method: 'get'
  })
}
/**
 * 添加工作经验
 */
export function addWork(param) {
  return request({
    url: '/platform/regist/addApplicationWork',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 修改工作经验
 */
export function editWork(param) {
  return request({
    url: '/platform/regist/updateApplicationWork',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 查询工作经验
 */
export function getWork(id) {
  return request({
    url: '/platform/regist/work/list/' + id,
    method: 'get'
  })
}
/**
 * 删除工作经验
 */
export function delWork(id) {
  return request({
    url: '/platform/regist/deleteWork/' + id,
    method: 'get'
  })
}
/**
 * 添加家庭成员
 */
export function addFamily(param) {
  return request({
    url: '/platform/regist/addApplicationFamily',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 修改家庭成员
 */
export function editFamily(param) {
  return request({
    url: '/platform/regist/updateApplicationFamily',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 查询家庭成员
 */
export function getFamily(id) {
  return request({
    url: '/platform/regist/family/list/' + id,
    method: 'get'
  })
}
/**
 * 删除家庭成员
 */
export function delFamily(id) {
  return request({
    url: '/platform/regist/deleteFamily/' + id,
    method: 'get'
  })
}
/**
 * 根据工号查询任职关系所需的最小员工资料。
 *
 * 先通过受权限和园区范围约束的工号检索取得主键，再读取受控详情；不能再请求
 * 会返回完整员工实体的历史接口。此处保留旧页面的 smtStaff 外层结构，避免影响
 * 任职关系弹窗的既有渲染逻辑。
 *
 * @param {string} badge 员工工号
 * @returns {Promise<{data: {data: {smtStaff: object|null}}>}>
 */
export async function selectStaffInfo (badge) {
  const normalizedBadge = String(badge || '').trim()
  const lookupResponse = await request({
    url: '/platform/staff/lookup',
    method: 'get',
    params: { badge: normalizedBadge }
  })
  const candidates = (lookupResponse.data && lookupResponse.data.data) || []
  const matchedStaff = candidates.find(item => item.badge === normalizedBadge)
  if (!matchedStaff) {
    return { data: { data: { smtStaff: null } } }
  }

  const detailResponse = await request({
    url: '/platform/staff/admin/' + matchedStaff.staffId,
    method: 'get'
  })
  const detail = detailResponse.data && detailResponse.data.data
  if (!detail) {
    return { data: { data: { smtStaff: null } } }
  }
  return {
    data: {
      data: {
        smtStaff: {
          badge: detail.badge,
          name: detail.name,
          sex: detail.sex,
          compName: detail.companyName,
          depName: detail.departmentName,
          jobName: detail.jobName
        }
      }
    }
  }
}

/**
 * 添加任职关系
 */
export function addRelation(param) {
  return request({
    url: '/platform/application/relation/addApplicationRelation',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 修改任职关系
 */
export function editRelation(param) {
  return request({
    url: '/platform/application/relation/updateApplicationRelation',
    method: 'post',
    contentType:'application/json',
    data: param
  })
}
/**
 * 查询任职关系
 */
export function getRelation(id) {
  return request({
    url: '/platform/application/relation/getApplicationInfo/' + id,
    method: 'get'
  })
}
/**
 * 删除任职关系
 */
export function delRelation(id) {
  return request({
    url: '/platform/application/relation/' + id,
    method: 'post'
  })
}
/**
 * 提交
 */
export function deliver(query) {
  return request({
    url: '/platform/regist/deliver',
    method: 'get',
    params: query
  })
}
