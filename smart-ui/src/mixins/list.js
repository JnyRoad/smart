const defaultListOptionConf = {
  index: true,
  indexLabel: '序号',
  addBtn: false,
  delBtn: false,
  editBtn: false,
  viewBtn: false,
  border: false,
  refreshBtn: false,
  columnBtn: false,
  stripe: false,
  page: true,
  align: 'center',
  menuAlign: 'center',
  menu: true,
  selection: false,
  tip: false,
  column: []
}
export default {
  data: function () {
    return {
      mixinPage: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 10 // 每页显示多少条
      },
      mixinSearchForm: {},
      mixinTableLoading: false
    }
  },
  computed: {
    mixinListOptionConf () {
      if (this.listOption && this.listOption.column) {
        this.listOption.column.forEach(item => {
          if (!item.formatter && !item.slot) {
            item.formatter = function (row, value) {
              if (!value && value !== 0) {
                return item.formatterLine || '-'
              }
              return value
            }
          }
        })
      }
      return Object.assign({}, defaultListOptionConf, this.listOption)
    },
  },
  watch: {

  },
  methods: {
    /**
     * 搜索回调
     */
    mixinSearchSubmit(form) {
      this.mixinPage.currentPage = 1
      this.getList(this.mixinPage, form)
    },
    /**
     * 改变size
     */
    mixinSizeChange(val) {
      this.mixinPage.currentPage = 1
      this.mixinPage.pageSize = val
      this.getList(this.mixinPage)
    },
    /**
     * 点击页码
     */
    mixinCurrentChange(val) {
      this.mixinPage.currentPage = val
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    /**
     * 清空搜索
     */
    mixinResetFrom(formName) {
      this.$refs[formName] && this.$refs[formName].resetFields()
      this.mixinPage.currentPage = 1
      this.getList(this.mixinPage)
    },
    /**
     * 删除，确认
     */
    mixinMsgDel(msg){
      const elm = this.$createElement;
      return this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, msg)
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
    },
  },
  mounted () {

  },
  activated () {

  },
  beforeDestroy () {
  }
}
