
import request from '@/router/axios'


export function getDetails (id) {
  return request({
    url: '/platform/snap/vehicle/searchVehicleAccessById/' + id,
    method: 'get',
    params: id
  })
}
