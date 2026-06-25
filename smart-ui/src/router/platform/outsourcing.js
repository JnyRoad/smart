import Layout from '@/page/index/'

export const outsourcingDetailRoute = {
    path: '/platform/outsourcing',
    component: Layout,
    redirect: '/platform/outsourcing/detail', //查看外包人员申请信息
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/outsourcing/detail/detail')
    }]
  }
