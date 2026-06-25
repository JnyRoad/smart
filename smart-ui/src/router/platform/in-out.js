import Layout from '@/page/index/'

export const inOutReturnFactoryDetailRoute = {
    path: '/platform/in_out/return_factory',
    component: Layout,
    redirect: '/platform/in_out/return_factory/detail', //宿舍报修，报修详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/in_out/return_factory/detail')
    }]
  }
