import Layout from '@views-mobile/layout'
export default {
  path: '/xuchang/test',
  component: Layout,
  children: [{
    path: '',
    component: () => import('@views-mobile/pages/test/index'),
    meta: { lable: '内容详情' }
  }]
}
