import request from '@/router/axios'

//添加岗位
export function addObj (obj) {
  return request({
    url: '/platform/recruitment/addRecruitment',
    method: 'post',
    data: obj
  })
}

//添加岗位
export function localList (obj) {
  return request({
    url: '/platform/recruitmentsetting/list',
    method: 'get',
    params: obj
  })
}
