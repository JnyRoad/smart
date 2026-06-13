import Vue from 'vue';
import Router from 'vue-router';

import Index from '../components/Index.vue';
import FaceDetect from '../components/FaceDetect.vue';
import Ocr from '../components/Ocr.vue';
import Liveness from '../components/Liveness.vue';
import Compare from '../components/Compare.vue';

import App from '../App.vue';

import "babel-polyfill";

Vue.use(Router);


export default new Router({
  routes: [
    {
      path: '/',
      name: 'App',
      component: App,
      children: [
        {
          path: 'index',
          name: 'Index',
          component: Index
        },
        {
          path: 'face/detect',
          name: 'FaceDetect',
          component: FaceDetect
        },
        {
          path: 'ocr',
          name: 'Ocr',
          component: Ocr
        },
        {
          path: 'liveness',
          name: 'Liveness',
          component: Liveness
        },
        {
          path: 'compare',
          name: 'Compare',
          component: Compare
        }
      ]
    }
  ]
})
