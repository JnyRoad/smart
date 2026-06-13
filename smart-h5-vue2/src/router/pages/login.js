import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/login',
  component: Layout,
  children: [
    {
      path: '',
      component: () => import('@views-mobile/pages/login/index'),
      meta: { lable: '登录', isAuth: false }
    },
    {
      path: 'wechat',
      component: () => import('@views-mobile/pages/login/login_wechat'),
      props: true,
      meta: { lable: '', isAuth: false }
    }, {
      path: 'wechat/code',
      component: () => import('@views-mobile/pages/login/code_wechat'),
      props: true,
      meta: { lable: '', isAuth: false }
    },
    {
      path: 'logon_badge',
      component: () => import('@views-mobile/pages/login/logon_badge'),
      props: true,
      meta: { lable: '', isAuth: false }
    }
  ]
}
