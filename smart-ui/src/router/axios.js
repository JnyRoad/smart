import { serialize } from '@/util/util'
import { getStore } from '../util/store'
import NProgress from 'nprogress' // progress bar
import errorCode from '@/const/errorCode'
import router from "@/router/router"
import { Message } from 'element-ui'
import 'nprogress/nprogress.css'
import store from "@/store"; // progress bar style
// 30s global timeout; endpoints that legitimately run longer (upload/download/export)
// must set their own per-request timeout instead of inflating the global one.
axios.defaults.timeout = 30000
// 返回其他状态吗
axios.defaults.validateStatus = function (status) {
  return status >= 200 && status <= 500 // 默认的
}
// 跨域请求，允许保存cookie
axios.defaults.withCredentials = true
// NProgress Configuration
NProgress.configure({
  showSpinner: false
})

// HTTPrequest拦截
axios.interceptors.request.use(config => {
  NProgress.start() // start progress bar
  const isToken = (config.headers || {}).isToken === false
  let token = store.getters.access_token
  if (token && !isToken) {
    config.headers['Authorization'] = 'Bearer ' + token// token
  }
  // headers中配置serialize为true开启序列化
  if (config.method === 'post' && config.headers.serialize) {
    config.data = serialize(config.data)
    delete config.data.serialize
  }
  return config
}, error => {
  return Promise.reject(error)
})


// HTTPresponse拦截
axios.interceptors.response.use(res => {
  NProgress.done()

  const status = Number(res.status) || 200
  const message = res.data.msg || errorCode[status] || errorCode['default']
  if (status === 401) {
    if(res.data.msg.indexOf("isStrongPwd")!=-1&&res.data.msg.indexOf("false")!=-1){
      localStorage.setItem("isStrongPwd", false);
      router.push({path: '/editPwd', query:{loginTo: true}})
    }else{
      store.dispatch('FedLogOut').then(() => {
        router.push({ path: '/login' })
      })
    }
    return Promise.reject(new Error(message))
  }

  if (status !== 200 || res.data.code === 1) {
    Message({
      message: message,
      type: 'error'
    })
    // CR-C14-002 灰度开关：打开后业务失败走 reject，调用方 .then 不再被错误数据穿透。
    // 开启：localStorage.setItem('SMART_UI_STRICT_REJECT', 'true') 后刷新页面；
    // 回滚：removeItem 或设为其他值。未捕获的 reject 由 src/error.js 的
    // unhandledrejection 监听记入 vuex logs，不弹窗。全量验证后默认行为再切为 reject。
    if (localStorage.getItem('SMART_UI_STRICT_REJECT') === 'true') {
      return Promise.reject(new Error(message))
    }
    return res
  }

  return res
}, error => {
  NProgress.done()
  return Promise.reject(new Error(error))
})

export default axios
