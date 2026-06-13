/* eslint-disable */

import Vue from 'vue'
import device from '@/util/device'
import store from './store'

/* IFTRUE_H5 */
import router from './router/index'
/* FITRUE_H5 */

/* IFTRUE_FACE */
import router from './router/face'
/* FITRUE_FACE */

import './services/index'

// 加载chunks
import './chunks/@tce'
import './chunks/components'
import './helper/chunk'
// import './error'
import './permission'

import './chunks/cube-ui'
import App from './App'
Vue.prototype.$device = device
Vue.mixin({
  data() {
    return {
      isMobile: device.mobile()
    }
  }
})
new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
