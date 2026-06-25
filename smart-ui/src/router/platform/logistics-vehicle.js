import Layout from '@/page/index/'

export const logisticsVehicleReserveRecordDetailRoute = {
    path: '/platform/logistics_vehicle/reserve_record',
    component: Layout,
    redirect: '/platform/logistics_vehicle/reserve_record/detail', //车辆出入详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/logistics_vehicle/reserve_record/detail')
    }]
  }
