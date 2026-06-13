import request from '@/router/axios'
export const waterUsageStatisticsApi = {
  getList (query) {
    return request({
      url: '/platform/sd/statistics/page',
      method: 'get',
      params: query
    })
  },
  exportList (query) {
    return request({
      url: '/platform/sd/statistics/list',
      method: 'get',
      params: query
    })
  }
}
