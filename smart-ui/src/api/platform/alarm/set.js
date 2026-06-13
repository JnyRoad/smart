
import request from '@/router/axios'

export function fetchDetail () {
    return request({
      url: '/platform/alarm/template/getAlarmTemplate',
      method: 'get'
    })
  }
