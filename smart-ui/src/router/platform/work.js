import Layout from '@/page/index/'

export const workAskLeaveDetailRoute = {
    path: '/platform/work/askLeave',
    component: Layout,
    redirect: '/platform/work/askLeave/detail', //业务监控，请假详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/askLeave/detail')
    }]
  }

export const workOverTimeDetailRoute = {
    path: '/platform/work/overTime',
    component: Layout,
    redirect: '/platform/work/overTime/detail', //业务监控，加班详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/overTime/detail')
    }]
  }

export const workAttendanceDetailRoute = {
    path: '/platform/work/attendance',
    component: Layout,
    redirect: '/platform/work/attendance/detail', //业务监控，补卡详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/attendance/detail')
    }]
  }

export const workLeaveApplicationDetailRoute = {
    path: '/platform/work/leaveApplication',
    component: Layout,
    redirect: '/platform/work/leaveApplication/detail', //业务监控，补卡详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/leaveApplication/detail')
    }]
  }

export const workToStaffDetailRoute = {
    path: '/platform/work/toStaff',
    component: Layout,
    redirect: '/platform/work/toStaff/detail', //业务监控，入职记录详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/toStaff/detail')
    }]
  }

export const workOutDormitoryDetailRoute = {
    path: '/platform/work/outDormitory',
    component: Layout,
    redirect: '/platform/work/outDormitory/detail', //业务监控，外宿补贴详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/outDormitory/detail')
    }]
  }

export const workBreakOffDetailRoute = {
    path: '/platform/work/breakOff',
    component: Layout,
    redirect: '/platform/work/breakOff/detail', //业务监控，调休记录详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/breakOff/detail')
    }]
  }

export const workCheckedOutDetailRoute = {
    path: '/platform/work/checkedOut',
    component: Layout,
    redirect: '/platform/work/checkedOut/detail', //业务监控，调休记录详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/checkedOut/detail')
    }]
  }
