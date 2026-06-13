<template>
  <el-dialog
    ref="dialog"
    :visible.sync="currVisible"
    width="500px"
    :show-close="false"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <el-form :rules="rules" ref="form" class="form" :model="addform" >
      <div class="ft-danger">*请直接将员工工号粘贴到下面框里，每个一行</div>
      <el-form-item label="" prop="badges">
        <el-input type="textarea" class="staffs" v-model="addform.badges" placeholder="请输入" clearable></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
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
      addform: {
        badges:''
      },
      rules: {
        badges: [tce.helper.formRules.vempty()]
      },
      refresh: [],
    }
  },
  props: {
    visible: Boolean
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
    /**
     * 添加
     */
    async addSubmit(){
      await this.validateForm()
      let arr = this.addform.badges.split(/[\s\n,]/)
      let arr2 = arr.filter(el=>{
        return el !=""
      })
      this.$emit('refresh', arr2)
      this.close()
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.addform = {
        badges:''
      }
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.addform = {
        badges:''
      }
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
  .form{
    margin-bottom: 40px;
  }
  .staffs ::v-deep {
    margin-top: 20px;
    textarea{
      min-height: 150px !important;
    }
  }
</style>
