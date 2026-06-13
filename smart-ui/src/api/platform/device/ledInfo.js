
import request from '@/router/axios'

//获取
export function get (obj) {
  return request({
    url: '/platform/led/get',
    method: 'post',
    headers: {'Content-Type':'application/json;charset=utf8'},
    data:obj
  })
}
//设置
export function set (obj) {
  return request({
    url: '/platform/led/set',
    method: 'post',
    data: obj
  })
}
export function fetchList (query) {
  return request({
    url: '/platform/device/page',
    method: 'get',
    params: query
  })
}