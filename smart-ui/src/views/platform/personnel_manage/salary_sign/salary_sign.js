
import request from '@/router/axios'



export function fetchList (query) {
  return request({
    url: '/platform/wage/sign/page',
    method: 'get',
    params: query,
    timeout: 3000*60*5
  })
}
//详情
export function getById (id) {
  return request({
    url: '/platform/wage/sign/'+id,
    method: 'get',
    data: id
  })
}

//发送短信提醒
export function sendMsg (obj) {
  return request({
    url: '/platform/wage/sign/msg',
    method: 'post',
    data: obj
  })
}

//发送短信条数
export function sendMsgNum (obj) {
  return request({
    url: '/platform/wage/sign/msg/count  ',
    method: 'post',
    data: obj
  })
}
