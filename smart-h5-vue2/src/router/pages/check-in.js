import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/checkIn',
  component: Layout,
  children: [
    {
      path: '',
      component: () => import('@views-mobile/pages/check-in/index'),
      meta: { lable: '宿舍申请' }
    },
    {
      path: 'selectRoom',
      component: () => import('@views-mobile/pages/check-in/select-room'),
      props: true,
      meta: { lable: '手动选择房间' }
    },
    {
      path: 'detail',
      component: () => import('@views-mobile/pages/check-in/list'),
      props: true,
      meta: { lable: '宿舍申请详情' }
    }
  ]
}
