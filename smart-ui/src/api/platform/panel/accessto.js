import request from '@/router/axios'

export function getVisitorIn () {
  return request({
    url: '/platform/visitor/searchVisitorInToday',
    method: 'get'
  })
}
export function getVisitorOut () {
  return request({
    url: '/platform/visitor/searchVisitorOutToday',
    method: 'get'
  })
}
export function getVisitorDevice () {
  return request({
    url: '/platform/visitor/searchVisitorDeviceToday',
    method: 'get'
  })
}
export function getVisitorAnalysis () {
  return request({
    url: '/platform/visitor/searchVisitorAnalysisToday',
    method: 'get'
  })
}
export function getVisitorNew () {
  return request({
    url: '/platform/visitor/searchVisitorDeviceAnalysisToday',
    method: 'get'
  })
}
export function getVehicleCount () {
  return request({
    url: '/platform/snap/vehicle/count',
    method: 'get'
  })
}