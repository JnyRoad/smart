import Vue from 'vue'

Vue.config.errorHandler = function (err, vm, info) {
  Vue.nextTick(() => {
    if (typeof window !== 'undefined') {
      window.__SMART_LAST_VUE_ERROR__ = {
        message: err && err.message,
        stack: err && err.stack,
        info
      }
    }
  })
}
