import request from '@/router/axios'
export const waterUsageStatisticsApi = {
  getList (query) {
    return request({
      url: '/platform/sd/statistics/use-page',
      method: 'get',
      params: query
    })
  },
  exportList (query) {
    return request({
      url: '/platform/sd/statistics/use-list',
      method: 'get',
      params: query
    })
  }
}
