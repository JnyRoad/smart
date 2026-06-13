
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/staff/todayIn',
    method: 'post',
    params: query
  })
}

export function bedDetail (roomId) {
  return request({
    url: `/platform/dormitory/room/bedDetail/${roomId}`,
    method: 'post'
  })
}
export function changeBed (param) {
  return request({
    url: `/platform/dormitory/staff/changeBed`,
    method: 'post',
    params: param
  })
}
export function autoallot (obj) {
  return request({
    url: `/platform/dormitory/room/autoallot`,
    method: 'post',
    data: obj
  })
}

export function getDormTreeByRoomId (roomId) {
  return request({
    url: `/platform/park/dormRoomTree/byRoomId/${roomId}`,
    method: 'get'
  })
}

export function getFloors (query) {
  return request({
    url: `/platform/park/tree/condition`,
    method: 'get',
    params: query
  })
}

export function getRooms (query) {
  return request({
    url: `/platform/dormitory/room/search/condition`,
    method: 'get',
    params: query
  })
}

export function checkDormInInfo (query) {
  return request({
    url: `/platform/dormitory/room/query/past/dormitory`,
    method: 'get',
    params: query
  })
}
