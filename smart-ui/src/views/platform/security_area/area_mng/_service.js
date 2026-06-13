
import request from '@/router/axios'
export const areaApi = {
  getList (query) {
    return request({
      url: '/platform/security-area/page',
      method: 'get',
      params: query
    })
  },
  getList1 (page,query) {
    return request({
      url: '/platform/security-area/page',
      method: 'get',
      params: page,
      data: query,
    })
  },
  getObj (id) {
    return request({
      url: `/platform/security-area/get/${id}`,
      method: 'get'
    })
  },
  addObj (data) {
    return request({
      url: `/platform/security-area/add`,
      method: 'post',
      data
    })
  },
  editObj (data) {
    return request({
      url: `/platform/security-area/update`,
      method: 'post',
      data
    })
  },
  delObj (id) {
    return request({
      url: `/platform/security-area/delete/${id}`,
      method: 'get'
    })
  }
}
