
import request from '@/router/axios'

export function getInfoApi (code) {
  return request({
    url: `/platform/visitor/searchVisitorByCode/${code}`,
    method: 'get'
  })
}
export function getInfoApiNew (code) {
  return request({
    url: `/platform/admittance/apply/searchVisitorByCode/${code}`,
    method: 'get'
  })
}

export function getInfoApiCard (idCard) {
  return request({
    url: `/platform/admittance/apply/search/byCard/new/${idCard}`,
    method: 'get'
  })
}

export function delSmsCode (visitorId) {
  return request({
    url: `/platform/visitor/delSmsCode/${visitorId}`,
    method: 'get'
  })
}

export function getImage (visitorId) {
  return request({
    url: `/platform/admittance/apply/smbPutPhoto/${visitorId}`,
    method: 'get'
  })
}

export function getAreaType(data) {
  return request({
    url: `/platform/admittance/apply/enum/factory/type`,
    method: 'get',
    params: data
  })
}
