import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/goodReleaseWork',
  component: Layout,
  children: [{
    path: '',
    component: () => import('@views-mobile/pages/good-release-work/index'),
    meta: { lable: '物品放行（办公区）' }
  }, {
    path: 'list',
    component: () => import('@views-mobile/pages/good-release-work/list'),
    props: true,
    meta: { lable: '物品放行记录（办公区）' }
  }, {
    path: 'detail',
    component: () => import('@views-mobile/pages/good-release-work/detail'),
    props: true,
    meta: { lable: '物品放行详情（办公区）' }
  }, {
    path: 'addPerson',
    component: () => import('@views-mobile/pages/good-release-work/add-person'),
    props: true,
    meta: { lable: '添加人员' }
  }, {
    path: 'addPersonList',
    component: () => import('@views-mobile/pages/good-release-work/add-person-list'),
    props: true,
    meta: { lable: '添加人员-列表' }
  }, {
    path: 'addGoods',
    component: () => import('@views-mobile/pages/good-release-work/add-goods'),
    props: true,
    meta: { lable: '添加物品' }
  }, {
    path: 'addGoodsList',
    component: () => import('@views-mobile/pages/good-release-work/add-goods-list'),
    props: true,
    meta: { lable: '添加物品-列表' }
  }, {
    path: 'detailPerson',
    component: () => import('@views-mobile/pages/good-release-work/detail-person'),
    props: true,
    meta: { lable: '人员详情' }
  }, {
    path: 'detailPersonList',
    component: () => import('@views-mobile/pages/good-release-work/detail-person-list'),
    props: true,
    meta: { lable: '人员详情-列表' }
  }, {
    path: 'detailGoods',
    component: () => import('@views-mobile/pages/good-release-work/detail-goods'),
    props: true,
    meta: { lable: '物品详情' }
  }, {
    path: 'detailGoodsList',
    component: () => import('@views-mobile/pages/good-release-work/detail-goods-list'),
    props: true,
    meta: { lable: '物品详情-列表' }
  } ]
}
