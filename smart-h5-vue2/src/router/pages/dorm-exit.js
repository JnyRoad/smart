import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/dormExit',
  component: Layout,
  children: [
    {
      path: '',
      component: () => import('@views-mobile/pages/dorm-exit/index'),
      meta: { lable: '退宿申请' }
    },
    {
      path: 'list',
      component: () => import('@views-mobile/pages/dorm-exit/list'),
      props: true,
      meta: { lable: '退宿申请列表' }
    },
    {
      path: 'detail',
      component: () => import('@views-mobile/pages/dorm-exit/detail'),
      props: true,
      meta: { lable: '退宿申请详情' }
    }
  ]
}
