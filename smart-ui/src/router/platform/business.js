import Layout from '@/page/index/'

export const businessVisitorDetailRoute = {
    path: '/platform/business/visitor',
    component: Layout,
    redirect: '/platform/business/visitor/detail', //业务设置，访客设置
    children: [{
      path: 'detail/:parkId',
      component: () =>
        import ('@/views/platform/business/visitor/detail')
    }]
  }

export const businessReleaseDetailRoute = {
    path: '/platform/business/release',
    component: Layout,
    redirect: '/platform/business/release/detail', //业务设置，物品放行设置
    children: [{
      path: 'detail',
      component: () =>
        import ('@/views/platform/business/release/detail')
    }]
  }

export const businessDormExitDetailRoute = {
    path: '/platform/business/dorm_exit',
    component: Layout,
    redirect: '/platform/business/dorm_exit/detail', //业务设置，物品放行设置
    children: [{
      path: 'detail',
      component: () =>
        import ('@/views/platform/business/dorm_exit/detail')
    }]
  }

export const businessRepairDetailRoute = {
    path: '/platform/business/repair',
    component: Layout,
    redirect: '/platform/business/repair/detail', //业务设置，园区报修设置
    children: [{
      path: 'detail',
      component: () =>
        import ('@/views/platform/business/repair/detail')
    }]
  }

export const businessSecurityAreaEditRoute = {
    path: '/platform/business/security_area',
    component: Layout,
    redirect: '/platform/business/security_area/edit', //业务设置，访客设置
    children: [{
      path: 'edit/:parkId',
      component: () =>
        import ('@/views/platform/business/security_area/edit')
    }, {
      path: 'auth_delete_log/index',
      component: () =>
        import ('@/views/platform/business/security_area/auth_delete_log/index')
    }]
  }

export const businessHydropowerAlarmRoute = {
    path: '/platform/business/hydropowerAlarm',
    component: Layout,
    redirect: '/platform/business/hydropower_alarm/index', //业务设置，水电告警设置
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/business/hydropower_alarm/index')
    }]
  }

export const businessDormitoryAlarmRoute = {
    path: '/platform/business/dormitoryAlarm',
    component: Layout,
    redirect: '/platform/business/dormitory_alarm/index', //业务设置，宿舍设置
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/business/dormitory_alarm/index')
    }]
  }

export const businessXcMsgSetRoute = {
    path: '/platform/business/xcMsgSet',
    component: Layout,
    redirect: '/platform/business/xc_msg_set/index', //业务设置，消息设置
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/business/xc_msg_set/index')
    }]
  }
