import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/home',
  component: Layout,
  children: [{
    path: '',
    component: () => import('@views-mobile/pages/home/index'),
    meta: { lable: '首页' }
  }, {
    path: 'bbs/list',
    component: () => import('@views-mobile/pages/home/bbs/list'),
    props: true,
    meta: { lable: '公告列表' }
  }, {
    path: 'bbs/detail',
    component: () => import('@views-mobile/pages/home/bbs/detail'),
    props: true,
    meta: { lable: '公告详情' }
  }]
}
