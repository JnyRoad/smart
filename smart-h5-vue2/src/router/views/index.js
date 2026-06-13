export default {
  path: '/',
  component: () => import(/* webpackChunkName: "pagesChunks" */ '@views-mobile/index'),
  meta: { lable: '工作台', isAuth: false }
}
