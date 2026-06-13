/**
 * 统一的表单相关操作
 */
import executeOnce from '@/util/executeOnce'
import { clone } from '@tce/tce-util'
export default {
  data: function () {
    const that = this
    let loading = null
    const executeOnceCurr = executeOnce({
      errorCall ({ message }) {
        that.$tceMobile.toast(`${message}`)
      },
      showToast ({ message }) {
        that.$alert.success(`${message}`)
      },
      loadingToggle ({ state, loadingText = '正在提交...' }) {
        // console.log(state)
        if (state === 'off') {
          that.$loading.hide()
          if (loading) {
            loading = null
          }
          return
        }
        loading = that.$loading.show(loadingText)
      }
    })
    return {
      mixinForm: {
        executeOnceCurr: executeOnceCurr, // 防重复提交原始对象，主要用于重置对象
        executeOnceSelect: executeOnceCurr.executeOnce, // 防重复提交构造方法{v,done,error,loading}
        promiseForms: {
          add: null,
          update: null,
          delete: null,
          get: null
        }, // promise对象集合{add,update,delete,get}
        rules: {}, // 验证对象
        message: {
          ok: '操作成功',
          warn: '无法完成弄些操作',
          error: '操作失败'
        },
        mixinFormAddCall () {
          // console.log('add 回调')
        },
        mixinFormUpdateCall () {
          // console.log('update 回调')
        },
        mixinFormDeleteCall () {
          // console.log('delete 回调')
        },
        mixinFormGetCall () {
          // console.log('get 回调')
        },
        data: {} // 提交数据
      }
    }
  },
  computed: {
    getMixinForm () {
      return this.mixinForm
    },
    cloneMixinFormData () { // 克隆 mixinFormData.data
      return clone(this.mixinForm.data)
    },
    originalMixinFormData () { // 原始 mixinFormData.data
      return this.mixinForm.data
    }
  },
  watch: {

  },
  methods: {
    /**
     * 添加
     * @param {*} data
     * @param {*} param1
     */
    async addSubmit (data, { message = '新增成功' } = {}) {
      const res = await this.mixinForm.executeOnceSelect.done(this.getMixinForm.promiseForms.add(data)).catch(() => { })
      if (!res) {
        this.mixinForm.executeOnceCurr.done()
        return
      }
      if (res && res.data.code === 1 && res.data.data) {
        res.done(message)
        this.mixinForm.mixinFormAddCall(res.data)
      } else {
        res.currError && res.currError(res.data.message || this.getMixinForm.message.error)
      }
    },
    /**
     * 修改
     * @param {*} data
     * @param {*} param1
     */
    async updateSubmit (data, { message = '更新成功' } = {}) {
      const res = await this.mixinForm.executeOnceSelect.done(this.getMixinForm.promiseForms.update(data)).catch(() => { })
      if (!res) {
        this.mixinForm.executeOnceCurr.done()
        return
      }
      if (res && res.data.code === 1 && res.data.data) {
        res.done(message)
        this.mixinForm.mixinFormUpdateCall(res.data)
      } else {
        res.currError && res.currError(res.data.message || this.getMixinForm.message.error)
      }
    },
    /**
     * 删除
     * @param {*} data
     * @param {*} param1
     */
    async deleteSubmit (data, { message = '删除成功', confirMessage = '是否继续?' } = {}) {
      const t = await this.$confirm({
        content: confirMessage,
        title: '提示'
      })
      if (!t) {
        return
      }
      const res = await this.mixinForm.executeOnceSelect.done(this.getMixinForm.promiseForms.delete(data)).catch(() => { })
      if (!res) {
        this.mixinForm.executeOnceCurr.done()
        return
      }
      if (res && res.data.code === 1 && res.data.data) {
        res.done(message)
        this.mixinForm.mixinFormDeleteCall(res.data)
      } else {
        res.currError && res.currError(res.data.message || this.getMixinForm.message.error)
      }
    },
    /**
     * 获取数据详情
     * @param {*} data
     * @param {*} param1
     */
    async getSubmit (data, { message = '' } = {}) {
      const res = await this.mixinForm.executeOnceSelect.done(this.getMixinForm.promiseForms.get(data), { loadingText: '加载中...' }).catch(() => { })
      if (!res) {
        this.mixinForm.executeOnceCurr.done()
        return
      }
      if (res && res.data.code === 1 && res.data.data) {
        res.done(message)
        this.mixinForm.mixinFormGetCall(res.data)
      } else {
        res.currError && res.currError(res.data.message || this.getMixinForm.message.error)
      }
      return res.data
    },
    /**
     * 其他提交
     * @param {*} data
     * @param {*} param1
     */
    async otherSubmit (data, { promise = null, message = '', callback = function () { } }) {
      if (promise) {
        const res = await this.mixinForm.executeOnceSelect.done(promise(data)).catch(() => { })
        if (!res) {
          this.mixinForm.executeOnceCurr.done()
          return
        }
        if (res && res.data.code === 1 && res.data.data) {
          res.done(message)
          callback(res.data)
        } else {
          res.currError && res.currError(res.data.message || this.getMixinForm.message.error)
        }
        return res.data
      }
      return null
    }
  }
}
