<!--批量抄电-->
<template>
  <el-dialog
    title="批量抄电"
    class="dialog_form plcd_form"
    width="800px"
    @close="resetSetForm('dataform')"
    :visible.sync="setFormVisible"
  >

    <div class="sdcb_tp">
      <el-form ref="dataform" :inline="true" :model="dataform" size="mini">
        <div class="row-line">
          <el-form-item label="园区选择" prop="parkId" class="m_ipt">
            <parkSelect
              @doChange="doParkChange"
              v-model="dataform.parkId"
            ></parkSelect>
          </el-form-item>
          <el-form-item label="宿舍选择" prop="dormitoryId" class="m_ipt">
            <dormSelect
              @doChange="doDormChange"
              :parkId="dataform.parkId"
              v-model="dataform.dormitoryId"
            ></dormSelect>
          </el-form-item>
        </div>
        <div class="row-line">
          <el-form-item label="楼层选择" prop="floorId" class="m_ipt">
            <floorSelect
              @doChange="getDetail"
              :parkId="dataform.parkId"
              :dormitoryId="dataform.dormitoryId"
              v-model="dataform.floorId"
            ></floorSelect>
          </el-form-item>
          <el-form-item label="抄表月份" prop="meterMonth" class="m_ipt">
            <el-date-picker
              v-model="dataform.meterMonth"
              type="month"
              @change="getDetail('m')"
              value-format="yyyy-MM"
              placeholder="选择抄表月份"
            >
            </el-date-picker>
          </el-form-item>
        </div>
      </el-form>
    </div>
    <div class="sdcb_btm">
      <el-form ref="addform" :inline="true" :model="addForm" size="mini">
      <template v-if="addForm.records&&addForm.records.length>0">
        <div class="sdcb_tb">
          <div class="sdcbt_t">
            <div class="c1"></div>
            <div class="cateRow">
              <div>上一月份表读数</div>
              <div>当前月表读数</div>
              <div>月度用量</div>
            </div>
          </div>
          <template v-for="(item, index) in addForm.records">
            <div class="sdcbt_row" :key="index">
              <div class="c1">{{item.roomName}}号房
                <!-- statementStatus 为1时  表示已结算 -->
                <span class="closed" v-if="item.statementStatus==1"></span>
                <span class="closed_holder" v-else></span>
              </div>
              <template v-for="(it2, index2) in item.meterReadDetailList">
                <div class="cateRow" :key="index2">
                  <div>
                    <!-- :rules="{ validator: vPreMonthNum, trigger: 'blur' }" -->
                    <!-- :rules="{ required: true, message: '请输入上月表读数', trigger: 'blur' }" -->
                    <el-form-item class="row_ipt"
                      :prop="'records.'+index+'.meterReadDetailList.'+index2+'.preMonthNum'"
                    >
                      <el-input v-model="it2.preMonthNum" @blur="setNum(item, it2)" :readonly="item.statementStatus===1"/>
                    </el-form-item>
                  </div>
                  <div>
                    <!-- :rules="{ validator: vCurMonthNum, trigger: 'blur' }" -->
                    <!-- :rules="{ required: true, message: '请输入当前月表读数', trigger: 'blur' }" -->
                    <el-form-item class="row_ipt"
                      :prop="'records.'+index+'.meterReadDetailList.'+index2+'.curMonthNum'"
                    >
                      <el-input v-model="it2.curMonthNum" @blur="setNum(item, it2)" :readonly="item.statementStatus===1"/>
                    </el-form-item>
                  </div>
                  <div>{{it2.preMonthNum | fl_getmNum(it2.curMonthNum)}}</div>
                </div>
              </template>
            </div>
          </template>
          <div class="tips2">
            <span class="closed"></span>
            <span style="margin-left: 10px;">表示已结算，不可更改该项表读数</span>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="tips">{{tip}}</div>
      </template>
      </el-form>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="resetSetForm('dataform')" plain
        >取 消</el-button
      >
      <!-- 全部结算后 不显示保存按钮 -->
      <el-button
        v-if="!allClosed"
        type="primary"
        @click="editSubmit('addform')"
        :loading="setLoading"
        :disabled="addForm&&addForm.records==0"
        >保 存</el-button
      >
    </div>
  </el-dialog>
</template>

<script>

import { meterreadByFloor, meterreadBatch } from "../_service.js";
import { validatenull } from "@/util/validate";
import { floatNumMinus } from "@/util/util";

export default {
  name: "",
  data() {
    return {
      setFormVisible: false,
      setLoading: false, //是否正在设置
      dataform: {
        parkId: '',
        dormitoryId: '',
        floorId: ''
      },
      addForm: {
        records: []
      },
      errArr: [],
      tip: '请选择查询条件！',
      floorId: '',
      allClosed: true //true 已全部结算
    };
  },
  props: {
    visible: {
      type: Boolean,
    },
    row: undefined,
  },
  filters: {
    fl_getmNum: function(val, val2) {
      if(validatenull(val)||validatenull(val2)){
        return
      }
      return floatNumMinus(val2, val)
    }
  },
  watch: {
    visible(newVal, oldVal) {
      if (newVal) {
        // this.getDetail();
      }
      this.setFormVisible = newVal;
      this.addForm.records = []
    },
    setFormVisible(newVal, oldVal) {
      if (newVal === false) {
        this.$emit("dlgdo", newVal);
      }
    },
    floorId(newVal, oldVal) {
      if(!newVal){
        this.addForm.records = []
      }
    },
    'dataform.meterMonth'(newVal, oldVal) {
      if(!newVal){
        this.addForm.records = []
      }
    },
    'addForm.records'(newVal, oldVal) {
      this.errArr = []
    }
  },
  created() {
    this.initData();
  },
  mounted: function () {},
  computed: {},
  methods: {
    setNum(room, meter){
      if(!this.validatenull(meter.preMonthNum)&&!this.validatenull(meter.curMonthNum)){
        if(Number(meter.preMonthNum)>Number(meter.curMonthNum)){
          this.$message.error(room.roomName+'号房，当前月表读数应大于上一月表读数');

          if(!this.errArr.includes(room.roomName)){
            this.errArr.push(room.roomName)
          }
        }else{
          if(this.errArr.includes(room.roomName)){
            let indexTemp = this.errArr.indexOf(room.roomName)
            this.errArr.splice(indexTemp, 1)
          }
        }
      }else{
        if(this.errArr.includes(room.roomName)){
          let indexTemp = this.errArr.indexOf(room.roomName)
          this.errArr.splice(indexTemp, 1)
        }
      }
    },
    doParkChange(e){
      this.dataform.dormitoryId = ''
      this.dataform.floorId = ''
    },
    doDormChange(e){
      this.dataform.floorId = ''
      this.floorId = ''
    },
    initData() {
      this.setFormVisible = this.visible;
    },
    async getDetail(e) {
      let month = this.dataform.meterMonth
      //不是月份
      if(e!=='m'){
        this.floorId = e
      }
      if(!this.validatenull(month) && !this.validatenull(this.floorId)){
        this.addForm.records = []
        let obj = {
          meterMonth: month
        }
        const res = await meterreadByFloor(this.floorId,obj)
        if(res.data.code==0){
          // this.records = res.data.data

          res.data.data.forEach(el=>{
            let obj1 = {}, obj2={}
            if(el.commonCates && el.commonCates.length>0){
              obj1 = el.commonCates.find(item => {
                if (item.categoryId == 3) { //只取电
                  return item;
                }
              });
            }
            if(el.dormitoryCates && el.dormitoryCates.length>0){
              obj2 = el.dormitoryCates.find(item => {
                if (item.categoryId == 3) { //只取电
                  return item;
                }
              });
            }
            if(!obj2){
              obj2 = {}
            }
            //有未结算的，就是没有 全部结算完
            if(el.statementStatus!=1){
              this.allClosed = false
            }
            this.addForm.records.push({
              meterMonth: el.meterMonth,
              meterReadDetailList: [
                {
                  categoryId: 3,
                  curMonthNum: obj2.curMonthNum,
                  preMonthNum: obj2.preMonthNum,
                  avgNum: obj1.avgNum
                }
              ],
              roomId: el.roomId,
              roomName: el.roomName,
              statementStatus: el.statementStatus
            })
          })
          if(this.addForm.records.length==0){
            this.tip="当前条件下，没有相关记录！"
          }
        }else{
          this.$message.error(res.data.msg);
        }
      }
    },
    resetSetForm(formName) {
      this.setFormVisible = false;
      this.setLoading = false;
      this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    async editSubmit(formName) {
      if(this.errArr&&this.errArr.length>0){
        this.$message.error('请检查'+this.errArr.toString()+'房间的当前月表读数，应大于对应的上月的表读数');
        return
      }
      await this.$refs[formName].validate(valid =>{
        if (valid) {
          meterreadBatch(this.addForm.records).then(res=>{
            if(res.data.code==0){
              this.$notify({
                title: '成功',
                message: '批量抄电成功',
                type: 'success'
              });
              this.resetSetForm(formName)
            }
          }).catch(err=>{
            return err
          })
        } else {
          return false;
        }
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.plcd_form ::v-deep {
  .tips{
    padding-top: 30px;
    text-align: center;
    color: #999;
  }
  .tips2{
    color: #999;
    font-size: 12px;
    padding-top: 10px;;
  }
  .closed::before{
    content: '';
    width: 6px;
    height: 6px;
    display: inline-block;
    border-radius: 50%;
    background: #0dbc82;
    vertical-align: middle;
    margin: 0 0 0 8px;
  }
  .closed_holder::before{
    content: '';
    width: 6px;
    height: 6px;
    display: inline-block;
    border-radius: 50%;
    background: transparent;
    vertical-align: middle;
    margin: 0 0 0 8px;
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
      .sdcbt_row, .sdcbt_t{
        display: flex;
        line-height: 40px;
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
          }
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
