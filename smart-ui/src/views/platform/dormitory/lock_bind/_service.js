
import request from '@/router/axios'

export function fetchList (query) {
  return request({
    url: '/platform/dormitory/staff/lock/device/page',
    method: 'get',
    params: query
  })
}

export function allList () {
  return request({
    url: '/platform/park/allList',
    method: 'post'
  })
}

export function fetchRoomList (query) {
  return request({
    url: '/platform/dormitory/room/list',
    method: 'get',
    params: query
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

export function bingRoom (query) {
  return request({
    url: `/platform/dormitory/staff/lock/bind/room`,
    method: 'post',
    data: query
  })
}
