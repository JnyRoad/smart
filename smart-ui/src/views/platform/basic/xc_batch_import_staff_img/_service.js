
import request from '@/router/axios'
export const xcBatchImgApi = {
  getList1 (query) {
    return request({
      url: '/platform/device/page',
      method: 'get',
      params: query
    })
  },
  getList (query, obj) {
    return request({
      url: '/platform/staff/img/task/page',
      method: 'post',
      data: obj,
      params: query,
      timeout: 3000*60*6
    })
  },
  getObj (query) {
    return request({
      url: `/platform/staff/img/detail/page`,
      method: 'get',
      params: query
    })
  },
  delObj (id) {
    return request({
      url: `/platform/staff/img/task/${id} `,
      method: 'post'
    })
  },
  exportDetail (query) {
    return request({
      url: `/platform/staff/img/detail/excel`,
      method: 'get',
      params: query,
      responseType: 'arraybuffer'
    })
  },
  importImg (data) {
    return request({
      url: '/platform/staff/img/task/upload',
      method: 'post',
      data: data
    })
  },

}
