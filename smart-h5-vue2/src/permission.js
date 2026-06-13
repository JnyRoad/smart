/**
 * 全站权限配置
 * 与后端强耦合
 */
import router from '@/router/index'

/**
 * 全局前置守卫
 */
router.beforeEach((to, from, next) => {
  // console.log('to---',to)
  // console.log('from---',from)
  // if(!meta.isAuth){

  // }else{
  //   // 登录
  //   if (store.getters.access_token) {
  //     if (to.path === '/xuchang/login/wechat' || to.path === '/xuchang/login/wechat/code') {
  //       next({ path: '/' })
  //       return
  //     }
  //   }else{
  //     // 未登录
  //     if (to.path !== '/xuchang/login/wechat' && to.path !== '/xuchang/login/wechat/code') {
  //       next({ path: '/xuchang/login/wechat' })
  //       return
  //     }
  //   }
  // }
  next()
})
/**
 * 动态设置浏览器标题
 */
router.afterEach((to) => {
  document.title = to.meta.label || to.meta.lable || ''
})
