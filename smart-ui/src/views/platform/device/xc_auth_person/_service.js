
import request from '@/router/axios'
export const xcAuthPersonApi = {
  getList (query) {
    return request({
      url: '/platform/device/person/auth/page',
      method: 'get',
      params: query
    })
  },
  exportApi (query) {
    return request({
      url: '/platform/device/person/auth/export',
      method: 'get',
      params: query,
      responseType: 'arraybuffer'
    })
  }
}
