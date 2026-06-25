import Layout from '@/page/index/'
import emailLayout from '@/views/platform/recruit/emailmodel/index'

export const recruitRecruitmentAddRoute = {
    path: '/platform/recruit/recruitment',
    component: Layout,
    redirect: '/platform/recruit/recruitment/add', //招聘管理，招聘岗位,添加岗位
    children: [{
      path: 'add',
      component: () =>
        import ('@/views/platform/recruit/recruitment/add')
    }]
  }

export const recruitRecruitmentDetailRoute = {
    path: '/platform/recruit/recruitment',
    component: Layout,
    redirect: '/platform/recruit/recruitment/detail', //招聘管理，招聘岗位详情、编辑页面
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/recruit/recruitment/detail')
    }]
  }

export const recruitApplicantDetailRoute = {
    path: '/platform/recruit/applicant',
    component: Layout,
    redirect: '/platform/recruit/applicant/detail', //招聘管理，招聘管理，应聘详情
    children: [{
      path: 'detail/:id',
      component: () =>
        import ('@/views/platform/recruit/applicant/detail')
    }]
  }

export const recruitEmailmodelModelRoute = {
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
  }

export const recruitEmailmodelModelRoute2 = {
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
  }

export const recruitEmailmodelModelRoute3 = {
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
  }

export const recruitEntryStep2Route = {
    path: '/platform/recruit/entry',
    component: Layout,
    redirect: '/platform/recruit/entry/step2', //批量入职第二步
    children: [{
      path: 'step2',
      component: () =>
        import ('@/views/platform/recruit/entry/step2')
    }]
  }
