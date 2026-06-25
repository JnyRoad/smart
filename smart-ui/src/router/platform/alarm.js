import Layout from '@/page/index/'

export const alarmRecordDetailRoute = {
    path: '/platform/alarm/record',
    component: Layout,
    redirect: '/platform/alarm/record/detail', //安防记录，警报记录，详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/alarm/record/detail')
    }]
  }
