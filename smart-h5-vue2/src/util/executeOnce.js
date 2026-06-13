/**
 * 只有一次提交
 * @param {Object} submitCall 提交表单函数，参数可以使用闭包
 */
import toast from '@/components/toast/main.js'
const myAlert = function (message) {
  toast(message.message)
}

const loading = function (message) {
  // console.log(message || '默认loading')
}

const ExecuteOnce = function (conf = {}) {
  this.formComplete = true
  this.formLoadText = '提交中...'
  this.formErrorText = '提交失败'
  this.isLoad = true
  this.executeOnce = null
  this.showToast = conf.showToast || myAlert
  this.loadingToggle = conf.loadingToggle || function ({ state }) {
    if (state === 'off') {
      loading('off')
      return
    }
    loading('on')
  } // 这个方法必须是Toggle自动关闭 不然可能出现严重bug
  this.errorCall = conf.errorCall || myAlert

  if (conf.formLoadText) {
    this.formLoadText = conf.formLoadText
  }
  if (conf.formErrorText) {
    this.formErrorText = conf.formErrorText
  }
  if (conf.formErrorText === false) {
    this.isLoad = false
  }
  this.init()
}

ExecuteOnce.prototype.reset = function () {
  this.formComplete = true
}

ExecuteOnce.prototype.loading = function ({ loadingText }) {
  if (this.isLoad) {
    this.loadingToggle({ state: 'on', loadingText: loadingText })
  }
}
ExecuteOnce.prototype.hideLoading = function () {
  if (this.isLoad) {
    this.loadingToggle({ state: 'off' })
  }
}

ExecuteOnce.prototype.done = function () {
  this.reset()
  this.hideLoading()
}

ExecuteOnce.prototype.init = function () {
  const that = this
  this.executeOnce = {
    v: () => {
      return that.formComplete
    },
    error: (errorInfo) => {
      if (errorInfo) {
        this.errorCall({ message: errorInfo })
      }
      that.reset()
      throw new Error(errorInfo)
    },
    loading: (loadingText) => {
      that.loading({ loadingText: loadingText })
    },
    done: async (done, { loadingText = '提交中...' } = {}) => {
      if (!that.formComplete) {
        throw new Error('repeat')
      }
      that.formComplete = false
      that.loading({ loadingText: loadingText })
      try {
        let res = await done
        res.done = function (msg) {
          return new Promise((resolve, reject) => {
            if (msg) {
              that.showToast({ message: msg })
            }
            that.hideLoading()
            resolve()
            that.reset()
            res.done = null
            res = null
          })
        }
        res.currError = function (msg) {
          return new Promise((resolve, reject) => {
            if (msg) {
              that.errorCall({ message: msg })
            }
            that.hideLoading()
            resolve()
            that.reset()
            res.done = null
            res = null
          })
        }
        return res
      } catch (e) {
        that.errorCall('操作失败')
        that.reset()
        throw new Error(e)
      }
    }
  }
}

export default function (res) {
  const cu = new ExecuteOnce(res)
  return cu
}
