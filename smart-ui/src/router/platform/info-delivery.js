import Layout from '@/page/index/'

export const infoDeliveryInfoMngAddRoute = {
    path: '/platform/info_delivery/info_mng',
    component: Layout,
    redirect: '/platform/info_delivery/info_mng/add',
    children: [{
      path: 'add',
      name: '添加信息资源',
      component: () =>
        import ('@/views/platform/info_delivery/info_mng/add'),
    }]
  }

export const infoDeliveryInfoMngEditRoute = {
    path: '/platform/info_delivery/info_mng',
    component: Layout,
    redirect: '/platform/info_delivery/info_mng/edit',
    children: [{
      path: 'edit/:id',
      name: '编辑信息资源',
      component: () =>
        import ('@/views/platform/info_delivery/info_mng/edit'),
    }]
  }
