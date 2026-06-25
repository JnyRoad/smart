import Layout from '@/page/index/'

export const personnelManageStaffAppealDetailRoute = {
    path: '/platform/personnel_manage/staff_appeal',
    component: Layout,
    redirect: '/platform/personnel_manage/staff_appeal/detail', //员工申诉，申诉详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/personnel_manage/staff_appeal/detail')
    }]
  }

export const personnelManageSalarySignDetailRoute = {
    path: '/platform/personnel_manage/salary_sign',
    component: Layout,
    redirect: '/platform/personnel_manage/salary_sign/detail', //人资行政管理，工资签收管理
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/personnel_manage/salary_sign/detail')
    }]
  }

export const personnelManageAttendanceSignDetailRoute = {
    path: '/platform/personnel_manage/attendance_sign',
    component: Layout,
    redirect: '/platform/personnel_manage/attendance_sign/detail', //人资行政管理，考勤汇总确认管理
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/personnel_manage/attendance_sign/detail')
    }]
  }
