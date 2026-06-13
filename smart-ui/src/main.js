import 'babel-polyfill'
import 'classlist-polyfill'
import Vue from 'vue'
import axios from './router/axios'
import VueAxios from 'vue-axios'
import App from './App'
import './permission' // 权限
import './error' // 日志
import router from './router/router'
import store from './store'
import mixins from '@/mixins/index'
import * as formRules from '@/util/formRules'
import { loadStyle } from './util/util'
import * as urls from '@/config/env'
import { iconfontUrl, iconfontVersion } from '@/config/env'
import * as filters from './filters' // 全局filter
import './styles/common.scss'
import Viewer from 'v-viewer'
import 'viewerjs/dist/viewer.css'
import basicContainer from './components/basic-container/main'
import tceSearchBar from "./components/tce-search-bar/index";
import tceImgUpload from "./components/tce-img/index";
import tceLabelJustify from "./components/tce-label-justify/index";
import parkSelect from "@/views/platform/components/park-select/index";
import buSelect from "@/views/platform/components/bu-select/index";
import buAllSelect from "@/views/platform/components/bu-all-select/index";
import deptSelect from "@/views/platform/components/dept-select/index";
import jobSelect from "@/views/platform/components/job-select/index";
import jcheSelect from "@/views/platform/components/jche-select/index";
import roomGenderSelect from "@/views/platform/components/room-gender-select/index";
import dormSelect from "@/views/platform/components/dorm-select/index";
import floorSelect from "@/views/platform/components/floor-select/index";
import roomSelect from "@/views/platform/components/room-select/index";
import imgUpload from "@/views/platform/components/_img_upload/index";
import authCarSelect from "@/views/platform/components/auth-car-select/index";
import authPersonSelect from "@/views/platform/components/auth-person-select/index";
import tceEmpty from '@/components/empty/empty'
import areaCascader from "@/views/platform/components/area-cascader/index"
import deptCascader from "@/views/platform/components/dept-cascader/index"

import roomTypeSelect from "@/views/platform/components/room-type-select/index";

import VueSeamlessScroll from 'vue-seamless-scroll'
Vue.use(VueSeamlessScroll)

//复制到粘贴板
import VueClipboard from 'vue-clipboard2'
VueClipboard.config.autoSetContainer = true
Vue.use(VueClipboard)

import errorImgPeaple from '../public/img/pl1.png' //人脸照
import errorImgPeapleCamera from '../public/img/pl2.png' //抓拍照
import errorImgPeaplePring from '../public/img/print_peaple.png' //标签打印处展示的人脸照
import errorImgIdentity from '../public/img/pl_identity.png' //身份证
import errorImgDriver from '../public/img/pl_driver.png' //驾驶证
import errorImgDriving from '../public/img/pl_driving.png' //行驶证
import errorImgSign from '../public/img/sign_img.png' //签字图
import errorImgGoods from '../public/img/rm_gzjy.png' //其他占位图
import errorImgCar from '../public/img/i_page3.png' //货车

import { validatenull } from '@/util/validate'
Vue.prototype.validatenull = validatenull

Vue.prototype.errorImgCar = function(){
  return 'this.onload=null;this.src=" '+errorImgCar+'";'
}
Vue.prototype.errorImgPeaple = function(){
  return 'this.onload=null;this.src=" '+errorImgPeaple+'";'
}
Vue.prototype.errorImgGoods = function(){
  return 'this.onload=null;this.src=" '+errorImgGoods+'";'
}
Vue.prototype.errorImgSign = function(){
  return 'this.onload=null;this.src=" '+errorImgSign+'";'
}
Vue.prototype.errorImgPeapleCamera = function(){
  return 'this.onload=null;this.src=" '+errorImgPeapleCamera+'";'
}
Vue.prototype.errorImgPeaplePring = function(){
  return 'this.onload=null;this.src=" '+errorImgPeaplePring+'";'
}
Vue.prototype.errorImgIdentity = function(){
  return 'this.onload=null;this.src=" '+errorImgIdentity+'";'
}
Vue.prototype.errorImgDriver = function(){
  return 'this.onload=null;this.src=" '+errorImgDriver+'";'
}
Vue.prototype.errorImgDriving = function(){
  return 'this.onload=null;this.src=" '+errorImgDriving+'";'
}

Vue.prototype.doClearValidate = function(formName){
  this.$nextTick(()=>{
    this.$refs[formName] && this.$refs[formName].clearValidate()
  })
}

// Vue.use(Viewer, {
//   defaultOptions: {
//     inline: false,
//     button: true,
//     navbar: false,
//     title: false,
//     toolbar: true,
//     tooltip: false,
//     movable: true,
//     zoomable: true,
//     rotatable: true,
//     scalable: true,
//     transition: true,
//     fullscreen: false,
//     keyboard: false,
//     url: "data-source"
//   }
// })
Vue.use(Viewer);
Viewer.setDefaults({
  Options: {
    "inline": true,
    "button": true,
    "navbar": true,
    "title": true,
    "toolbar": true,
    "tooltip": true,
    "movable": true,
    "zoomable": true,
    "rotatable": true,
    "scalable": true,
    "transition": true,
    "fullscreen": false,
    "keyboard": true,
    "url": "data-source"
  }
});


Vue.use(router)

Vue.use(VueAxios, axios)

// 注册全局容器
Vue.component('basicContainer', basicContainer)
Vue.component('tceSearchBar', tceSearchBar)
Vue.component('tceImgUpload', tceImgUpload)
Vue.component('tceLabelJustify', tceLabelJustify)
Vue.component('parkSelect', parkSelect)
Vue.component('buSelect', buSelect)
Vue.component('buAllSelect', buAllSelect)
Vue.component('deptSelect', deptSelect)
Vue.component('jobSelect', jobSelect)
Vue.component('jcheSelect', jcheSelect)
Vue.component('roomGenderSelect', roomGenderSelect)
Vue.component('dormSelect', dormSelect)
Vue.component('floorSelect', floorSelect)
Vue.component('roomSelect', roomSelect)
Vue.component('roomTypeSelect', roomTypeSelect)
Vue.component('imgUpload', imgUpload)
Vue.component('authCarSelect', authCarSelect)
Vue.component('authPersonSelect', authPersonSelect)
Vue.component('tceEmpty', tceEmpty)
Vue.component('areaCascader', areaCascader)
Vue.component('deptCascader', deptCascader)




// 加载相关url地址
Object.keys(urls).forEach(key => {
  Vue.prototype[key] = urls[key]
})

//加载过滤器
Object.keys(filters).forEach(key => {
  Vue.filter(key, filters[key])
})

// 动态加载阿里云字体库
iconfontVersion.forEach(ele => {
  loadStyle(iconfontUrl.replace('$key', ele))
})

const helper = {
  formRules: formRules
}

window.tce = window.tce || {}
window.tce.helper = helper

Vue.config.productionTip = false

new Vue({
  router,
  store,
  mixins,
  helper,
  render: h => h(App)
}).$mount('#app')
