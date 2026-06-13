import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/mine',
  component: Layout,
  children: [{
    path: '',
    component: () => import('@views-mobile/pages/mine/index'),
    meta: { lable: '个人中心' }
  }, {
    path: 'detail',
    component: () => import('@views-mobile/pages/mine/detail'),
    props: true,
    meta: { lable: '个人信息' }
  }]
}
