
import request from '@/router/axios'
export const xcSignMngApi = {
  getList (page,query) {
    return request({
      url: '/platform/security/person/all/staff',
      method: 'post',
      params: page,
      data: query,
    })
  },
  getProjectsByStaffId (staffId) {
    return request({
      url: `/platform/security/zone/byStaff/${staffId}`,
      method: 'get'
    })
  },
  delObjBatch () {
    return request({
      url: `/platform/security/person/batch/delete`,
      method: 'post'
    })
  }
}
