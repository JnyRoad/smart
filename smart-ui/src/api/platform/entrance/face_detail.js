
import request from '@/router/axios'

export function getDetails (id) {
  return request({
    url: '/platform/snap/person/searchSnapPersonDetail/' + id,
    method: 'get',
    params: id
  })
}
