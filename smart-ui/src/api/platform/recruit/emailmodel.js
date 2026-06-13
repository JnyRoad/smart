
import request from '@/router/axios'

export function fetchList () {
  return request({
    url: '/platform/message/template/eamil/all',
    method: 'get'
  })
}
// export function getDetail (id) {
//   return request({
//     url: '/platform/message/template/eamil/'+id,
//     method: 'get'
//   })
// }
export function getDetail (tempCode) {
  return request({
    url: '/platform/message/template/getByCode/'+tempCode,
    method: 'get'
  })
}
//保存模板
export function editModel (obj) {
  return request({
    url: '/platform/message/template/update',
    method: 'post',
    data: obj
  })
}
//保存邮件接收人
export function editPerson (obj) {
  return request({
    url: '/platform/message/template/update/receive',
    method: 'post',
    data: obj
  })
}

export function getPark (id) {
  return request({
    url: `/platform/park/` + id,
    method: 'get'
  })
}
