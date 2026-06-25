import Layout from '@/page/index/'

export const parkServiceFeedBackDetailRoute = {
    path: '/platform/park_service/feed_back',
    component: Layout,
    redirect: '/platform/park_service/feed_back/detail', //园区服务，员工反馈详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/park_service/feed_back/detail')
    }]
  }

export const parkServicePaperDetailRoute = {
    path: '/platform/park_service/paper',
    component: Layout,
    redirect: '/platform/park_service/paper/detail', //园区服务，调查表详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/park_service/paper/detail')
    }]
  }

export const parkServicePaperAddRoute = {
    path: '/platform/park_service/paper',
    component: Layout,
    redirect: '/platform/park_service/paper/add', //园区服务，调查表添加
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/park_service/paper/add')
    }]
  }
