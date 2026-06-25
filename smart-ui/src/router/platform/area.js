import Layout from '@/page/index/'
import parkSettingLayout from '@/views/platform/area/park/setting'

export const areaAreaPersonRoute = {
    path: '/platform/area/area',
    component: Layout,
    redirect: '/platform/area/area/person', //区域管理，区域管理，通关人员
    children: [{
      path: 'person/:id',
      component: () =>
        import ('@/views/platform/area/area/person')
    }]
  }

export const areaLimitAddRoute = {
    path: '/platform/area/limit',
    component: Layout,
    redirect: '/platform/area/limit/add', //区域管理，权限策略，添加权限策略
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/area/limit/add')
    }]
  }

export const areaLimitEditRoute = {
    path: '/platform/area/limit',
    component: Layout,
    redirect: '/platform/area/limit/edit', //区域管理，权限策略，编辑权限策略
    children: [{
      path: 'edit/:id',
      component: () =>
        import ('@/views/platform/area/limit/edit')
    }]
  }

export const areaLimitPersonDetailRoute = {
    path: '/platform/area/limit',
    component: Layout,
    redirect: '/platform/area/limit/personDetail', //区域管理，权限策略，权限关联员工
    children: [{
      path: 'personDetail/:id/:type',
      component: () =>
        import ('@/views/platform/area/limit/personDetail')
    }]
  }

export const areaLimitVechileDetailRoute = {
    path: '/platform/area/limit',
    component: Layout,
    redirect: '/platform/area/limit/vechileDetail', //区域管理，权限策略，权限关联车辆
    children: [{
      path: 'vechileDetail/:id/:type',
      component: () =>
        import ('@/views/platform/area/limit/vechileDetail')
    }]
  }

export const areaParkSettingRoute = {
    path: '/platform/area/park',
    component: Layout,
    children: [{
      path: 'setting',
      component: parkSettingLayout
    }]
  }

export const areaParkSettingRoute2 = {
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
  }

export const areaParkSettingRoute3 = {
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
  }

export const areaParkSettingRoute4 = {
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
  }
