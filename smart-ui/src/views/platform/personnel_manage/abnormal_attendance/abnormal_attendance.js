
import request from '@/router/axios'


export function fetchList(query, obj) {
  return request({
    url: '/platform/application/attendance/patch/statistics',
    method: 'post',
    data: obj,
    params: query
  })
}
