import Layout from '@/page/index/'

export const entranceFaceDetailRoute = {
    path: '/platform/entrance/face',
    component: Layout,
    name: 'faceDetail',
    redirect: '/platform/entrance/face/detail', //人员出入详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/entrance/face/detail')
    }]
  }

export const entranceVehicleDetailRoute = {
    path: '/platform/entrance/vehicle',
    component: Layout,
    redirect: '/platform/entrance/vehicle/detail', //车辆出入详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/entrance/vehicle/detail')
    }]
  }

export const entranceArticleDetailRoute = {
    path: '/platform/entrance/article',
    component: Layout,
    redirect: '/platform/entrance/article/detail', //物品放行详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/entrance/article/detail')
    }]
  }
