// 正式打印工作区需要登录；模板、设备和现场处置按园区逐接口鉴权。
import Layout from '@/page/index/'
export default [{
  path: '/platform/print', component: Layout, redirect: '/platform/print/templates',
  children: [{
    path: '', component: () => import('@/views/platform/print/index.vue'),
    children: [
      { path: 'templates', name: '打印单面模板', component: () => import('@/views/platform/print/templates/index.vue'), meta: { keepAlive: false, isAuth: true } },
      { path: 'pairs', name: '打印正反面组合', component: () => import('@/views/platform/print/pairs/index.vue'), meta: { keepAlive: false, isAuth: true } },
      { path: 'jobs/manual', name: '人员厂牌打印', component: () => import('@/views/platform/print/jobs/manual.vue'), meta: { keepAlive: false, isAuth: true } },
      { path: 'jobs/visitor', name: '访客凭条打印工作台', component: () => import('@/views/platform/print/jobs/visitor.vue'), meta: { keepAlive: false, isAuth: true } },
      { path: 'printers', name: '打印机档案', component: () => import('@/views/platform/print/printers/index.vue'), meta: { keepAlive: false, isAuth: true } },
      { path: 'bindings', name: '打印模板适用规则', component: () => import('@/views/platform/print/bindings/index.vue'), meta: { keepAlive: false, isAuth: true } }
    ]
  }]
}]
