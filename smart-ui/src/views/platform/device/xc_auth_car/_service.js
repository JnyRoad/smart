
import request from '@/router/axios'
export const xcAuthCarApi = {
  getList (query) {
    return request({
      url: '/platform/device/vehicle/auth/page',
      method: 'get',
      params: query
    })
  },
  exportApi (query) {
    return request({
      url: '/platform/device/vehicle/auth/export',
      method: 'get',
      params: query,
      responseType: 'arraybuffer'
    })
  }
}
