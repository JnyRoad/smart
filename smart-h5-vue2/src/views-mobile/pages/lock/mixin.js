export default {
  inject: ['formScope'],
  data () {
    return {
      formData: null
    }
  },
  props: {
    field: String,
    label: String, // 显示的字段
    placeholder: { // 显示placeholder
      type: String,
      default: ''
    },
    valueData: [Object, String, Number, Array], // 初始数据,用于回填数据
    required: Boolean,
    requiredMessage: String,
    formOption: Object,
    disable: Boolean,
    readonly: Boolean
  },
  watch: {
    formData (val) {
      this.syncFormData(val)
    },
    valueData (val) {
      if (val || val === 0) {
        this.formData = val
        this.syncValue && this.syncValue(val)
      }
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
      this.formScope.$formRoot.setFormData(this.field, val)
    },
    /**
     * form 提示展示
     * @param {Object} mes
     */
    syncshowToast (mes) {
      this.formScope.$formRoot.showVisibleMessage(mes)
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
    }
  },
  created () {
    if (this.valueData || this.valueData === 0) {
      this.formData = this.valueData
      this.syncValue && this.syncValue(this.valueData)
    }
  },
  mounted () {
    this.formScope.$formRoot.pushRequiredNode(this)
  },
  beforeDestroy () {
    this.formData = null
  }
}
