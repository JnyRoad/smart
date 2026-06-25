import Layout from '@/page/index/'

export const equipmentHydropowerRoute = {
    path: '/platform/equipment/hydropower',
    component: Layout,
    redirect: '/platform/equipment/hydropower/index', //设备管理，水电数据看板
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/equipment/hydropower/index')
    }]
  }
