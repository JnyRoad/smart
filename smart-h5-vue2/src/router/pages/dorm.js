import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/dorm',
  component: Layout,
  children: [{
    path: '',
    component: () => import('@views-mobile/pages/dorm/index'),
    meta: { lable: '我的宿舍' }
  }, {
    path: 'waterElec',
    component: () => import('@views-mobile/pages/dorm/water-elec'),
    meta: { lable: '水电扣费明细' }
  }, {
    path: 'lock',
    component: () => import('@views-mobile/pages/lock/index'),
    meta: { lable: '门锁动态码' }
  }, {
    path: 'getCode',
    component: () => import('@views-mobile/pages/lock/get-code'),
    meta: { lable: '获取门锁动态码' }
  }]
}
