// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue';
import MyFrame from './MyFrame.vue';
import router from './router';
import $ from 'jquery';
import echarts from 'echarts';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';

import './assets/css/index.css';
import './assets/css/element.css';

Vue.prototype.router = router;
Vue.prototype.echarts = echarts;
Vue.prototype.$ = $;
Vue.prototype.loadingOptions = {
  fullscreen: false,
  body: true,
  lock: true,
  text: '数据加载中，请稍后……',
  spinner: 'el-icon-loading',
  background: 'rgba(0,0,0,0.1)'
};

Vue.use(ElementUI);
Vue.config.productionTip = true;

Vue.prototype.get = function (url, success, error, async, load) {
  ajax('GET', url, null, success, error, async, load);
};
Vue.prototype.post = function (url, data, success, error, async, load) {
  ajax('POST', url, data, success, error, async, load);
};
function ajax(method, url, data, success, error, async, load) {
  let req = {
    type: method,
    url: url,
    cache: false,
    withCredentials: true,
    contentType: 'application/json',
    dataType: 'JSON',
    complete: function () {
      if (loading) {
        loading.close();
      }
    },
    success: function (r) {
      if (success) {
        success(r);
      }
    },
    error: function (e) {
      if (error) {
        error(e);
        return;
      }
      if (e.responseJSON && e.responseJSON.message) {
        ElementUI.Message.error(e.responseJSON.message);
        return;
      }
      ElementUI.Message.error('服务器繁忙，请稍后再进行尝试');
    }
  };
  if (async === false || async === true) {
    req.async = async;
  }
  if (typeof data !== 'string') {
    req.data = JSON.stringify(data);
  } else {
    req.data = data;
  }
  let loading;
  if (load !== false) {
    loading = ElementUI.Loading.service(Vue.prototype.loadingOptions);
  }
  $.ajax(req);
}

// 将时间戳（10位）转为 Y-M-D h:m:s格式的过滤器
Vue.filter('timestampTo', function (timestamp) {
  let date = new Date(timestamp * 1000),//时间戳为10位需*1000，时间戳为13位的话不需乘1000
    Y = date.getFullYear() + '-',
    M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-',
    D = date.getDate() + ' ',
    h = date.getHours() + ':',
    m = date.getMinutes() + ':',
    s = date.getSeconds();
  return Y + M + D + h + m + s;
});

Vue.filter('numTo', function (value) {
  if (!value && value !== 0) {
    return '-';
  }
  // 截取当前数据到小数点后两位

  let realVal = parseFloat(value).toFixed(2);

  // num.toFixed(2)获取的是字符串

  return parseFloat(realVal)
});

Vue.filter('toFixed', function (value, num) {
  if (!value) {
    return '-';
  }
  // 截取当前数据到小数点后两位

  let realVal = parseFloat(value).toFixed(num);

  // num.toFixed(2)获取的是字符串

  return parseFloat(realVal)
});


router.beforeEach((to, from, next) => {
  if (to.path === '/') {
    next({path: '/index'});
    return;
  }
  next();
});
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: {
    MyFrame
  },
  template: '<MyFrame/>'
});
