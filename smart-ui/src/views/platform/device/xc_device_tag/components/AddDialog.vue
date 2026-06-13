<template>
  <el-dialog
    ref="dialog"
    :title="title"
    :visible.sync="currVisible"
    width="500px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <el-form :rules="rules" ref="form" class="form" :model="addform" label-width="80px">
      <el-form-item label="标签名称" prop="tagName">
        <el-input v-model="addform.tagName" placeholder="请输入" clearable></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { xcDeviceTagApi } from '../_service'
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      addform: {},
      rules: {
        tagName: [tce.helper.formRules.vempty()]
      }
    }
  },
  props: {
    visible: Boolean,
    title: String,
    itemObj: Object
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      } else {
        if(this.itemObj && this.itemObj.id){
          this.addform = Object.assign({},this.itemObj)
          if(this.addform.targetParkId){
            this.addform.targetParkId = Number(this.addform.targetParkId)
          }
        }else{
          this.addform = {}
        }
      }
    },
    itemObj:{
      handler(){},
      immediate: true
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
      if(this.itemObj && this.itemObj.id){
        this.editSubmit()
      }else{
        this.addSubmit()
      }
    },
    /**
     * 添加
     */
    async addSubmit(){
      await this.validateForm()
      await this.executeOnceSubmit({
        promise: xcDeviceTagApi.addObj(this.addform)
      })
      this.refresh()
    },
    /**
     * 编辑
     */
    async editSubmit(){
      await this.validateForm()
      let obj = {
        tagName: this.addform.tagName
      }
      await this.executeOnceSubmit({
        promise: xcDeviceTagApi.editObj(obj, this.addform.id)
      })
      this.refresh()
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.addform = {}
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.addform = {}
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
</style>
