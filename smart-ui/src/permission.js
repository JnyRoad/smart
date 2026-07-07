/**
 * 全站权限配置
 *
 */
import router from './router/router'
import store from '@/store'
import {getStore} from '@/util/store'
import {validatenull} from '@/util/validate'
import {reportCaughtError} from '@/error-reporter'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
NProgress.configure({showSpinner: false})

// 导航失败（守卫抛错、异步组件/chunk 加载失败等）时 afterEach 不会触发，
// 必须在这里收尾进度条并上报，否则页面表现为无限加载且无任何痕迹
router.onError(error => {
  NProgress.done()
  reportCaughtError(error, '路由导航失败')
})
const lockPage = store.getters.website.lockPage // 锁屏页
router.beforeEach((to, from, next) => {
  // 缓冲设置
  if (to.meta.keepAlive === true && store.state.tags.tagList.some(ele => {
    return ele.value === to.fullPath
  })) {
    to.meta.$keepAlive = true
  } else {
    NProgress.start()
    if (to.meta.keepAlive === true && validatenull(to.meta.$keepAlive)) {
      to.meta.$keepAlive = true
    } else {
      to.meta.$keepAlive = false
    }
  }
  const meta = to.meta || {}
  if (store.getters.access_token) {
    if (store.getters.isLock && to.path != lockPage) {
      next({path: lockPage})
    } else if (to.path === '/login') {
      next({path: '/'})
    } else {
      if (store.getters.roles.length === 0) {
        store.dispatch('GetUserInfo').then(() => {
          next({...to, replace: true})
        }).catch(() => {
          store.dispatch('FedLogOut').then(() => {
            next({path: '/login'})
          })
        })
      } else {
        // The interceptor only writes this key on the weak-password 401 (CR-C14-002),
        // so a missing key must mean "strong" or fresh sessions would be logged out.
        let isStrongPwd = localStorage.getItem("isStrongPwd") !== "false";
        // 如果不是强密码，且不是跳转到修改密码的路由，都退出系统
        if (!isStrongPwd && !to.query.loginTo) {
          store.dispatch('FedLogOut').then(() => {
            next({ path: '/login' })
          })
        }
        const value = to.query.src || to.fullPath
        const label = to.query.name || to.name
        if (meta.isTab !== false && !validatenull(value) && !validatenull(label)) {
          store.commit('ADD_TAG', {
            label: label,
            value: value,
            params: to.params,
            query: to.query,
            group: router.$avueRouter.group || []
          })
        }
        next()
      }
    }
  } else {
    if (meta.isAuth === false) {
      next()
    } else {
      next('/login')
    }
  }
})

router.afterEach(() => {
  NProgress.done()
  const title = store.getters.tag.label
  router.$avueRouter.setTitle(title)
})
