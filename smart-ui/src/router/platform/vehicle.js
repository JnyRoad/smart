import Layout from '@/page/index/'

export const vehicleStaffVehicleDetailRoute = {
    path: '/platform/vehicle/staff_vehicle',
    component: Layout,
    redirect: '/platform/vehicle/staff_vehicle/detail', //车辆管理，员工车辆详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/vehicle/staff_vehicle/detail')
    }]
  }

export const vehicleStaffVehicleAddRoute = {
    path: '/platform/vehicle/staff_vehicle',
    component: Layout,
    redirect: '/platform/vehicle/staff_vehicle/add', //车辆管理，员工车辆，添加车辆
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/vehicle/staff_vehicle/add')
    }]
  }

export const vehicleEntryExamineDetailRoute = {
    path: '/platform/vehicle/entry_examine',
    component: Layout,
    redirect: '/platform/vehicle/entry_examine/detail', //车辆管理，入园审批详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/vehicle/entry_examine/detail')
    }]
  }

export const vehicleCompanyVehicleAddRoute = {
    path: '/platform/vehicle/company_vehicle',
    component: Layout,
    redirect: '/platform/vehicle/company_vehicle/add', //车辆管理，公司车辆，添加公车
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/vehicle/company_vehicle/add')
    }]
  }

export const vehicleNonStaffVehicleDetailRoute = {
    path: '/platform/vehicle/non_staff_vehicle',
    component: Layout,
    redirect: '/platform/vehicle/non_staff_vehicle/detail', //车辆管理，非员工车辆详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/vehicle/non_staff_vehicle/detail')
    }]
  }

export const vehicleNonStaffVehicleAddRoute = {
    path: '/platform/vehicle/non_staff_vehicle',
    component: Layout,
    redirect: '/platform/vehicle/non_staff_vehicle/add', //车辆管理，非员工车辆，添加车辆
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/vehicle/non_staff_vehicle/add')
    }]
  }

export const vehicleVehicleMaintainRoute = {
    path: '/platform/vehicle/vehicle_maintain',
    component: Layout,
    redirect: '/platform/vehicle/vehicle_maintain/add', //车辆维护
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/vehicle/vehicle_maintain/index')
    },{
      path: 'add',
      component: () =>
        import ('@/views/platform/vehicle/vehicle_maintain/add')
    }]
  }
