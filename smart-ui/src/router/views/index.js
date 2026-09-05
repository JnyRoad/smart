import PrintRoutes from './print'
import Layout from '@/page/index/'
import Layout2 from '@/views/platform/panel/index/'
export default [
  ...PrintRoutes,
  {
    path: '/home',
    component: Layout,
    redirect: '/home/index',
    children: [{
        path: 'index',
        name: '首页',
        component: () =>
            import ('@/views/platform/home/index'),
    }]
  },
  {
    path: '/platform/panel',
    name: '可视化面板',/* 可视化面板 */
    component: () =>
        import ('@/views/platform/panel/index'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },
  {
    path: '/platform/dormitory/pad',
    name: '宿舍水电抄表',/* 链接跳转过来显示的简历登记页面 */
    component: () =>
        import ('@/views/platform/dormitory/new_room/index'),
    meta: {
        keepAlive: true,
        isTab: false,
        isPad: true
    }
  },
  {
    path: '/platform/panel',
    component: Layout2,
    redirect: '/platform/panel/bigdata',
    children: [{
        path: 'bigdata',
        name: '园区概况',
        component: () =>
          import ('@/views/platform/panel/bigdata'),
        meta: {
          keepAlive: true,
          isTab: false,
          isAuth: false
        }
    }]
  },
  {
    path: '/platform/panel',
    component: Layout2,
    redirect: '/platform/panel/accessto',
    children: [{
        path: 'accessto',
        name: '人员出入',
        component: () =>
          import ('@/views/platform/panel/accessto'),
        meta: {
          keepAlive: true,
          isTab: false,
          isAuth: false
        }
    }]
  },
  {
    path: '/platform/panel/park',
    name: '可视化',/* 可视化面板 */
    component: () =>
        import ('@/views/platform/panel_new/park'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },
  {
    path: '/platform/panel/view',
    name: '可视化-园区概括',/* 可视化面板 */
    component: () =>
        import ('@/views/platform/panel_new/view'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },
  {
    path: '/platform/panel/person',
    name: '可视化-人员出入',/* 可视化面板 */
    component: () =>
        import ('@/views/platform/panel_new/person'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },
  {
    path: '/gangwei/:id',
    name: '简历登记',/* 链接跳转过来显示的简历登记页面 */
    component: () =>
        import ('@/views/platform/resume/index'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },
  {
    path: '/platform/face',
    name: '人脸识别',/* 链接跳转过来显示的简历登记页面 */
    component: () =>
        import ('@/views/platform/resume/face'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },
  {
    path: '/platform/info',
    name: '信息',/* 链接跳转过来显示的简历登记页面 */
    component: () =>
        import ('@/views/platform/resume/info'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },
  {
    path: '/platform/resumeFinish',
    name: '信息完成',/* 链接跳转过来显示的简历登记页面 */
    component: () =>
        import ('@/views/platform/resume/resume_finish'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },
  {
      path: '/platform/appointment',
      name: '预约访客',/* 裕同官网跳转过来要显示的页面 */
      component: () =>
          import ('@/views/platform/appointment/index'),
      meta: {
          keepAlive: true,
          isTab: false,
          isAuth: false
      }
  },
  {
    path: '/visitor/qrCode',
    name: '访客核实',/* */
    component: () =>
        import ('@/views/platform/visitor/visitor_record/qrCode/index'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },
  {
    path: '/qrCode',
    name: '访客条打印',/* */
    component: () =>
        import ('@/views/platform/visitor/qrCode/index'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },
  {
    path: '/qrCodeNew',
    name: '访客条打印（新）',/* */
    component: () =>
        import ('@/views/platform/visitor/qrCode_new/index'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },
  {
    path: '/info',
    component: Layout,
    redirect: '/info/index',
    children: [{
        path: 'index',
        name: '个人信息',
        component: () =>
            import ('@/views/admin/user/info'),
    }]
  },
  {
    path: '/editPwd',
      name: '修改密码',
      component: () =>
          import ('@/views/admin/user/info'),
      meta: {
          keepAlive: true,
          isTab: false,
          isAuth: false
      }
  },{
    path: '/video',
    name: '视频',/* */
    component: () =>
        import ('@/views/platform/info_delivery/data_show/video'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  },{
    path: '/pdf',
    name: 'PDF',/* */
    component: () =>
        import ('@/views/platform/info_delivery/data_show/pdf'),
    meta: {
        keepAlive: true,
        isTab: false,
        isAuth: false
    }
  }
]
