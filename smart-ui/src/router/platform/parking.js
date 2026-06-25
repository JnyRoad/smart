import Layout from '@/page/index/'

export const parkingPresentParkingDetailRoute = {
    path: '/platform/parking/present_parking',
    component: Layout,
    redirect: '/platform/parking/present_parking/detail', //停车场管理,当前车辆和停车记录的详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/parking/present_parking/detail')
    }]
  }
