import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/help',
  component: Layout,
  children: [{
    path: '',
    component: () => import('@views-mobile/pages/help/index'),
    meta: { lable: '帮助中心' }
  },
  {
    path: 'detail',
    component: () => import('@views-mobile/pages/help/detail'),
    meta: { lable: '帮助文档详情' }
  }
  ]
}
