import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/goodReleaseLive',
  component: Layout,
  children: [{
    path: '',
    component: () => import('@views-mobile/pages/good-release-live/index'),
    meta: { lable: '物品放行（生活区）' }
  }, {
    path: 'list',
    component: () => import('@views-mobile/pages/good-release-live/list'),
    props: true,
    meta: { lable: '物品放行记录（生活区）' }
  }, {
    path: 'detail',
    component: () => import('@views-mobile/pages/good-release-live/detail'),
    props: true,
    meta: { lable: '物品放行详情（生活区）' }
  }]
}
