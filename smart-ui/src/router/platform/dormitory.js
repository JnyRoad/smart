import Layout from '@/page/index/'

export const dormitoryRoomVisualRoute = {
    path: '/platform/dormitory/room',
    component: Layout,
    redirect: '/platform/dormitory/room/visual', //宿舍管理，房间管理，可视化界面
    children: [{
      path: 'visual',
      component: () =>
        import ('@/views/platform/dormitory/room/visual')
    }]
  }

export const dormitoryWaterRoomDayRoute = {
    path: '/platform/dormitory/water_room_day',
    component: Layout,
    redirect: '/platform/dormitory/water_room_day/index', //宿舍管理，宿舍水电日计算表
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/dormitory/water_room_day/index')
    }]
  }

export const dormitoryWaterWarningRecordRoute = {
    path: '/platform/dormitory/water_warning_record',
    component: Layout,
    redirect: '/platform/dormitory/water_warning_record/index', //宿舍管理，水电告警记录
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/dormitory/water_warning_record/index')
    }]
  }

export const dormitoryWaterStatisticsRoute = {
    path: '/platform/dormitory/water_statistics',
    component: Layout,
    redirect: '/platform/dormitory/water_statistics/index', //宿舍管理，水电用量排行统计
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/dormitory/water_statistics/index')
    }]
  }

export const dormitoryNewRoomSdPublicRoute = {
    path: '/platform/dormitory/new_room',
    component: Layout,
    redirect: '/platform/dormitory/new_room/sd_public', //宿舍管理，房间管理，公摊水电
    children: [{
      path: 'sd_public',
      component: () =>
        import ('@/views/platform/dormitory/new_room/sd_public')
    }]
  }

export const dormitoryBedMngCheckInRoute = {
    path: '/platform/dormitory/bed_mng',
    component: Layout,
    redirect: '/platform/dormitory/bed_mng/check_in', //宿舍管理，床位管理，入住
    children: [{
      path: 'check_in/:id',
      component: () =>
        import ('@/views/platform/dormitory/bed_mng/check_in')
    }]
  }

export const dormitoryRepairsDetailRoute = {
    path: '/platform/dormitory/repairs',
    component: Layout,
    redirect: '/platform/dormitory/repairs/detail', //宿舍报修，报修详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/dormitory/repairs/detail')
    }]
  }
