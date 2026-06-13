const getParentNode = function (node) {
  if (!node.$parent) {
    return null
  }
  if (node.$parent.$options.name === 'business-form-group') {
    return getParentNode(node.$parent)
  }
  if (node.$parent.$options.name === 'business-form') {
    return node.$parent
  }
}

export default {
  data () {
    return {
      formData: null
    }
  },
  props: {
    field: String, // 最终获取得到的数据字段标记key
    label: String, // 显示的字段
    placeholder: { // 显示placeholder
      type: String,
      default: ''
    },
    valueData: [Object, String, Number, Array], // 初始数据,用于回填数据
    required: Boolean, // 是否必填
    requiredMessage: String, // 必填的提示字段
    formOption: Object, // 其他配制
    disable: Boolean
  },
  watch: {
    formData (val) {
      this.syncFormData(val)
    },
    valueData (val) {
      this.initSyncValueData()
    }
  },
  computed: {
    computedRequiredMessage () {
      if (this.requiredMessage) {
        return this.requiredMessage
      }
      return `${this.label}为必填项`
    }
  },
  methods: {
    clearFormData () {
      this.formData = null
    },
    /**
     * form 同步数据
     * @param {Object} val
     */
    syncFormData (val) {
      if (!this.field) {
        throw new Error('field 无配制')
      }
      this.$formRoot.setFormData(this.field, val)
    },
    /**
     * form 提示展示
     * @param {Object} mes
     */
    syncshowToast (mes) {
      this.$formRoot.showVisibleMessage(mes)
    },
    /**
     * 非空校验
     */
    verifyEmpty () {
      if (this.formData === null || this.formData === undefined || this.formData === '') {
        return this.verifyFail(this.computedRequiredMessage)
      }
      if (Object.prototype.toString.call(this.formData) === '[object Array]' && this.formData.length === 0) {
        return this.verifyFail(this.computedRequiredMessage)
      }
      if (Object.prototype.toString.call(this.formData) === '[object Object]' && Object.keys(this.formData).length ===
        0) {
        return this.verifyFail(this.computedRequiredMessage)
      }
      return this.verifySuccess('')
    },
    /**
     * 校验失败
     * @param {Object} error
     */
    verifyFail (error) {
      if (error) {
        this.syncshowToast(error)
      }
      return Promise.reject(error)
    },
    /**
     * 校验成功
     * @param {Object} mes
     */
    verifySuccess (mes) {
      return Promise.resolve(mes)
    },
    initSyncValueData () {
      if (this.valueData || this.valueData === 0) {
        this.formData = this.valueData
        this.syncValue && this.syncValue(this.valueData)
      }
    }
  },
  created () {
    this.initSyncValueData()
  },
  mounted () {
    this['$formRoot'] = getParentNode(this)
    this.$formRoot.pushRequiredNode(this)
  },
  beforeDestroy () {
    this.formData = null
    this['$formRoot'] = null
  }
}
