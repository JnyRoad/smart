import Vue from 'vue'
import store from './store'
import { reportCaughtError } from './error-reporter'

Vue.config.errorHandler = function (err, vm, info) {
  Vue.nextTick(() => {
    store.commit('ADD_LOGS', {
      type: 'error',
      message: err.message,
      stack: err.stack,
      info
    })
  })
}

// CR-C14-002 配套：SMART_UI_STRICT_REJECT 开关打开后，没写 catch 的调用方会产生
// unhandledrejection。这里只记录（console + vuex logs），不弹窗，避免灰度期间打扰用户。
window.addEventListener('unhandledrejection', event => {
  reportCaughtError(event.reason, 'unhandledrejection')
})
