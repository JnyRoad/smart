import Layout from '@/page/index/'

export const securityAreaSecurityGuardApplyDetailRoute = {
    path: '/platform/security_area/securityGuardApply',
    component: Layout,
    redirect: '/platform/security_area/securityGuardApply', //保密区门禁申请
    children: [{
      path: 'detail/:id', //-详情
      component: () =>
        import ('@/views/platform/security_area/xc_guard_apply/detail')
    },{
      path: 'add', //-添加
      component: () =>
        import ('@/views/platform/security_area/xc_guard_apply/add')
    }]
  }

export const securityAreaSecurityProjectMngStaffRoute = {
    path: '/platform/security_area/securityProjectMng',
    component: Layout,
    redirect: '/platform/security_area/securityProjectMng', //保密项目维护
    children: [{
      path: 'staff/:id', //-授权人员
      component: () =>
        import ('@/views/platform/security_area/xc_project_mng/staff')
    }]
  }
