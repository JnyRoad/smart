
import request from '@/router/axios'

const baseUrl = '/platform'
/**
 * 查询用户所有园区
 */
export function allPark () {
  return request({
    url: `${baseUrl}/park/all`,
    method: 'get'
  })
}
/**
 * BU -【查询所有bu】
 */
export function getCompsObj () {
  return request({
    url: `${baseUrl}/recruitment/getComp`,
    method: 'get'
  })
}
/**
 * BU -【与登录用户园区相关联的bu】
 */
export function getComp () {
  return request({
    url: `${baseUrl}/vehicle/comp`,
    method: 'get'
  })
}
/**
 * 园区→BU→部门 -【三级联动组件使用接口】
 */
export function getCompTree () {
  return request({
    url: `${baseUrl}/vehicle/comp/tree`,
    method: 'get'
  })
}
/**
 * @param {number|string} compId bu的id
 * 部门
 */
export function getDeptsObj (compId) {
  return request({
    url: `${baseUrl}/recruitment/getDep/${compId}`,
    method: 'get'
  })
}
/**
 * @param {number|string} deptId 部门的id
 * 岗位
 */
export function getJobsObj (deptId) {
  return request({
    url: `${baseUrl}/recruitment/getJob/${deptId}`,
     method: 'get'
  })
}
/**
 * 职层
 */
export function getJchesObj () {
  return request({
    url: `${baseUrl}/recruitment/getJche`,
    method: 'get'
  })
}
/**
 * 根据工号查询员工详情
 */
export function getStaffDetail (badge) {
  return request({
    url: `${baseUrl}/vehicle/staff/detail/${badge}`,
    method: 'get'
  })
}
