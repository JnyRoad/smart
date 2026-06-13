
import request from '@/router/axios'

//批量导入
export function importStaff (obj) {
  return request({
    url: '/platform/staff/register',
    method: 'post',
    data: obj
  })
}
