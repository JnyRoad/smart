import request from '@/router/axios'
export const deviceApi = {
  getList (query) {
    return request({
      url: '/platform/dormitory/staff/lock/device/page',
      method: 'get',
      params: query
    })
  }
}
