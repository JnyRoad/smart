
import request from '@/router/axios'
export const xcProjectApi = {
  getList (query, page) {
    return request({
      url: '/platform/security/zone/page',
      method: 'post',
      data: query,
      params: page
    })
  },
  getListAll () {
    return request({
      url: '/platform/security/zone/list',
      method: 'post'
    })
  },
  addObjProject (data) {
    return request({
      url: '/platform/security/zone/save',
      method: 'post',
      data: data
    })
  },
  editObjProject (data) {
    return request({
      url: `/platform/security/zone/edit`,
      method: 'post',
      data: data
    })
  },
  delObjProjectBatch (data) {
    return request({
      url: `/platform/security/zone/batch/delete`,
      method: 'post',
      data: data
    })
  },
  getPersonList (page, query) {
    return request({
      url: '/platform/security/person/page',
      method: 'post',
      data: query,
      params: page,
    })
  },
  searchStaff (query) {
    return request({
      url: '/platform/security/zone/ready/Staff',
      method: 'post',
      data: query
    })
  },
  addPersonToProject(query){
    return request({
      url: '/platform/security/person/save/relation',
      method: 'post',
      data: query
    })
  },
  delPersonFromProjectBatch(query){
    return request({
      url: '/platform/security/person/batch/delete',
      method: 'post',
      data: query
    })
  },
  importPersonToProject(query){
    return request({
      url: '/platform/security/person/export/save/relation',
      method: 'post',
      data: query
    })
  },
  getDeptTree(){
    return request({
      url: '/platform/security/person/staff/tree',
      method: 'post',
    })
  },
  getPersonTreeByDeptId(depId){
    return request({
      url: `/platform/security/person/staff/tree/${depId}`,
      method: 'get'
    })
  }
}
