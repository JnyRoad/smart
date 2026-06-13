<!--退宿-->
<template>
  <el-dialog
    title="退宿"
    class="dialog_form"
    width="500px"
    @close="resetSetForm('dataform')"
    :visible.sync="setFormVisible"
  >
    <el-form ref="dataform" :model="dataform" label-width="100px">
      <el-form-item label="退宿类型" prop="type">
        <el-select v-model="dataform.type" clearable placeholder="退宿类型">
          <el-option label="外宿" :value="2"></el-option>
          <el-option label="离职" :value="3"></el-option>
          <el-option label="自离" :value="5"></el-option>
        </el-select>
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
import { checkOut } from "@/api/platform/dormitory/bed_mng";
import { validatenum } from "@/util/validate";
import { mapGetters } from "vuex";
export default {
  name: "",
  data() {
    return {
      setFormVisible: false,
      setLoading: false, //是否正在设置
      dataform: {
      },
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
          this.dataform.id = this.row.inRecordId
          checkOut(this.dataform).then(res => {
            if(res.data.code==0){
              this.$notify.success({
                title: '成功',
                message: '退宿成功'
              });
              this.$emit("dlgdoSuccess");
              this.setFormVisible = false;
            }else{
              this.$notify.error({
                title: '退宿失败',
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