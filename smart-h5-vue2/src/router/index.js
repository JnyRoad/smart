import Vue from 'vue'
import VueRouter from 'vue-router'
import viewsUpRouter from './views/index'
import homeRouter from './pages/home'
import mineRouter from './pages/mine'
import dormRouter from './pages/dorm'
import backLogRouter from './pages/backLog'
import loginRouter from './pages/login'
import goodReleaseLiveRouter from './pages/good-release-live'
import goodReleaseWorkRouter from './pages/good-release-work'
import returnFactoryRouter from './pages/return-factory'
import visitorRouter from './pages/visitor'
import checkInRouter from './pages/check-in'
import dormRepairsRouter from './pages/dorm-repairs'
import helpRouter from './pages/help'
import dormExitRouter from './pages/dorm-exit'

import testRouter from './pages/test'
Vue.use(VueRouter)

export default new VueRouter({
  routes: [
    loginRouter,
    homeRouter,
    mineRouter,
    dormRouter,
    goodReleaseLiveRouter,
    goodReleaseWorkRouter,
    returnFactoryRouter,
    backLogRouter,
    visitorRouter,
    checkInRouter,
    dormRepairsRouter,
    helpRouter,
    dormExitRouter,

    testRouter,
    viewsUpRouter
  ]
})
