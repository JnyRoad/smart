<template>
  <el-dialog
    ref="dialog"
    title="特殊名单充值"
    :visible.sync="currVisible"
    width="500px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <div>
      <el-form ref="form" :model="dataform" :rules="addRule" label-width="80px">
        <el-form-item label="工号" prop="badges">
          <!-- type="textarea" -->
          <el-input v-model="dataform.badges" placeholder="请粘贴工号，以空格隔开"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dataform.remark" placeholder="请填写特殊充值的原因，备注信息"/>
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
import { singleRecharge } from "../recharge_service.js";
import { validatenull } from '@/util/validate'
export default {
  data() {
    var vldBadges = (rule, value, callback) => {
      if(validatenull(value)){
        callback(new Error('请输入'))
      }else{
        callback()
      }
    }
    var vldRemark = (rule, value, callback) => {
      if(validatenull(value)){
        callback(new Error('请输入'))
      }else{
        callback()
      }
    }
    return {
      dataform:{},
      addRule: {
        badges: [
          // { validator: vldBadges, trigger: "blur" }
          { required: true, message: '请输入', trigger: 'blur' },
        ],
        remark: [
          // { validator: vldRemark, trigger: "blur" }
          { required: true, message: '请输入', trigger: 'blur' },
        ]
      },
      btnLoading: false,
      currVisible: false,
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
    test(e){
      // console.log(e)
    },
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
      const res = await singleRecharge(this.dataform)
      if(res.data.code === 0){
        this.$message({
          message: '充值成功',
          type: 'success'
        });
        if(res.data.data){
          this.$alert(res.data.data, '提示', {
            confirmButtonText: '确定',
          });
        }
        this.refresh()
      }else{
        this.$message.error({
          message: res.data.msg,
        });
      }
    },
    refresh() {
      this.$emit('refresh',this.dataform.badges)
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
