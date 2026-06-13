/* eslint-disable */
import Vue from 'vue'
import index from './index.vue'
const ToastConstructor = Vue.extend(index)
let instance
let instances = []
const Toast = function (message) {
  if (instances.length) {
    instance = instances[0]
    instance.message = message
    // console.log(message)
    // console.log(instance)
    return instance
  } else {
    instance = new ToastConstructor()
  }
  instance.message = message
  instance.$mount()
  document.body.appendChild(instance.$el)
  instance.onClose = function () {
    Toast.closeAll()
  }
  instance.visible = true
  // console.log(instance.$el)
  // instance.$el.style.zIndex = 99999
  instances.push(instance)
  return instance
}

Toast.closeAll = function () {
  if (instances.length) {
    instances = []
  }
}

export default Toast
