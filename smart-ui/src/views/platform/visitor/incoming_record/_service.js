
import request from '@/router/axios'
export const xcIncomingRecordApi = {
  getList (query) {
    return request({
      url: '/platform/admittance/apply/page',
      method: 'get',
      params: query
    })
  },
  getDetail(id){
    return request({
      url: `/platform/admittance/apply/search/Detail/${id}`,
      method: 'get'
    })
  },
  getCauseEnum(id){
    return request({
      url: `/platform/admittance/apply/enum/cause`,
      method: 'get'
    })
  },
  reSend(data){
    return request({
      url: `/platform/admittance/apply/repeat/auth`,
      method: 'post',
      params: data
    })
  },
  getFactoryList(){
    return request({
      url: `/platform/admittance/area/type/security/factory/list`,
      method: 'get'
    })
  },
  getAreaType(data){
    return request({
      url: `/platform/admittance/apply/enum/factory/type`,
      method: 'get',
      params: data
    })
  },
}
