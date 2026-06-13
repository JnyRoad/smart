<template>
  <el-dialog
    ref="dialog"
    :title="title"
    :visible.sync="currVisible"
    width="900px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <el-form :rules="rules" ref="form" :model="addform" label-width="130px">
      <el-form-item label="授权有效期设置" prop="validTimeStart">
        <div>
          <el-radio-group v-model="isLong">
            <el-radio :label="1">指定时间</el-radio>
            <el-radio :label="2">长期有效</el-radio>
          </el-radio-group>
          <el-date-picker
            v-if="isLong===1"
            v-model="times"
            type="datetimerange"
            :picker-options="pickerOptions"
            range-separator="-"
            :default-time="['00:00:00', '23:59:59']"
            value-format="yyyy-MM-dd HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            clearable>
          </el-date-picker>
        </div>
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { authApi } from '../_service'
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    var vPeriodTime = (rule, value, callback) => {
      if (this.isLong ===1 ) {
        if(!value){
          callback(new Error('请选择时间'))
        }else{

          callback()
        }
      } else {
        callback()
      }
    }
    return {
      btnLoading: false,
      currVisible: false,
      isLong: 2, // 2 长期有效，1 指定时间 （只有开始时间和结束时间同时有值才说指定时间，其他是长期有效）
      times: [],
      addform: {
        id: undefined,
        // personId: undefined,
        // deviceId: undefined,
        validTimeStart: undefined,
        validTimeEnd: undefined
      },
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7;
        }
      },
      rules: {
        validTimeStart: [{ validator: vPeriodTime, trigger: 'change' }]
      },
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
      }
    },
    isLong(val){
      if(val ===2){
        this.times = []
      }
    },
    times(val){
      if(val && val.length>0){
        this.addform.validTimeStart = val[0]
        this.addform.validTimeEnd = val[1]
      }else{
        this.addform.validTimeStart = undefined
        this.addform.validTimeEnd = undefined
      }
    },
    itemObj:{
      handler(newV){
        if( newV && newV.id ){
          this.addform.id = newV.id
          // this.addform.deviceId = newV.deviceId
          // this.addform.personId = newV.personId
          if(newV.validTimeStart && newV.validTimeEnd){
            this.times = [newV.validTimeStart, newV.validTimeEnd]
            this.isLong = 1
          }else{
            this.isLong = 2
          }
        }
      },
      immediate: true
    }
  },
  created(){},
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
      await this.validateForm()
      this.btnLoading = true
      const res =  await authApi.reAuth(this.addform)
      if(res.data.code === 200){
        this.$message.success('重新授权成功！');
        this.refresh()
        this.btnLoading = false
      }else{
        this.btnLoading = false
        this.$message.error(res.data.message);
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

<style lang="scss" scoped>
  .theme-yutong .append-button:active {
      background-color: transparent;
      border-color: transparent;
      color: inherit;
  }
  .theme-yutong .append-button:hover {
      background-color: transparent;
      border-color: transparent;
      color: inherit;
  }
</style>
