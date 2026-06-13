<template>
  <el-dialog
    ref="dialog"
    title="退宿"
    :visible.sync="currVisible"
    width="600px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <div>
      <el-form ref="form" :model="dataform" :rules="rules">
        <el-form-item label="退宿类型" prop="type">
          <el-select v-model="dataform.type" clearable placeholder="退宿类型">
            <el-option label="外宿" value="2"></el-option>
            <el-option label="离职" value="3"></el-option>
            <el-option label="自离" value="5"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { checkOut } from "@/api/platform/dormitory/bed_mng"
export default {

  data() {
    return {
      btnLoading: false,
      currVisible: false,
      dataform: {
        id: "",
        type: ""
      },
      rules: {
        type: [{ required: true, message: "请选择退宿类型", trigger: "change" }]
      }
    }
  },
  props: {
    visible: Boolean,
    dataItem: Object
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
    dataItem:{
			handler: function(val){
        if(val && val.id){
          this.dataform.id = val.id
        }
      },
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
    async formSumit() {
      await this.validateForm()
      this.btnLoading = true
      const res = await checkOut(this.dataform)
      this.btnLoading = false
      this.refresh()
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
      this.btnLoading = false
      this.$emit('refresh')
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
      this.btnLoading = false
      this.$emit('refresh')
    }
  }
}
</script>

<style lang="scss">

</style>
