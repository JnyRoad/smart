
import request from '@/router/axios'


export function fetchList(query, obj) {
  return request({
    url: '/platform/recharge/page',
    method: 'post',
    data: obj,
    params: query,
    timeout: 3000*60*5
  })
}

export function getSeniorInfo(query) {
  return request({
    url: '/platform/recharge/senior/recharge',
    method: 'get',
    params: query,
    timeout: 3000*60*5
  })
}

export function exportTitle() {
  return request({
    url: '/platform/recharge/excel/title',
    method: 'get'
  })
}

export function toC6(obj) {
  return request({
    url: '/platform/recharge/toC6',
    method: 'post',
    data: obj
  })
}

//批量删除，一键清除
export function deleteRecharge(obj) {
  return request({
    url: '/platform/recharge/delete/recharge',
    method: 'post',
    data: obj
  })
}

//特殊名单充值
export function singleRecharge(obj) {
  return request({
    url: '/platform/recharge/single/recharge',
    method: 'get',
    params: obj
  })
}

//修改应发餐补
export function updateRecharge(obj) {
  return request({
    url: '/platform/recharge/update/recharge',
    method: 'get',
    params: obj
  })
}
