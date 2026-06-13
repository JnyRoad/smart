import request from '@/router/axios'
export function saveData(data) {
  return request({
    url: `/platform/config/meterread/edit`,
    method: 'post',
    data: data
  })
}
export function getDetails(parkId) {
  return request({
    url: `/platform/config/meterread/details/${parkId}`,
    method: 'get'
  })
}

/**
   * 查询用户所有园区
   */
 export function allPark (query) {
  return request({
    url: '/platform/park/all',
    method: 'get'
  })
}