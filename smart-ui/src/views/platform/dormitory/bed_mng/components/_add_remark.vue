<template>
  <el-dialog
    ref="dialog"
    title="添加标记"
    :visible.sync="currVisible"
    width="600px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <div>
      <el-form ref="form" :model="dataform" :rules="rules">
        <el-form-item label="标记理由" prop="reasonType">
          <el-select v-model="dataform.reasonType" clearable placeholder="退宿类型">
            <el-option label="出差" :value="1"></el-option>
            <el-option label="请假" :value="2"></el-option>
            <el-option label="其他" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="时间" prop="time">
          <el-date-picker
            v-model="dataform.time"
            type="datetimerange"
            range-separator="-"
            value-format="yyyy-MM-dd HH:mm:ss"
            :default-time="['00:00:00', '23:59:59']"
            start-placeholder="起始时间"
            end-placeholder="截止时间"
            clearable
          ></el-date-picker>
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
import { remarAdd } from "@/api/platform/dormitory/bed_mng"

export default {
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      dataform: {
        reasonType: '',
        dorStaffId: '',
        time : []
      },
      rules: {
        reasonType: [{ required: true, message: "请选择理由", trigger: "change" }],
        time: [{ required: true, message: "请选择时间", trigger: "change" }]
      }
    }
  },
  props: {
    visible: Boolean,
    checkInObj: Object,
    remarkItem: Object
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
        if(this.checkInObj && this.checkInObj.id){
          this.dataform.dorStaffId = this.checkInObj.id
        }
      }
    },
    checkInObj:{
			handler: function(val){},
			immediate: true
		},
    remarkItem:{
			handler: function(val){
        if(val && val.id){
          let obj = Object.assign({}, val)
          this.dataform.id = obj.id
          this.dataform.reasonType = obj.reasonType
          this.dataform.dorStaffId = obj.dorStaffId
          this.dataform.time = [obj.startTime, obj.endTime]
        }else{
          this.dataform.reasonType = ''
          this.dataform.time = []
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
      let obj = Object.assign({
        startTime: this.dataform.time[0],
        endTime: this.dataform.time[1],
      },this.dataform)
      obj.time = ''
      const res = await remarAdd(obj)
      if(res.data.code===0){
        this.$message({
          message: '操作成功',
          type: 'success'
        })
      }else{
        this.$message.error('操作失败')
      }
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
      this.dataform = {
        reasonType: '',
        dorStaffId: '',
        time : []
      }
      this.$emit('refresh')
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
      this.btnLoading = false
      this.dataform = {
        reasonType: '',
        dorStaffId: '',
        time : []
      }
      this.$emit('refresh')
    }
  }
}
</script>

<style lang="scss">

</style>
