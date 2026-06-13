<!--公摊抄表-新增抄表-->
<template>
  <el-dialog
    :title="mTitle"
    class="dialog_form"
    width="500px"
    @close="resetSetForm('dataform')"
    :visible.sync="setFormVisible"
  >
    <el-form ref="dataform" :model="dataform" :rules="addRule" label-width="110px">
      <el-form-item label="抄表月份" prop="meterMonth">
        <el-date-picker
          v-model="dataform.meterMonth"
          type="month"
          placeholder="选择抄表月份"
          value-format="yyyy-MM"
          @change="getMonthDetail('m')"
        >
        </el-date-picker>
      </el-form-item>
      <el-form-item label="上月表盘数值" prop="preMonthNum">
        <el-input v-model="dataform.preMonthNum" :disabled="dataform.statementStatus==1"/>
      </el-form-item>
      <el-form-item label="当前表盘数值" prop="curMonthNum">
        <el-input v-model="dataform.curMonthNum" :disabled="dataform.statementStatus==1"/>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="resetSetForm('dataform')" plain
        >取 消</el-button
      >
      <el-button
        type="primary"
        @click="editSubmit('dataform')"
        :loading="setLoading"
        :disabled="dataform.statementStatus==1"
        >保 存</el-button
      >
    </div>
  </el-dialog>
</template>

<script>
import { meterreadSave, meterreadDetail } from "../_service.js";
import { validatenum } from "@/util/validate";
import { mapGetters } from "vuex";
export default {
  name: "",
  data() {
    return {
      setFormVisible: false,
      setLoading: false, //是否正在设置
      dataform: {
        meterMonth: '',
        curMonthNum: '',
        preMonthNum: '',
        mrId: '',
        statementStatus: ''
      },
      addRule: {
        meterMonth: [
          { required: true, message: '请选择抄表月份', trigger: 'blur' }
        ],
        preMonthNum: [
          { required: true, message: '请输入上月表盘数值', trigger: 'blur' },
        ],
        curMonthNum: [
          { required: true, message: '请输入当前表盘数值', trigger: 'blur' },
        ],
      }
    };
  },
  props: {
    mTitle: {
      type: String,
      default: function(){
        return '新增抄表'
      }
    },
    visible: {
      type: Boolean,
    },
    commonId: undefined,
    row: undefined
  },
  watch: {
    visible(newVal, oldVal) {
      this.setFormVisible = newVal;
    },
    setFormVisible(newVal, oldVal) {
      if (newVal === false) {
        this.$emit("dlgdo", newVal);
      }
    },
    row:{
			handler: function(val){
        if(val&&val.sdId){
          this.getDetail();
        }else{
          this.initForm()
        }
      },
			immediate: true
		}
  },
  created() {
    this.initData();
  },
  mounted: function () {},
  computed: {},
  methods: {
    initData() {
      this.setFormVisible = this.visible;
    },
    async getDetail() {
      if(this.row&&this.row.sdId){
        let obj = Object.assign({},this.row)
        this.dataform = {
          meterMonth: obj.meterMonth,
          curMonthNum: obj.sdCategory.curMonthNum,
          preMonthNum: obj.sdCategory.preMonthNum+"",
          mrId: obj.sdCategory.mrId,
          statementStatus: obj.statementStatus
        }
      }
    },
    async getMonthDetail() {
      const res = await meterreadDetail({
        id: this.commonId,
	      meterMonth: this.dataform.meterMonth
      })
      if(res.data.code==0){
        if(res.data.data.sdCategory){
          this.$set(this.dataform, 'preMonthNum', res.data.data.sdCategory.preMonthNum)
          this.$set(this.dataform, 'curMonthNum', res.data.data.sdCategory.curMonthNum)
          this.$set(this.dataform, 'statementStatus', res.data.data.statementStatus)


        }else{
          this.$set(this.dataform, 'preMonthNum', '')
          this.$set(this.dataform, 'curMonthNum', '')
          this.$set(this.dataform, 'statementStatus', '')

        }
      }
    },
     initForm(){
      this.dataform= {}
    },
    resetSetForm(formName) {
      this.setFormVisible = false;
      this.setLoading = false;
      this.initForm()
      // this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    async editSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid){
          if(Number(this.dataform.preMonthNum)>Number(this.dataform.curMonthNum)){
            this.$message.error('当前表盘数值应大于上月表盘数值');
            return
          }
          this.dataform.commonId = this.commonId
          meterreadSave(this.dataform).then(res => {
            if(res.data.code==0){
              this.resetSetForm(formName)
              this.$emit("dlgdoSuccess");
              this.$notify({
                title: '成功',
                message: this.mTitle,
                type: 'success'
              });
            }
          });
        } else {
          return false
        }
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.plcd_form ::v-deep {
  .el-dialog__body {
    padding: 10px 0 0 0;
  }
  .el-form--inline .el-form-item{
    margin-right: 0;
  }
  .el-form--inline .m_ipt .el-form-item__content{
    width: 250px;
  }
  .sdcb_tp {
    border-bottom: 10px solid #f0f2f5;
    padding: 0 20px;
    >div{
      display: flex;
      justify-content: space-between;
    }
  }
  .sdcb_btm {
    padding: 10px 20px 50px;
    .sdcb_tb{
      .sdcbt_row, .sdcbt_t{
        display: flex;
        height: 40px;
        div{
          line-height: 40px;
          text-align: center;
        }
        div:not(.c1){
          flex: 1;
        }
        .c1{
          width: 90px;
        }
      }
      .sdcbt_row{
        height: 50px;
        .row_ipt{
          width: 150px;
        }
        .el-input--mini .el-input__inner{
          text-align: center;
          background: #fafafa;
          border: 1px solid #dcdfe6;
          border-radius: 0;
        }
      }
    }
    .tip{
      font-size: 14px;
      color: #999;
      margin-top: 10px;
    }
  }
}
</style>
