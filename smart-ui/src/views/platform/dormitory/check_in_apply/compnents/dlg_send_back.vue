<!--退回-->
<template>
  <el-dialog
    title="退回"
    class="dialog_form send_back"
    width="500px"
    @close="resetSetForm('dataform')"
    :visible.sync="setFormVisible"
  >
    <el-form ref="dataform" :model="dataform" :rules="rules" label-width="50px">
      <el-form-item label="原因" prop="reason">
        <el-radio-group v-model="dataform.reason" class="reason_group">
          <el-radio-button label="当前入住人数过多，已无空闲床位"></el-radio-button>
          <el-radio-button label="已申请过外宿，不能申请宿舍"></el-radio-button>
          <el-radio-button label="其他"></el-radio-button>
        </el-radio-group>
        <el-input
          v-if="dataform.reason=='其他'"
          v-model="reason2"
          placeholder="请输入原因"
        />
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
        >保 存</el-button
      >
    </div>
  </el-dialog>
</template>

<script>
import { failBack } from "../_service.js";
import { mapGetters } from "vuex";
export default {
  name: "",
  data() {
    return {
      setFormVisible: false,
      setLoading: false, //是否正在设置
      reason2: '',
      dataform: {
        reason: ''
      },
      rules: {
        reason: [
          { required: true, message: '请选择原因', trigger: 'change' }
        ],
      }
    };
  },
  props: {
    visible: {
      type: Boolean,
    },
    row: undefined,
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
    resetSetForm(formName) {
      this.setFormVisible = false;
      this.setLoading = false;
      this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    async editSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid){
          let var1 = ''
          if(this.dataform.reason==='其他'){
            if(!this.validatenull(this.reason2)){
              var1 = this.reason2
            }else{
              this.$message.error('请输入原因');
              return
            }
          }else{
            var1 = this.dataform.reason
          }
          let obj = {
            id: this.row.id,
            resultRemark: var1
          }
          failBack(obj).then(res => {
            if(res.data.code==0){
              this.$notify.success({
                title: '成功',
                message: '退回成功'
              });
              this.$emit("dlgdoSuccess");
              this.setFormVisible = false;
            }else{
              this.$notify.error({
                title: '退回失败',
                message: res.data.msg
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
.send_back ::v-deep {
  .reason_group{
    width: 100%;
    .el-radio-button, .el-radio-button__inner{
      display: block;
      margin-bottom: 10px;
    }
    .el-radio-button__inner{
      border: 1px solid #dcdfe6;
    }
    .el-radio-button:first-child .el-radio-button__inner{
      border-radius: 4px;
    }
    .el-radio-button:last-child .el-radio-button__inner{
      border-radius: 4px;
    }
    .el-radio-button .el-radio-button__inner{
      border-radius: 4px;
    }
    .el-radio-button__inner:hover{
      color: #fff;
      background-color: #ed6d00;
    }
    .el-radio-button__orig-radio:checked+.el-radio-button__inner{
      color: #fff;
      background-color: #ed6d00;
      border-color: #ed6d00;
      box-shadow: -1px 0 0 0 #ed6d00;
    }
  }
}
</style>