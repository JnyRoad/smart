/**
 * 防重复提交
 * @author yang.chuan <yang.chuan@bjtce.com>
 * @date 2020-12-22
 */
import executeOnce from '@/util/executeOnce'
export default {
  data () {
    const that = this
    const executeOnceInstance = executeOnce({
      errorCall ({ message }) {
        that.$message.error(`${message}`)
      },
      showToast ({ message }) {
        that.$message({
          message: `${message}`,
          type: 'success'
        })
      },
      loadingToggle ({ state, loadingText = '正在提交...' }) {
        if (state === 'off') {
          that.executeOnceLoading = false
          return
        }
        that.executeOnceLoading = true
      }
    })
    return {
      executeOnceLoading: false,
      executeOnceInstance: executeOnceInstance,
      executeOnceInstanceDone: executeOnceInstance.executeOnce
    }
  },
  methods: {
    /**
     * 提交
     * @param {*} data
     * @param {*} param1
     */
    async executeOnceSubmit ({ promise, message = '提交成功', finallyCall = null }) {
      if (!this.executeOnceInstanceDone.v()) {
        return
      }
      return new Promise((resolve, reject) => {
        this.executeOnceInstanceDone.done(promise).then((res) => {
          if (res.data.code !== 0) {
            // res.currError(res.data.message)
            this.executeOnceInstance.reset()
            this.$message.error(res.data.message)
            reject(new Error(res.data.message))
          } else {
            res.done(message)
            resolve(res)
          }
        }, (e) => {
          // console.log(e)
          // this.$message.error(`网络连接失败`)
          this.executeOnceInstance.reset()
          reject(e)
        }).finally(() => {
          finallyCall && finallyCall()
        })
      })
    }
  }
}
