
import request from '@/router/axios'

export function queryRoomStatementDetail (mrId) {
  return request({
    url: '/platform/dormitory/meterread/statement/' + mrId,
    method: 'get'
  })
}
