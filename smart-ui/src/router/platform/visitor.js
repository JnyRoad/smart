import Layout from '@/page/index/'

export const visitorQrCodeIndexRoute = {
    path: '/platform/visitor/qrCode',
    component: Layout,
    redirect: '/index',
    children: [{
      path: 'index',
      name: '访客码',
      component: () =>
        import ('@/views/platform/visitor/qrCode/index'),
    }]
  }

export const visitorVisitorRecordDetailRoute = {
    path: '/platform/visitor/visitor_record',
    component: Layout,
    redirect: '/platform/visitor/visitor_record/detail', //访客预约，预约记录详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/visitor/visitor_record/detail')
    }]
  }

export const visitorIncomingRecordDetailRoute = {
    path: '/platform/visitor/incoming_record',
    component: Layout,
    redirect: '/platform/visitor/incoming_record/detail', //入厂申请记录，入厂申请详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/visitor/incoming_record/detail')
    }]
  }
