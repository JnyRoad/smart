import Layout from '@/page/index/'

export const businessManageArticleDetailRoute = {
    path: '/platform/business_manage/article',
    component: Layout,
    redirect: '/platform/business_manage/article/detail', //业务管理-物品放行
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/business_manage/article/detail')
    }]
  }
