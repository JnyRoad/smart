import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/dormRepairs',
  component: Layout,
  children: [
    {
      path: '',
      component: () => import('@views-mobile/pages/dorm-repairs/index'),
      meta: { lable: '宿舍报修' }
    },
    {
      path: 'list',
      component: () => import('@views-mobile/pages/dorm-repairs/list'),
      props: true,
      meta: { lable: '宿舍报修列表' }
    },
    {
      path: 'detail',
      component: () => import('@views-mobile/pages/dorm-repairs/detail'),
      props: true,
      meta: { lable: '宿舍报修详情' }
    }
  ]
}
