
import request from '@/router/axios'
export const xcDeviceTagApi = {
  getList (query) {
    return request({
      url: '/platform/device/tag/page',
      method: 'get',
      params: query
    })
  },
  addObj (data) {
    return request({
      url: '/platform/device/tag/save',
      method: 'post',
      params: data
    })
  },
  editObj (data, id) {
    return request({
      url: `/platform/device/tag/update/${id}`,
      method: 'post',
      params: data
    })
  },
  delObj (id) {
    return request({
      url: `/platform/device/tag/${id}`,
      method: 'post'
    })
  }
}
