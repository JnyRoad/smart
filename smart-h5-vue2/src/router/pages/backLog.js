import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/backLog',
  component: Layout,
  children: [{
    path: '',
    component: () => import('@views-mobile/pages/backLog/index'),
    meta: { lable: '待办事项' }
  }, {
    path: 'goodReleaseLive',
    component: () => import('@views-mobile/pages/backLog/good-release-live/list'),
    props: true,
    meta: { lable: '物品放行(生活区)' }
  }, {
    path: 'goodReleaseLive/detail',
    component: () => import('@views-mobile/pages/backLog/good-release-live/detail'),
    props: true,
    meta: { lable: '物品放行(生活区)-详情' }
  }, {
    path: 'goodReleaseLive/result',
    component: () => import('@views-mobile/pages/backLog/good-release-live/result'),
    props: true,
    meta: { lable: '物品放行-结果' }
  }, {
    path: 'code',
    component: () => import('@views-mobile/pages/backLog/good-release-live/code'),
    props: true,
    meta: { lable: '二维码信息', isAuth: false }
  }, {
    path: 'goodReleaseWork',
    component: () => import('@views-mobile/pages/backLog/good-release-work/list'),
    props: true,
    meta: { lable: '物品放行(办公区)' }
  }, {
    path: 'goodReleaseWork/detail',
    component: () => import('@views-mobile/pages/backLog/good-release-work/detail'),
    props: true,
    meta: { lable: '物品放行(办公区)-详情' }
  }, {
    path: 'goodReleaseWork/result',
    component: () => import('@views-mobile/pages/backLog/good-release-work/result'),
    props: true,
    meta: { lable: '物品放行-结果' }
  }, {
    path: 'dormRepairs',
    component: () => import('@views-mobile/pages/backLog/dorm-repairs/list'),
    props: true,
    meta: { lable: '园区报修' }
  }, {
    path: 'dormRepairs/detail',
    component: () => import('@views-mobile/pages/backLog/dorm-repairs/detail'),
    props: true,
    meta: { lable: '园区报修-详情' }
  }, {
    path: 'dormExit',
    component: () => import('@views-mobile/pages/backLog/dorm-exit/list'),
    props: true,
    meta: { lable: '退宿申请' }
  }, {
    path: 'dormExit/detail',
    component: () => import('@views-mobile/pages/backLog/dorm-exit/detail'),
    props: true,
    meta: { lable: '退宿申请-详情' }
  }]
}
