
import request from '@/router/axios'


export function fetchList(query) {
  return request({
    url: '/platform/staff/appeal/page',
    method: 'get',
    params: query
  })
}

export function getDetails (id) {
  return request({
    url: '/platform/staff/appeal/detail/' + id,
    method: 'get'
  })
}

export function saveReply (params) {
  return request({
    url: '/platform/staff/appeal/saveReply',
    method: 'post',
    data: params
  })
}


export function queryStaffList (badge) {
  return request({
    url: '/platform/staff/getStaffList/' + badge,
    method: 'get'
  })
}

export function changeStaff (data) {
  return request({
    url: '/platform/staff/appeal/changeApprove',
    method: 'post',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    transformRequest:[function(date){
      let ret = '';
      for(let it in data){
        ret += encodeURIComponent(it)+'='+encodeURIComponent(data[it])+'&'
      }
      return ret;
    }],
    data: data
  })
}
