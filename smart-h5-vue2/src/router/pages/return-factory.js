import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/returnFactory',
  component: Layout,
  children: [{
    path: '',
    component: () => import('@views-mobile/pages/return-factory/list'),
    meta: { lable: '返厂确认' }
  }, {
    path: 'detail',
    component: () => import('@views-mobile/pages/return-factory/detail'),
    props: true,
    meta: { lable: '返厂确认详情' }
  }]
}
