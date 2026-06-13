import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/visitor',
  component: Layout,
  children: [{
    path: '',
    component: () => import('@views-mobile/pages/visitor/index'),
    meta: { lable: '入厂申请', isAuth: false }
  }, {
    path: 'tel',
    component: () => import('@views-mobile/pages/visitor/tel'),
    props: true,
    meta: { lable: '手机验证', isAuth: false }
  }, {
    path: 'telHefei',
    component: () => import('@views-mobile/pages/visitor/telHefei'),
    props: true,
    meta: { lable: '手机验证', isAuth: false }
  }, {
    path: 'visitorInfo',
    component: () => import('@views-mobile/pages/visitor/visitorInfo'),
    props: true,
    meta: { lable: '访客信息', isAuth: false }
  }, {
    path: 'indexHefei',
    component: () => import('@views-mobile/pages/visitor/indexHefei'),
    props: true,
    meta: { lable: '入厂申请', isAuth: false }
  }, {
    path: 'visitorInfoHefei',
    component: () => import('@views-mobile/pages/visitor/visitorInfoHefei'),
    props: true,
    meta: { lable: '访客信息', isAuth: false }
  }, {
    path: 'addPersonList',
    component: () => import('@views-mobile/pages/visitor/add-person-list'),
    props: true,
    meta: { lable: '随行人员列表', isAuth: false }
  }, {
    path: 'addPerson',
    component: () => import('@views-mobile/pages/visitor/add-person'),
    props: true,
    meta: { lable: '添加随行人员', isAuth: false }
  }, {
    path: 'addPersonHefei',
    component: () => import('@views-mobile/pages/visitor/add-person-hefei'),
    props: true,
    meta: { lable: '添加随行人员', isAuth: false }
  }, {
    path: 'addCarList',
    component: () => import('@views-mobile/pages/visitor/add-car-list'),
    props: true,
    meta: { lable: '车辆列表', isAuth: false }
  }, {
    path: 'addCar',
    component: () => import('@views-mobile/pages/visitor/add-car'),
    props: true,
    meta: { lable: '添加车辆', isAuth: false }
  }, {
    path: 'result',
    component: () => import('@views-mobile/pages/visitor/result'),
    props: true,
    meta: { lable: '提交成功', isAuth: false }
  }, {
    path: 'resultTruck',
    component: () => import('@views-mobile/pages/visitor/resultTruck'),
    props: true,
    meta: { lable: '提交成功', isAuth: false }
  }, {
    path: 'code',
    component: () => import('@views-mobile/pages/visitor/code'),
    props: true,
    meta: { lable: '二维码信息', isAuth: false }
  },
  {
    path: 'addAreaType',
    component: () => import('@views-mobile/pages/visitor/addAreaType'),
    props: true,
    meta: { lable: '添加授权进入区域', isAuth: false }
  },
  {
    path: 'truck',
    component: () => import('@views-mobile/pages/visitor/truck'),
    props: true,
    meta: { lable: '货车预约', isAuth: false }
  }
  // {
  //   path: 'detail',
  //   component: () => import('@views-mobile/pages/good-release-live/detail'),
  //   props: true,
  //   meta: { lable: '物品放行详情（生活区）', isAuth: false  }
  // }
  ]
}
