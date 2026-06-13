<template>
  <div>

    <el-form ref="form" :model="dataform" :inline="true" :rules="rules" label-width="80px">
      <el-form-item label="工号" prop="staffBadge">
        <el-input placeholder="请输入工号" v-model="dataform.staffBadge" style="width:240px" @keypress.native.enter="selectStaffDetail('form')">
          <el-button
            type="info"
            slot="append"
            @click="selectStaffDetail('form')"
            class="noPading"
            icon="el-icon-search"
          ></el-button>
        </el-input>
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input placeholder="无需输入，由工号关联" style="width:240px" v-model="currentStaff.name" disabled></el-input>
      </el-form-item>
      <el-form-item label="所属BU" prop="compId">
        <el-input placeholder="无需输入，由工号关联" style="width:240px" v-model="currentStaff.compName" disabled></el-input>
      </el-form-item>
      <el-form-item label="所属部门" prop="depId">
        <el-input placeholder="无需输入，由工号关联" style="width:240px" v-model="currentStaff.depName" disabled></el-input>
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-input placeholder="无需输入，由工号关联" style="width:240px" v-model="currentStaff.sex" disabled></el-input>
      </el-form-item>
      <el-form-item label="员工状态" prop="statusDesc">
        <el-input placeholder="无需输入，由工号关联" style="width:240px" v-model="currentStaff.statusDesc" disabled></el-input>
      </el-form-item>
      <el-form-item label="入住日期" prop="createTime">
        <el-date-picker
        style="width:240px!important"
          v-model="dataform.createTime"
          type="date"
          value-format="yyyy-MM-dd HH:mm:ss"
          placeholder="请选择入住日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="备注" prop="simpleRemark">
        <el-input
        style="width:240px"
        type="textarea"
        maxlength="20"
        placeholder="20字以内"
        v-model="currentStaff.simpleRemark"
        show-word-limit="true"></el-input>
      </el-form-item>
    </el-form>
    <div class="tips">
      <template v-if="checkInfo">
        <div class="f1" style="margin-bottom: 20px; font-size: 18px;">{{ dataform.staffBadge }} - {{ currentStaff.name }} ，入住检查</div>
        <div><span class="label">是否已申请过外宿补贴：</span><span class="f1">{{checkInfo.isOutDormitory===0?'否':'是'}}</span></div>
        <div class="tip_row">
          <span class="label">是否拥有多套宿舍：</span>
          <span class="f1">
            <template v-if="!checkInfo.rooms || checkInfo.rooms.length===0">
              否
            </template>
            <template v-else>是
              <div v-for="(item, index) in checkInfo.rooms" :key="index" style="margin-top: 5px;">
                {{item}}
              </div>
            </template>
          </span>
        </div>
      </template>
    </div>
    <div class="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading" >保 存</el-button>
    </div>
  </div>
</template>

<script>
import { callOwanceDetails, addDormitoryStaff } from "@/api/platform/dormitory/bed_mng";
import { getStaffDetail } from "@/api/platform/vehicle/staff_vehicle_detail";
import { dateFormat } from "@/util/date";
const dateNow = dateFormat(new Date());
export default {
  data() {
    return {
      checkInfo: null, //入住检查结果
      currentStaff: {},
      dataform: {
        staffBadge: '',
        createTime: dateNow
      },
      rules: {
        staffBadge: [{ required: true, message: "请输入工号", trigger: "blur" }],
        createTime: [
          { required: true, message: "请选择入住日期", trigger: "change" }
        ]
      },
      btnLoading: false,
    }
  },
  props: {
    visible: Boolean,
    dataItem: Object
  },
  created() {},
  watch: {
    'dataform.staffBadge'(v){
      if(v === ''){
        this.currentStaff = {}
        this.checkInfo = null
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
      if(this.currentStaff&&this.currentStaff.id){
        if(this.currentStaff.status === 0){
          this.$message.error('此员工已离职，不能入住!');
          return
        }
      }else{
        this.$message.error('请先查询员工信息');
        return
      }
      if(!this.checkInfo.isPass){
        this.$message.error(this.checkInfo.errorDor);
        return
      }
      let obj = {
        bedId: this.dataItem.bedId,
        staffId: this.currentStaff.id,
        createTime: this.dataform.createTime,
        simpleRemark: this.currentStaff.simpleRemark
      };
      this.btnLoading = true
      const res = await addDormitoryStaff(obj)
      this.btnLoading = false
      if(res.data.data){
        this.$message({
          message: '入住成功',
          type: 'success'
        });
        this.refresh()
      }
    },
    //根据工号查询员工详细信息
    selectStaffDetail(formName) {
      this.$refs[formName].validateField("staffBadge", errorMsg => {
        if (!errorMsg) {
          //空格过滤
          const staffBadge = this.dataform.staffBadge.replace(/\s+/g,"");
          getStaffDetail(staffBadge).then(res=>{
            let result = res.data.data;
            if (!this.validatenull(result)) {
              let id = result.id;
              if (!this.validatenull(id)) {
                this.currentStaff = res.data.data;
                if (this.currentStaff.sex == 0) {
                  this.currentStaff.sex = "男";
                } else if (this.currentStaff.sex == 1) {
                  this.currentStaff.sex = "女";
                } else {
                  this.currentStaff.sex = "";
                }
                this.dataform.staffBadge = this.currentStaff.badge
                //入住检查
                this.selectStaff()
              } else {
                this.$notify.error({
                  title: "提示",
                  message: "暂无该工号对应员工信息，请检查工号是否正确！"
                });
              }
            }
          })
        } else {
          return false;
        }
      });
    },
    // 入住检查
    async selectStaff() {
      const res = await callOwanceDetails(
        {
          staffBadge: this.dataform.staffBadge,
          parkId: this.dataItem.parkId,
          nowDor: this.dataItem.dormitoryName
        }
      )
      if (res.data.code === 0) {
        this.checkInfo = res.data.data
      }
    },
    refresh() {
      this.$refs.form && this.$refs.form.resetFields()
      this.btnLoading = false
      this.$emit('refresh')
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.btnLoading = false
      this.$emit('cancel')
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
.tips {
  margin-bottom: 20px;
  .f1{
    color: #ed6d00;
  }
  div{
    margin-bottom: 8px;
  }
  .label{
    width: 160px;
    display: inline-block;
  }
  .tip_row{
    display: flex;
    // justify-content: space-between;
  }
}
.footer{
  padding: 20px 0 30px 0;
  text-align: right;
}
</style>
