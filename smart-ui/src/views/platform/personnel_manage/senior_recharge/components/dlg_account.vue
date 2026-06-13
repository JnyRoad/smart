<template>
  <el-dialog
    ref="dialog"
    title="修改应发餐补"
    :visible.sync="currVisible"
    width="500px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <div>
      <el-form ref="form" :model="dataform" :rules="addRule" label-width="80px">
        <el-form-item label="应发餐补" prop="account">
          <el-input v-model.number="dataform.account" placeholder="请输入"/>
        </el-form-item>
        <el-form-item label="备注" prop="blank">
          <el-input v-model="dataform.blank" placeholder="请输入"/>
        </el-form-item>
      </el-form>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { updateRecharge } from "../recharge_service.js";
export default {
  data() {
    return {
      dataform:{},
      addRule: {
        account: [
          { required: true, message: '请输入', trigger: 'blur' },
          { type: 'number', message: '请输入数字', trigger: 'blur' },
        ],
        blank: [
          { required: true, message: '请输入', trigger: 'blur' },
        ]
      },
      btnLoading: false,
      currVisible: false,
    }
  },
  props: {
    visible: Boolean,
    row: Object
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
    async formSumit() {
      await this.validateForm()
      if(this.row && this.row.id){
        this.dataform.id = this.row.id
        const res = await updateRecharge(this.dataform)
        this.$message({
          message: '修改成功',
          type: 'success'
        });
        this.refresh()
      }else{
        this.$message.error('获取当前记录信息异常');
      }
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss">

</style>
