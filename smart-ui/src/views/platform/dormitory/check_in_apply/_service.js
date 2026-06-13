
import request from '@/router/axios'

/*
  分页记录
*/
export function fetchList (query) {
  return request({
    url: '/platform/dormitory/apply/page',
    method: 'get',
    params: query
  })
}

/*
  手动分配
*/
export function applyManual (data) {
  return request({
    url: '/platform/dormitory/apply/manual',
    method: 'post',
    params: data
  })
}

/*
  退回
*/
export function failBack (data) {
  return request({
    url: '/platform/dormitory/apply/failback',
    method: 'post',
    data: data
  })
}

/*
  自动推荐
*/
export function recommend (data) {
  return request({
    url: '/platform/dormitory/apply/recommend',
    method: 'post',
    params: data
  })
}

/*
  根据roomId查询床位
*/
export function bedDetail (roomId) {
  return request({
    url: `/platform/dormitory/room/bedDetail/${roomId}`,
    method: 'post'
  })
}

/*
  查询树形
*/
export function getDormTreeByApplyId (applyId) {
  return request({
    url: `/platform/park/dormRoomTree/byApplyId/${applyId}`,
    method: 'get'
  })
}
