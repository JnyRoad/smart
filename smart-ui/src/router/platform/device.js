import Layout from '@/page/index/'

export const deviceCarDistributionRecordRoute = {
    path: '/platform/device/carDistributionRecord',
    component: Layout,
    redirect: '/platform/device/carDistributionRecord/index', //设备管理，车辆下发记录
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/device/carDistributionRecord/index')
    }]
  }

export const deviceFaceDistributionRecordRoute = {
    path: '/platform/device/faceDistributionRecord',
    component: Layout,
    redirect: '/platform/device/faceDistributionRecord/index', //设备管理，人脸下发记录
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/device/faceDistributionRecord/index')
    }]
  }

export const deviceLockDoorRecordRoute = {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/xc_lock_door', //设备管理, 门锁管理下发记录
    children: [{
      path: 'lock_door/record/:id',
      component: () =>
        import ('@/views/platform/device/xc_lock_door/components/record')
    }]
  }

export const deviceWaterManageRecordRoute = {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/water_manage', //设备管理, 水表历史度数
    children: [{
      path: 'waterManage/record/:id',
      component: () =>
        import ('@/views/platform/device/water_manage/components/record')
    }]
  }

export const deviceWaterManageLogRoute = {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/water_manage', //设备管理, 水表操作记录
    children: [{
      path: 'waterManage/log/:id',
      component: () =>
        import ('@/views/platform/device/water_manage/components/logs')
    }]
  }

export const deviceValveManageLogRoute = {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/valve_manage', //设备管理, 水表操作记录
    children: [{
      path: 'valveManage/log/:id',
      component: () =>
        import ('@/views/platform/device/valve_manage/logs')
    }]
  }

export const deviceElectricManageRecordRoute = {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/electric_manage', //设备管理, 电表历史度数
    children: [{
      path: 'electricManage/record/:id',
      component: () =>
        import ('@/views/platform/device/electric_manage/components/record')
    }]
  }

export const deviceElectricManageLogRoute = {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/electric_manage', //设备管理, 电表操作记录
    children: [{
      path: 'electricManage/log/:id',
      component: () =>
        import ('@/views/platform/device/electric_manage/components/logs')
    }]
  }

export const devicePersonListRoute = {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/person_list', //设备管理, 门禁和闸机的通关人员
    children: [{
      path: 'person_list/:id',
      component: () =>
        import ('@/views/platform/device/person_list')
    }]
  }

export const deviceGateLimitRoute = {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/gate_limit', //设备管理, 闸机管理，更新设备权限
    children: [{
      path: 'gate_limit',
      component: () =>
        import ('@/views/platform/device/gate_limit')
    }]
  }

export const deviceAutomaticLimitRoute = {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/automatic_limit', //设备管理, 道闸管理，更新设备权限
    children: [{
      path: 'automatic_limit',
      component: () =>
        import ('@/views/platform/device/automatic_limit')
    }]
  }

export const deviceLedInfoRoute = {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/ledInfo', //设备管理, 道闸管理，编辑显示屏信息
    children: [{
      path: 'ledInfo/:id',
      component: () =>
        import ('@/views/platform/device/ledInfo')
    }]
  }

export const deviceVehicleListRoute = {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/vehicle_list', //设备管理, 道闸的通关车辆
    children: [{
      path: 'vehicle_list/:id',
      component: () =>
        import ('@/views/platform/device/vehicle_list')
    }]
  }
