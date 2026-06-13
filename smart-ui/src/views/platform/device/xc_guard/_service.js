
import request from '@/router/axios'
export const xcGuardApi = {
  setTagApi (data) {
    return request({
      url: '/platform/device/tag/set',
      method: 'post',
      data: data
    })
  },
  getTagList () {
    return request({
      url: '/platform/device/tag/list',
      method: 'get'
    })
  },
  authClear (deviceId) {
    return request({
      url:  `/platform/device/auth/clear/${deviceId}`,
      method: 'post'
    })
  },
  equipReissue(deviceId){
    return request({
      url:  `/platform/device/auth/repeat/${deviceId}`,
      method: 'get'
    })
  }
}
