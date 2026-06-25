import Layout from '@/page/index/'

export const basicStaffInfoDetailRoute = {
    path: '/platform/basic/staff_info',
    component: Layout,
    redirect: '/platform/basic/staff_info/detail', //基础信息，员工信息详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/basic/staff_info/detail')
    }]
  }

export const basicUploadRecordDetailRoute = {
    path: '/platform/basic/upload_record',
    component: Layout,
    redirect: '/platform/basic/upload_record/detail', //基础信息，上传员工信息详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/basic/upload_record/detail')
    }]
  }

export const basicStaffInfoAuthRoute = {
    path: '/platform/basic/staff_info',
    component: Layout,
    redirect: '/platform/basic/staff_info/auth', //基础信息，员工权限详情
    children: [{
      path: 'auth/:id',
      component: () =>
        import ('@/views/platform/basic/staff_info/auth')
    }]
  }

export const basicXcBatchImportImgDetailRoute = {
    path: '/platform/basic/xc_batchImportImg',
    component: Layout,
    redirect: '/platform/basic/xc_batchImportImg/detail', //基础信息，批量导入照片详情
    children: [{
      path: 'detail',
      component: () =>
        import ('@/views/platform/basic/xc_batch_import_staff_img/detail')
    }]
  }

export const basicAppPermisAddRoute = {
    path: '/platform/basic/app_permis',
    component: Layout,
    redirect: '/platform/basic/app_permis/add', //基础信息，APP权限，添加
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/basic/app_permis/add')
    }]
  }

export const basicAppPermisEditRoute = {
    path: '/platform/basic/app_permis',
    component: Layout,
    redirect: '/platform/basic/app_permis/edit', //基础信息，APP权限，编辑
    children: [{
      path: 'edit/:id',
      component: () =>
        import ('@/views/platform/basic/app_permis/edit')
    }]
  }
