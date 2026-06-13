import Layout from '@/page/index/'
export default [{
        path: '/login',
        name: '登录页',
        component: () =>
            import ('@/page/login/index'),
        meta: {
            keepAlive: true,
            isTab: false,
            isAuth: false
        }
    },
    {
        path: '/pad/login',
        name: '登录页',
        component: () =>
            import ('@/page/login/index'),
        meta: {
            keepAlive: true,
            isTab: false,
            isAuth: false,
            isPad: true
        }
    },
    {
        path: '/lock',
        name: '锁屏页',
        component: () =>
            import ('@/page/lock/index'),
        meta: {
            keepAlive: true,
            isTab: false,
            isAuth: false
        }
    },
    {
        path: '/404',
        component: () =>
            import ('@/components/error-page/404'),
        name: '404',
        meta: {
            keepAlive: true,
            isTab: false,
            isAuth: false
        }

    },
    {
        path: '/403',
        component: () =>
            import ('@/components/error-page/403'),
        name: '403',
        meta: {
            keepAlive: true,
            isTab: false,
            isAuth: false
        }
    },
    {
        path: '/500',
        component: () =>
            import ('@/components/error-page/500'),
        name: '500',
        meta: {
            keepAlive: true,
            isTab: false,
            isAuth: false
        }
    },
    {
        path: '/',
        name: '主页',
        redirect: '/home'
    },
    {
        path: '/myiframe',
        component: Layout,
        redirect: '/myiframe',
        children: [{
            path: ":routerPath",
            name: 'iframe',
            component: () =>
                import ('@/components/iframe/main'),
            props: true
        }]
    },
    {
        path: '*',
        redirect: '/404',
    },
    {
      path: '/authredirect',
      name: '授权页',
      component: () =>
      import ('@/page/login/authredirect'),
      meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
      }
    },
]
