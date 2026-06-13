import Layout from '@/page/index/'

import emailLayout from '@/views/platform/recruit/emailmodel/index'
import parkSettingLayout from '@/views/platform/area/park/setting'
export default [
  {
    path: '/platform/records/isc_card_import',
    component: Layout,
    redirect: '/platform/records/isc_card_import/index',
    children: [{
      path: 'index',
      component: () =>
        import ('@/views/platform/records/isc_card_import/index')
    }]
  },
  {
    path: '/platform/records/isc_access_cleanup',
    component: Layout,
    redirect: '/platform/records/isc_access_cleanup/index',
    children: [{
      path: 'index',
      component: () =>
        import ('@/views/platform/records/isc_access_cleanup/index')
    }]
  },
  {
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
  },
  {
    path: '/platform/security_area/securityProjectMng',
    component: Layout,
    redirect: '/platform/security_area/securityProjectMng', //保密项目维护
    children: [{
      path: 'staff/:id', //-授权人员
      component: () =>
        import ('@/views/platform/security_area/xc_project_mng/staff')
    }]
  },
  {
    path: '/platform/outsourcing',
    component: Layout,
    redirect: '/platform/outsourcing/detail', //查看外包人员申请信息
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/outsourcing/detail/detail')
    }]
  },
  {
    path: '/platform/dormitory/room',
    component: Layout,
    redirect: '/platform/dormitory/room/visual', //宿舍管理，房间管理，可视化界面
    children: [{
      path: 'visual',
      component: () =>
        import ('@/views/platform/dormitory/room/visual')
    }]
  },{
    path: '/platform/dormitory/water_room_day',
    component: Layout,
    redirect: '/platform/dormitory/water_room_day/index', //宿舍管理，宿舍水电日计算表
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/dormitory/water_room_day/index')
    }]
  }, {
    path: '/platform/dormitory/water_warning_record',
    component: Layout,
    redirect: '/platform/dormitory/water_warning_record/index', //宿舍管理，水电告警记录
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/dormitory/water_warning_record/index')
    }]
  },{
    path: '/platform/dormitory/water_statistics',
    component: Layout,
    redirect: '/platform/dormitory/water_statistics/index', //宿舍管理，水电用量排行统计
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/dormitory/water_statistics/index')
    }]
  },
  {
    path: '/platform/equipment/hydropower',
    component: Layout,
    redirect: '/platform/equipment/hydropower/index', //设备管理，水电数据看板
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/equipment/hydropower/index')
    }]
  },{
    path: '/platform/device/carDistributionRecord',
    component: Layout,
    redirect: '/platform/device/carDistributionRecord/index', //设备管理，车辆下发记录
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/device/carDistributionRecord/index')
    }]
  },{
    path: '/platform/device/faceDistributionRecord',
    component: Layout,
    redirect: '/platform/device/faceDistributionRecord/index', //设备管理，人脸下发记录
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/device/faceDistributionRecord/index')
    }]
  },
   {
    path: '/platform/dormitory/new_room',
    component: Layout,
    redirect: '/platform/dormitory/new_room/sd_public', //宿舍管理，房间管理，公摊水电
    children: [{
      path: 'sd_public',
      component: () =>
        import ('@/views/platform/dormitory/new_room/sd_public')
    }]
  }, {
    path: '/platform/dormitory/bed_mng',
    component: Layout,
    redirect: '/platform/dormitory/bed_mng/check_in', //宿舍管理，床位管理，入住
    children: [{
      path: 'check_in/:id',
      component: () =>
        import ('@/views/platform/dormitory/bed_mng/check_in')
    }]
  },{
    path: '/platform/dormitory/repairs',
    component: Layout,
    redirect: '/platform/dormitory/repairs/detail', //宿舍报修，报修详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/dormitory/repairs/detail')
    }]
  },{
    path: '/platform/in_out/return_factory',
    component: Layout,
    redirect: '/platform/in_out/return_factory/detail', //宿舍报修，报修详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/in_out/return_factory/detail')
    }]
  }, {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/xc_lock_door', //设备管理, 门锁管理下发记录
    children: [{
      path: 'lock_door/record/:id',
      component: () =>
        import ('@/views/platform/device/xc_lock_door/components/record')
    }]
  }, {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/water_manage', //设备管理, 水表历史度数
    children: [{
      path: 'waterManage/record/:id',
      component: () =>
        import ('@/views/platform/device/water_manage/components/record')
    }]
  }, {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/water_manage', //设备管理, 水表操作记录
    children: [{
      path: 'waterManage/log/:id',
      component: () =>
        import ('@/views/platform/device/water_manage/components/logs')
    }]
  }, {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/valve_manage', //设备管理, 水表操作记录
    children: [{
      path: 'valveManage/log/:id',
      component: () =>
        import ('@/views/platform/device/valve_manage/logs')
    }]
  }, {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/electric_manage', //设备管理, 电表历史度数
    children: [{
      path: 'electricManage/record/:id',
      component: () =>
        import ('@/views/platform/device/electric_manage/components/record')
    }]
  }, {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/electric_manage', //设备管理, 电表操作记录
    children: [{
      path: 'electricManage/log/:id',
      component: () =>
        import ('@/views/platform/device/electric_manage/components/logs')
    }]
  }, {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/person_list', //设备管理, 门禁和闸机的通关人员
    children: [{
      path: 'person_list/:id',
      component: () =>
        import ('@/views/platform/device/person_list')
    }]
  }, {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/gate_limit', //设备管理, 闸机管理，更新设备权限
    children: [{
      path: 'gate_limit',
      component: () =>
        import ('@/views/platform/device/gate_limit')
    }]
  }, {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/automatic_limit', //设备管理, 道闸管理，更新设备权限
    children: [{
      path: 'automatic_limit',
      component: () =>
        import ('@/views/platform/device/automatic_limit')
    }]
  }, {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/ledInfo', //设备管理, 道闸管理，编辑显示屏信息
    children: [{
      path: 'ledInfo/:id',
      component: () =>
        import ('@/views/platform/device/ledInfo')
    }]
  }, {
    path: '/platform/device',
    component: Layout,
    redirect: '/platform/device/vehicle_list', //设备管理, 道闸的通关车辆
    children: [{
      path: 'vehicle_list/:id',
      component: () =>
        import ('@/views/platform/device/vehicle_list')
    }]
  },
  {
    path: '/platform/entrance/face',
    component: Layout,
    name: 'faceDetail',
    redirect: '/platform/entrance/face/detail', //人员出入详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/entrance/face/detail')
    }]
  },
  {
    path: '/platform/entrance/vehicle',
    component: Layout,
    redirect: '/platform/entrance/vehicle/detail', //车辆出入详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/entrance/vehicle/detail')
    }]
  },
  {
    path: '/platform/entrance/article',
    component: Layout,
    redirect: '/platform/entrance/article/detail', //物品放行详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/entrance/article/detail')
    }]
  },
  {
    path: '/platform/logistics_vehicle/reserve_record',
    component: Layout,
    redirect: '/platform/logistics_vehicle/reserve_record/detail', //车辆出入详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/logistics_vehicle/reserve_record/detail')
    }]
  }, {
    path: '/platform/parking/present_parking',
    component: Layout,
    redirect: '/platform/parking/present_parking/detail', //停车场管理,当前车辆和停车记录的详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/parking/present_parking/detail')
    }]
  },
  {
    path: '/platform/vehicle/staff_vehicle',
    component: Layout,
    redirect: '/platform/vehicle/staff_vehicle/detail', //车辆管理，员工车辆详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/vehicle/staff_vehicle/detail')
    }]
  },
  {
    path: '/platform/personnel_manage/staff_appeal',
    component: Layout,
    redirect: '/platform/personnel_manage/staff_appeal/detail', //员工申诉，申诉详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/personnel_manage/staff_appeal/detail')
    }]
  }, {
    path: '/platform/vehicle/staff_vehicle',
    component: Layout,
    redirect: '/platform/vehicle/staff_vehicle/add', //车辆管理，员工车辆，添加车辆
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/vehicle/staff_vehicle/add')
    }]
  }, {
    path: '/platform/vehicle/entry_examine',
    component: Layout,
    redirect: '/platform/vehicle/entry_examine/detail', //车辆管理，入园审批详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/vehicle/entry_examine/detail')
    }]
  }, {
    path: '/platform/vehicle/company_vehicle',
    component: Layout,
    redirect: '/platform/vehicle/company_vehicle/add', //车辆管理，公司车辆，添加公车
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/vehicle/company_vehicle/add')
    }]
  }, {
    path: '/platform/vehicle/non_staff_vehicle',
    component: Layout,
    redirect: '/platform/vehicle/non_staff_vehicle/detail', //车辆管理，非员工车辆详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/vehicle/non_staff_vehicle/detail')
    }]
  }, {
    path: '/platform/vehicle/non_staff_vehicle',
    component: Layout,
    redirect: '/platform/vehicle/non_staff_vehicle/add', //车辆管理，非员工车辆，添加车辆
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/vehicle/non_staff_vehicle/add')
    }]
  }, {
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
  }, {
    path: '/platform/basic/staff_info',
    component: Layout,
    redirect: '/platform/basic/staff_info/detail', //基础信息，员工信息详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/basic/staff_info/detail')
    }]
  }, {
    path: '/platform/basic/upload_record',
    component: Layout,
    redirect: '/platform/basic/upload_record/detail', //基础信息，上传员工信息详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/basic/upload_record/detail')
    }]
  }, {
    path: '/platform/personnel_manage/salary_sign',
    component: Layout,
    redirect: '/platform/personnel_manage/salary_sign/detail', //人资行政管理，工资签收管理
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/personnel_manage/salary_sign/detail')
    }]
  }, {
    path: '/platform/personnel_manage/attendance_sign',
    component: Layout,
    redirect: '/platform/personnel_manage/attendance_sign/detail', //人资行政管理，考勤汇总确认管理
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/personnel_manage/attendance_sign/detail')
    }]
  }, {
    path: '/platform/business/visitor',
    component: Layout,
    redirect: '/platform/business/visitor/detail', //业务设置，访客设置
    children: [{
      path: 'detail/:parkId',
      component: () =>
        import ('@/views/platform/business/visitor/detail')
    }]
  }, {
    path: '/platform/business/release',
    component: Layout,
    redirect: '/platform/business/release/detail', //业务设置，物品放行设置
    children: [{
      path: 'detail',
      component: () =>
        import ('@/views/platform/business/release/detail')
    }]
  }, {
    path: '/platform/business/dorm_exit',
    component: Layout,
    redirect: '/platform/business/dorm_exit/detail', //业务设置，物品放行设置
    children: [{
      path: 'detail',
      component: () =>
        import ('@/views/platform/business/dorm_exit/detail')
    }]
  }, {
    path: '/platform/business/repair',
    component: Layout,
    redirect: '/platform/business/repair/detail', //业务设置，园区报修设置
    children: [{
      path: 'detail',
      component: () =>
        import ('@/views/platform/business/repair/detail')
    }]
  }, {
    path: '/platform/business/security_area',
    component: Layout,
    redirect: '/platform/business/security_area/edit', //业务设置，访客设置
    children: [{
      path: 'edit/:parkId',
      component: () =>
        import ('@/views/platform/business/security_area/edit')
    }]
  },
  {
    path: '/platform/business/hydropowerAlarm',
    component: Layout,
    redirect: '/platform/business/hydropower_alarm/index', //业务设置，水电告警设置
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/business/hydropower_alarm/index')
    }]
  },
  {
    path: '/platform/business/dormitoryAlarm',
    component: Layout,
    redirect: '/platform/business/dormitory_alarm/index', //业务设置，宿舍设置
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/business/dormitory_alarm/index')
    }]
  },
  {
    path: '/platform/business/xcMsgSet',
    component: Layout,
    redirect: '/platform/business/xc_msg_set/index', //业务设置，消息设置
    children: [{
      path: '',
      component: () =>
        import ('@/views/platform/business/xc_msg_set/index')
    }]
  },
  // hydropower_alarm
  {
    path: '/platform/work/askLeave',
    component: Layout,
    redirect: '/platform/work/askLeave/detail', //业务监控，请假详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/askLeave/detail')
    }]
  },
  {
    path: '/platform/work/overTime',
    component: Layout,
    redirect: '/platform/work/overTime/detail', //业务监控，加班详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/overTime/detail')
    }]
  },
  {
    path: '/platform/work/attendance',
    component: Layout,
    redirect: '/platform/work/attendance/detail', //业务监控，补卡详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/attendance/detail')
    }]
  },
  {
    path: '/platform/work/leaveApplication',
    component: Layout,
    redirect: '/platform/work/leaveApplication/detail', //业务监控，补卡详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/leaveApplication/detail')
    }]
  },
  {
    path: '/platform/work/toStaff',
    component: Layout,
    redirect: '/platform/work/toStaff/detail', //业务监控，入职记录详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/toStaff/detail')
    }]
  },
  {
    path: '/platform/work/outDormitory',
    component: Layout,
    redirect: '/platform/work/outDormitory/detail', //业务监控，外宿补贴详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/outDormitory/detail')
    }]
  },
  {
    path: '/platform/work/breakOff',
    component: Layout,
    redirect: '/platform/work/breakOff/detail', //业务监控，调休记录详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/breakOff/detail')
    }]
  },
  {
    path: '/platform/work/checkedOut',
    component: Layout,
    redirect: '/platform/work/checkedOut/detail', //业务监控，调休记录详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/work/checkedOut/detail')
    }]
  },
  {
    path: '/platform/park_service/feed_back',
    component: Layout,
    redirect: '/platform/park_service/feed_back/detail', //园区服务，员工反馈详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/park_service/feed_back/detail')
    }]
  },
  {
    path: '/platform/park_service/paper',
    component: Layout,
    redirect: '/platform/park_service/paper/detail', //园区服务，调查表详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/park_service/paper/detail')
    }]
  },
  {
    path: '/platform/park_service/paper',
    component: Layout,
    redirect: '/platform/park_service/paper/add', //园区服务，调查表添加
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/park_service/paper/add')
    }]
  },
  {
    path: '/platform/basic/staff_info',
    component: Layout,
    redirect: '/platform/basic/staff_info/auth', //基础信息，员工权限详情
    children: [{
      path: 'auth/:id',
      component: () =>
        import ('@/views/platform/basic/staff_info/auth')
    }]
  },
  {
    path: '/platform/basic/xc_batchImportImg',
    component: Layout,
    redirect: '/platform/basic/xc_batchImportImg/detail', //基础信息，批量导入照片详情
    children: [{
      path: 'detail',
      component: () =>
        import ('@/views/platform/basic/xc_batch_import_staff_img/detail')
    }]
  }, {
    path: '/platform/basic/app_permis',
    component: Layout,
    redirect: '/platform/basic/app_permis/add', //基础信息，APP权限，添加
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/basic/app_permis/add')
    }]
  }, {
    path: '/platform/basic/app_permis',
    component: Layout,
    redirect: '/platform/basic/app_permis/edit', //基础信息，APP权限，编辑
    children: [{
      path: 'edit/:id',
      component: () =>
        import ('@/views/platform/basic/app_permis/edit')
    }]
  }, {
    path: '/platform/recruit/recruitment',
    component: Layout,
    redirect: '/platform/recruit/recruitment/add', //招聘管理，招聘岗位,添加岗位
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/recruit/recruitment/add')
    }]
  }, {
    path: '/platform/recruit/recruitment',
    component: Layout,
    redirect: '/platform/recruit/recruitment/detail', //招聘管理，招聘岗位详情、编辑页面
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/recruit/recruitment/detail')
    }]
  }, {
    path: '/platform/recruit/applicant',
    component: Layout,
    redirect: '/platform/recruit/applicant/detail', //招聘管理，招聘管理，应聘详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/recruit/applicant/detail')
    }]
  },
  {
    path: '/platform/recruit/emailmodel',
    component: Layout,
    children: [{
      path: 'model',
      component: emailLayout,
      children: [{
        path: 'msinform', //招聘管理，邮件通知模板，面试通知
        component: () =>
          import ('@/views/platform/recruit/emailmodel/msinform')
      }]
    }]
  },
  {
    path: '/platform/recruit/emailmodel',
    component: Layout,
    children: [{
      path: 'model',
      component: emailLayout,
      children: [{
        path: 'rzzbinform', //招聘管理，邮件通知模板，入职准备通知
        component: () =>
          import ('@/views/platform/recruit/emailmodel/rzzbinform')
      }]
    }]
  },
  {
    path: '/platform/recruit/emailmodel',
    component: Layout,
    children: [{
      path: 'model',
      component: emailLayout,
      children: [{
        path: 'rzinform', //招聘管理，邮件通知模板，入职通知
        component: () =>
          import ('@/views/platform/recruit/emailmodel/rzinform')
      }]
    }]
  }, {
    path: '/platform/recruit/entry',
    component: Layout,
    redirect: '/platform/recruit/entry/step2', //批量入职第二步
    children: [{
      path: 'step2',
      component: () =>
        import ('@/views/platform/recruit/entry/step2')
    }]
  },
  {
    path: '/platform/visitor/qrCode',
    component: Layout,
    redirect: '/index',
    children: [{
      path: 'index',
      name: '访客码',
      component: () =>
        import ('@/views/platform/visitor/qrCode/index'),
    }]
  },
  {
    path: '/platform/visitor/visitor_record',
    component: Layout,
    redirect: '/platform/visitor/visitor_record/detail', //访客预约，预约记录详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/visitor/visitor_record/detail')
    }]
  }, {
    path: '/platform/visitor/incoming_record',
    component: Layout,
    redirect: '/platform/visitor/incoming_record/detail', //入厂申请记录，入厂申请详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/visitor/incoming_record/detail')
    }]
  }, {
    path: '/platform/business_manage/article',
    component: Layout,
    redirect: '/platform/business_manage/article/detail', //业务管理-物品放行
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/business_manage/article/detail')
    }]
  },{
    path: '/platform/area/area',
    component: Layout,
    redirect: '/platform/area/area/person', //区域管理，区域管理，通关人员
    children: [{
      path: 'person/:id',
      component: () =>
        import ('@/views/platform/area/area/person')
    }]
  }, {
    path: '/platform/area/limit',
    component: Layout,
    redirect: '/platform/area/limit/add', //区域管理，权限策略，添加权限策略
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/area/limit/add')
    }]
  }, {
    path: '/platform/area/limit',
    component: Layout,
    redirect: '/platform/area/limit/edit', //区域管理，权限策略，编辑权限策略
    children: [{
      path: 'edit/:id',
      component: () =>
        import ('@/views/platform/area/limit/edit')
    }]
  }, {
    path: '/platform/area/limit',
    component: Layout,
    redirect: '/platform/area/limit/personDetail', //区域管理，权限策略，权限关联员工
    children: [{
      path: 'personDetail/:id/:type',
      component: () =>
        import ('@/views/platform/area/limit/personDetail')
    }]
  },{
    path: '/platform/area/limit',
    component: Layout,
    redirect: '/platform/area/limit/vechileDetail', //区域管理，权限策略，权限关联车辆
    children: [{
      path: 'vechileDetail/:id/:type',
      component: () =>
        import ('@/views/platform/area/limit/vechileDetail')
    }]
  }, {
    path: '/platform/alarm/record',
    component: Layout,
    redirect: '/platform/alarm/record/detail', //安防记录，警报记录，详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/alarm/record/detail')
    }]
  },
  {
    path: '/platform/area/park',
    component: Layout,
    children: [{
      path: 'setting',
      component: parkSettingLayout
    }]
  },
  {
    path: '/platform/area/park',
    component: Layout,
    children: [{
      path: 'setting',
      component: parkSettingLayout,
      children: [{
        path: 'setbu', //园区管理，配置信息，组织关系
        component: () =>
          import ('@/views/platform/area/park/setBu')
      }]
    }]
  },
  {
    path: '/platform/area/park',
    component: Layout,
    children: [{
      path: 'setting',
      component: parkSettingLayout,
      children: [{
        path: 'setvisitor', //园区管理，配置信息，访客设置
        component: () =>
          import ('@/views/platform/area/park/setVisitor')
      }]
    }]
  },
  {
    path: '/platform/area/park',
    component: Layout,
    children: [{
      path: 'setting',
      component: parkSettingLayout,
      children: [{
        path: 'setrecruit', //园区管理，配置信息，招聘设置
        component: () =>
          import ('@/views/platform/area/park/setRecruit')
      }]
    }]
  },
  // {
  //   path: '/platform/info_delivery',
  //   component: Layout,
  //   redirect: '/platform/info_delivery/data_show/index',
  //   children: [{
  //     path: 'data_show',
  //     name: '文本展示',
  //     component: () =>
  //       import ('@/views/platform/info_delivery/data_show/index'),
  //   }]
  // },
  {
    path: '/platform/info_delivery/info_mng',
    component: Layout,
    redirect: '/platform/info_delivery/info_mng/add',
    children: [{
      path: 'add',
      name: '添加信息资源',
      component: () =>
        import ('@/views/platform/info_delivery/info_mng/add'),
    }]
  },
  {
    path: '/platform/info_delivery/info_mng',
    component: Layout,
    redirect: '/platform/info_delivery/info_mng/edit',
    children: [{
      path: 'edit/:id',
      name: '编辑信息资源',
      component: () =>
        import ('@/views/platform/info_delivery/info_mng/edit'),
    }]
  }
]
