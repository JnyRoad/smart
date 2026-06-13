import http from '@/util/http'
import { APPIP } from '@/conf'
import store from '@/store'
/**
 * 监听response
 */
http.setHttpInterceptors({
  request: function (config) {
    // if (config.carryAuth) {
    //   config.data = Object.assign({}, config.data, {
    //     enterpriseId: store.getters.storeActivePark.enterpriseId,
    //     parkId: store.getters.storeActivePark.parkId || ''
    //   })
    //   if (store.getters.storeActivePark.enterpriseId) {
    //     config.data = Object.assign({}, config.data, {
    //       enterpriseId: store.getters.storeActivePark.enterpriseId
    //     })
    //   }
    //   if (store.getters.storeActivePark.parkId) {
    //     config.data = Object.assign({}, config.data, {
    //       parkId: store.getters.storeActivePark.parkId
    //     })
    //   }
    // }
    return config
  },
  response: function (res, originalData) {
    const data = Object.assign({}, res.data)
    // debugger
    if (data.code === 401 && data.msg === 'invalid_token') {
      store.commit('SET_ACCESS_TOKEN', 'invalid_token')
      let url = encodeURIComponent(`${window.location.origin}/#/xuchang/login/wechat/code`)
      window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${APPIP}&redirect_uri=${url}&response_type=code&scope=snsapi_base&state=123#wechat_redirect`
    } else {
      data['$original'] = res
      data['$is_null'] = false
      if (res.data === null || res.data === '' || res.data === ' ' || res.data === undefined) {
        data['$is_null'] = true
      }
      return data
    }
  }
})
