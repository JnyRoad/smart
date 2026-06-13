<template>
  <el-dialog ref="dialog" :visible.sync="currVisible" width="500px" :show-close="false" @open="open" @close="close" :close-on-click-modal="false" :append-to-body="true" :custom-class="'my-dialog-'">
    <el-form ref="form" class="form">
      <div class="ft-danger">*请直接将员工工号粘贴到下面框里，每个一行，最多输入200个</div>
      <el-form-item label="">
        <el-input type="textarea" class="staffs" v-model="badges" placeholder="请输入" clearable></el-input>
      </el-form-item>
      <div style="text-align: right;">{{rowsNum}}/200</div>
    </el-form>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="resetFrom()" plain>清空</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      badges: '',
      rowsNum: 0
    }
  },
  props: {
    visible: Boolean,
    dataItem: String
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      }
    },
    dataItem(val){
    },
    badges(newValue, oldValue){
      if (this.getAddRow() > 200) {
        this.badges = oldValue
        this.$message({
          message: '一次最多输入200个',
          type: 'warning'
        });
      }else{
        this.rowsNum = this.getAddRow()
      }
    }
  },
  methods: {
    /**
     * 验证表单
     */
    validateForm() {
      if (this.$refs.form) {
        return this.$refs.form.validate()
      }
      return Promise.resolve()
    },
    /**
     * 提交
     */
    async formSumit() {
      this.addSubmit()
    },
    getAddRow() {
      let row = 0
      this.badges.split("\n").forEach(item => {
          if (item.length === 0) {
              row += 1
          } else {
              row += Math.ceil(item.replace(/[\u0391-\uFFE5]/g,"aa").length / 20)
          }
      })
      return row
    },
    /**
     * 添加
     */
    async addSubmit() {
      // console.log(this.addform.badges)
      this.$emit('refresh', this.badges)
      this.currVisible = false
    },
    resetFrom(){
      this.badges = ''
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.badges = ''
      this.currVisible = false
    },
    open() {
      this.currVisible = true
      this.badges = this.dataItem
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.badges = ''
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
.form {
  margin-bottom: 40px;
}
.staffs ::v-deep {
  margin-top: 20px;
  textarea {
    min-height: 150px !important;
  }
}
</style>
