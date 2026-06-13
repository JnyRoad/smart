
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/bed/page',
    method: 'get',
    params: query,
    timeout: 3000*60*5
  })
}
export function exportFamily (query) {
  return request({
    url: '/platform/dormitory/bed/family/list',
    method: 'get',
    params: query,
    timeout: 3000*60*5
  })
}
//左侧树形查询，其他页面都用这个接口
// export function allList () {
//   return request({
//     url: '/platform/park/allList',
//     method: 'post'
//   })
// }
export function getLeaveCount (query) {
  return request({
    url: '/platform/dormitory/bed/getLeaveStaff',
    method: 'get',
    params: query
  })
}
//左侧树形查询，其他页面用的跟这个接口不一样，这个接口在房间后面要显示（标准人数//实住人数/剩余空位）
export function allList () {
  return request({
    url: '/platform/park/dormitory/allList',
    method: 'post'
  })
}
export function fetchStaffList (query) {
  return request({
    url: '/platform/staff/quetyStaffNODormitory',
    method: 'get',
    params: query
  })
}
//员工入住
export function addDormitoryStaff (query) {
  return request({
    url: '/platform/dormitory/staff/addDormitoryStaff',
    method: 'post',
    data: query
  })
}
//非员工入住
export function addDormitory (query) {
  return request({
    url: '/platform/dormitory/staff/addDormitory',
    method: 'post',
    data: query
  })
}
//非员工入住,编辑
export function editCheckInDormitory (query) {
  return request({
    url: '/platform/dormitory/bed/update',
    method: 'post',
    data: query
  })
}
export function checkOut (query) {
  return request({
    url: '/platform/dormitory/staff/changeDormitory',
    method: 'post',
    data: query
  })
}
//修改入住时间
export function updateDormitoryStaff (query) {
  return request({
    url: '/platform/dormitory/staff/updateDormitoryStaff',
    method: 'post',
    data: query
  })
}
//修改备注
export function updateSimpleRemark (query) {
  return request({
    url: '/platform/dormitory/staff/update/remark',
    method: 'get',
    params: query
  })
}
//修改床位编号
export function updateBedName (query) {
  return request({
    url: '/platform/dormitory/bed/updatename',
    method: 'post',
    data: query
  })
}
export function callOwanceDetails (query) {
  return request({
    url: '/platform/out/dormitory/staff/callOwanceDetails',
    method: 'get',
    params: query
  })
}
//是否锁定
export function isLock (data) {
  return request({
    url: '/platform/dormitory/bed/switchDelFlg',
    method: 'post',
    data: data
  })
}
//入住信息导入，json格式
export function importBatch (data) {
  return request({
    url: '/platform/dormitory/staff/batch',
    method: 'post',
    data: data
  })
}
//入住信息导入，excel文件
export function importBatchExcel (data) {
  return request({
    url: `/platform/dormitory/staff/info`,
    method: 'post',
    data: data,
    headers: { 'content-type': 'multipart/form-data' },
    responseType: 'arraybuffer',
    timeout: 1000*60*5
  })
}
//批量移除，已入住未报道
export function delBatch (data) {
  return request({
    url: `/platform/dormitory/staff/delete/leave`,
    method: 'post',
    data: data
  })
}

//住宿备注，查询
export function remarkList (dorStaffId) {
  return request({
    url: `/platform/dormitory/out/remark/list/${dorStaffId}`,
    method: 'get',
  })
}
//住宿备注，新增/编辑
export function remarAdd (data) {
  return request({
    url: `/platform/dormitory/out/remark`,
    method: 'post',
    data: data
  })
}
//住宿备注，删除
export function remarkDel (id) {
  return request({
    url: `/platform/dormitory/out/remark/delete/${id}`,
    method: 'get'
  })
}