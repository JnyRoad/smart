<!--修改均摊人天-->
<template>
  <el-dialog
    class="dialog_form plcd_form"
    ref="dialog"
    title="修改入住人天"
    :visible.sync="currVisible"
    width="800px"
    @open="open"
    @close="close"
    :append-to-body="true"
  >
    <div class="sdcb_btm">
      <el-form ref="addform" :inline="true" :model="addForm" size="mini">
      <template v-if="addForm.records&&addForm.records.length>0">
        <div class="in-total">入住总天数：{{row.totalStayDays}}</div>
        <div class="in-tip">* 入住总天数，等于此房间所有人的入住天数之和，结算前可修改</div>
        <div class="sdcb_tb">
          <div class="sdcbt_t">
            <div class="cateRow">
              <div>工号-姓名</div>
              <div>入住天数</div>
              <div>修改原因</div>
            </div>
          </div>
          <template v-for="(item, index) in addForm.records">
            <div class="sdcbt_row" :key="index">
              <div class="cateRow">
                <div>
                  {{item.staffBadge}}-{{item.staffName}}
                </div>
                <div>
                  <!-- {{item.inTime}} -->
                  <el-form-item class="row_ipt"
                    :prop="'records.'+index+'.tayDays'"
                    :disabled="item.statementDate"
                  >
                    <el-input v-model="item.tayDays"/>
                  </el-form-item>
                </div>
                <div>
                  <!-- :rules="{ required: true, message: '请输入入住天数', trigger: 'blur' }" -->
                  <el-form-item class="row_ipt row_ipt2"
                    :prop="'records.'+index+'.remark'"
                    :disabled="item.remark"
                  >
                    <el-input type="textarea" v-model="item.remark" />
                  </el-form-item>
                </div>
              </div>
            </div>
          </template>
        </div>
      </template>
      <template v-else>
        <div class="tips">{{tip}}</div>
      </template>
      </el-form>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="cancel" plain
        >取 消</el-button
      >
      <el-button
        type="primary"
        v-if="addForm.records&&addForm.records.length>0"
        @click="editSubmit('addform')"
        :loading="btnLoading"
        >保 存</el-button
      >
    </div>
  </el-dialog>
</template>

<script>
import { queryStayInfo, updateStayInfo } from "../_service";

export default {
  name: "",
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      addForm: {
        records: []
      },
      tip: '当前无记录！',
    };
  },
  props: {
    visible: {
      type: Boolean,
    },
    row: undefined,
  },
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      }
    },
    row:{
      handler:function(newVal, oldVal) {
        if(newVal&&newVal.id){
          this.getDetail(newVal.id);
        }else{
          this.addForm.records = []
        }
      },
      immediate: true
    }
  },
  created() {
  },
  mounted: function () {},
  computed: {},
  methods: {
    async getDetail(e) {
      const res = await queryStayInfo( {mrId: e} )
      if(res.data.code==0){
        this.addForm.records = res.data.data
        if(this.addForm.records.length==0){
          this.tip="当前条件下，没有相关记录！"
        }
      }else{
        this.addForm.records = []
        this.$message.error(res.data.msg);
        this.tip="当前条件下，没有相关记录！"
      }
    },
    resetSetForm(formName) {
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    async editSubmit(formName) {
      await this.$refs[formName].validate(valid =>{
        if (valid) {
          this.btnLoading = true
          updateStayInfo(this.addForm.records, this.row.id).then(res=>{
            if(res.data.code==0){
              this.$notify({
                title: '成功',
                message: '修改成功',
                type: 'success'
              });
              this.refresh()
              this.resetSetForm(formName)
            }
            this.btnLoading = false
          }).catch(err=>{
            this.btnLoading = false
            return err
          })
        } else {
          return false;
        }
      });
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
      this.addForm.records = []
      this.currVisible = false
    }
  },
};
</script>
<style lang="scss" scoped>
.plcd_form ::v-deep {
  .in-total{
    margin-bottom: 10px;
  }
  .in-tip{
    font-size: 12px;
  }

  .tips{
    padding-top: 30px;
    text-align: center;
    color: #999;
  }
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
    .row-line{
      display: flex;
      justify-content: space-between;
    }
  }
  .sdcb_btm {
    padding: 10px 20px 50px;
    .sdcb_tb{
      margin-top: 10px;
      .sdcbt_row, .sdcbt_t{
        display: flex;
        // line-height: 40px;
        div{
          text-align: center;
        }
        // div:not(.c1){
        //   flex: 1;
        // }
        .c1{
          width: 90px;
        }
        .r-item{
          display: flex;
        }
        .cateRow{
          flex: 1;
          display: flex;
          >div{
            flex: 1;
            box-sizing: border-box;
            padding: 13px 0;
            border: 1px solid #e0e0e0;
            border-right: none;
            border-bottom: none;
          }
          >div:last-child{
            border-right: 1px solid #e0e0e0;
          }
        }
      }
      .sdcbt_row:last-child .cateRow{
        border-bottom: 1px solid #e0e0e0;
      }
      .sdcbt_row ::v-deep {
        // height: 50px;
        .row_ipt{
          width: 150px;
          margin-bottom: 0;
        }
        .el-input--mini .el-input__inner{
          text-align: center;
          background: #fafafa;
          border: 1px solid #dcdfe6;
          border-radius: 0;
        }
        .row_ipt2{
          width: 230px;
          .el-form-item__content{
            width: 100%;
          }
        }
        .el-textarea__inner{
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