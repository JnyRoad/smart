import Vue from 'vue'
import App from './App'

import store from './store'
import ytLoading from '@/components/yt-loading/index.vue' // loading动画
import ytHint from '@/config/handling.js' // 弹窗类封装
import ytSystem from '@/config/system.js' // 系统类封装 例如震动
import hideModel from '@/mixins/hideModel.vue' // 特定条件下隐藏输入框和Loading动画
import restart from '@/mixins/restart.js' // 检查网络状态 对应处理
// 注册加载动画全局组件
Vue.component('yt-loading', ytLoading)
Vue.mixin(restart)
Vue.mixin(hideModel)
// 是否显示加载 调用 store 中的 mutations 方法

function loading(xshow) {
	store.commit('ytLoading',xshow)
}
Vue.prototype.$store = store
// 将这个提示弹窗挂载到vue上面
Vue.prototype.$ytHint = ytHint
// 全局使用 system 包括检查网络状态也操作错误震动提示
Vue.prototype.$ytSystem = ytSystem
// 全局loading
Vue.prototype.$loading = loading

Vue.config.productionTip = false

App.mpType = 'app'

const app = new Vue({
    store,
    ...App
})
app.$mount()