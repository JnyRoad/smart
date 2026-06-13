<!--水电抄表-->
<template>
  <el-dialog
    title="水电抄表"
    class="dialog_form sdcb_form"
    :width="dConfig.width"
    :top="dConfig.top"
    @close="resetSetForm('dataform')"
    :visible.sync="setFormVisible"
  >
    <el-form ref="dataform" :inline="true" :model="dataform" size="mini">
      <div class="sdcb_tp">
        <div class="swiper_outer">
          <el-radio-group class="rdGroup" v-model="curRoomId" @change="getDetail()">
            <swiper :options="swiperOption" v-if="rowsOther.length > 0">
              <swiper-slide
                ref="mySwiper"
                class="swiper-slide"
                v-for="(item, index) in rowsOther"
                :key="index"
              >
                <div class="room-item">
                  <el-radio-button :label="item.roomId" :value="item.roomId">{{item.roomName}}</el-radio-button>
                  <i class="tagi" :class="{'tag1':item.status==2,'tag2':item.status==1}"></i>
                </div>
              </swiper-slide>
            </swiper>
          </el-radio-group>
          <div class="swiper-button-prev sdcb-prev"></div>
          <div class="swiper-button-next sdcb-next"></div>
        </div>
        <div>
          <el-form-item label="抄表月份">
            <el-date-picker
              @change="getDetail('m')"
              v-model="curMonth"
              type="month"
              value-format="yyyy-MM"
              placeholder="选择抄表月份"
              @focus="forbid"
              :clearable="false"
            >
            </el-date-picker>
          </el-form-item>
          <div class="statusTip">
            <div><i class="tagi tag1"></i>全部已抄完</div>
            <div><i class="tagi tag2"></i>部分抄完</div>
          </div>
          <div class="tip2" v-if="account">
            *{{curItem.roomName}}号房间 {{curMonth}}已结算
          </div>
        </div>
      </div>
      <div class="sdcb_btm">
        <template v-if="dataform && dataform.meterReadDetailList.length>0">
          <div class="sdcb_tb">
            <div class="sdcbt_t">
              <div class="c1"></div>
              <div>上月表读数</div>
              <div>当月表读数</div>
              <div>月度用量</div>
              <div>人均每天用量</div>
            </div>
            <template v-for="(item, index) in dataform.meterReadDetailList">
              <div class="sdcbt_row" :key="index">
                <div class="c1">{{item.categoryId | fl_getCategory}}</div>
                <div>
                  <el-form-item
                    class="row_ipt"
                    :prop="'meterReadDetailList.'+index+'.preMonthNum'"
                    :rules="{ required: true, message: '请输入上月表读数', trigger: 'blur' }"
                  >
                    <template v-if="isPad">
                      <el-input type="number" v-model="item.preMonthNum" @blur="setNum(item.categoryId, item)" :disabled="account"/>
                    </template>
                    <template v-else>
                      <el-input v-model="item.preMonthNum" @blur="setNum(item.categoryId, item)" :disabled="account"/>
                    </template>
                  </el-form-item>
                </div>
                <div>
                  <el-form-item
                    class="row_ipt"
                    :prop="'meterReadDetailList.'+index+'.curMonthNum'"
                    :rules="{ required: true, message: '请输入当前月表读数', trigger: 'blur' }"
                  >
                   <template v-if="isPad">
                      <el-input type="number" v-model="item.curMonthNum" @blur="setNum(item.categoryId, item)" :disabled="account"/>
                    </template>
                    <template v-else>
                      <el-input v-model="item.curMonthNum" @blur="setNum(item.categoryId, item)" :disabled="account"/>
                    </template>
                  </el-form-item>
                </div>
                <div>{{item.preMonthNum | fl_getmNum(item.curMonthNum)}}</div>
                <div>{{item.avgNum}}</div>
              </div>
            </template>
            <template v-for="(item, index) in commonCates">
              <div class="sdcbt_row" :key="index">
                <div class="c1">{{item.categoryId | fl_getCategory2}}</div>
                <div>-</div>
                <div>-</div>
                <div>-</div>
                <div>{{item.avgNum}}</div>
              </div>
            </template>
          </div>
          <p class="tip">*人均每天用量为抄表截至日期，累计用水电量与累计入住人天数的比值</p>
        </template>
        <template v-else>
          <div class="tips">{{tip}}</div>
        </template>
      </div>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="resetSetForm('dataform')" plain
        >取 消</el-button
      >
      <el-button
        type="primary"
        @click="editSubmit('dataform')"
        :loading="setLoading"
        :disabled="account || dataform && dataform.meterReadDetailList.length==0"
        >保 存</el-button
      >
    </div>
  </el-dialog>
</template>

<script>
import { swiper, swiperSlide } from "vue-awesome-swiper";
import "swiper/dist/css/swiper.css";
import { meterreadRoom, meterreadAdd, meterreadStatus} from "../_service.js";
import { validatenull } from "@/util/validate";
import { getDateMonth, floatNumMinus} from "@/util/util";
export default {
  name: "",
  components: {
    swiper,
    swiperSlide,
  },
  data() {
    return {
      setFormVisible: false,
      setLoading: false, //是否正在设置
      swiperOption: {
        initialSlide: 0,
        slidesPerView: "auto",
        spaceBetween: 10,
        observer: true,
        observeParents: true,
        loop: false,
        navigation: {
          nextEl: ".sdcb-prev",
          prevEl: ".sdcb-next",
        },
      },
      commonCates: [], //公摊数据
      tip: '请选择查询条件！',
      account: false, //当前是否已结算
      records: [],
      curRoomId: '',
      curMonth: '',
      roomIds: [],
      statusArr: [],
      rowsOther: [],
      dataform: {
        meterReadDetailList: []
      },
      errArr: []
    };
  },
  props: {
    visible: {
      type: Boolean,
    },
    curItem: undefined,
    curIndex: undefined,
    rows: undefined,
    isPad: Boolean
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
    'dataform.meterReadDetailList'(newVal, oldVal) {
      this.errArr = []
    },
    rows:{
			handler: function(val){
        [...this.rowsOther] = val
        this.roomIds = []
        if(val&&val.length>0){
          val.forEach(el=>{
            this.roomIds.push(el.roomId)
          })
        }
        if(this.curMonth&&this.roomIds.length>0){
          this.checkStatus()
        }
      },
			immediate: true
    },
    curItem:{
			handler: function(val){
        this.curRoomId = val.roomId
        this.getDetail()
      },
			immediate: true
    },
    curIndex(newVal, oldVal) {
      this.$set(this.swiperOption, "initialSlide", newVal);
    },
  },
  created() {
    this.initData();
    this.curMonth = getDateMonth();
  },
  mounted: function () {},
  computed: {
    dConfig(){
      if(this.isPad){
        return {
          width: '80%',
          top: '30px'
        }
      }else{
        return {
          width: '800px',
          top: '100px'
        }
      }
    }
  },
  filters: {
    fl_getCategory: function(val) {
      if(validatenull(val)){
        return
      }
      const arr = ['热水（方）','冷水（方）','电（度）']
      return  arr[val-1]
    },
    fl_getCategory2: function(val) {
      if(validatenull(val)){
        return
      }
      const arr = ['公摊热水（方）','公摊冷水（方）','公摊电（度）']
      return  arr[val-1]
    },
    fl_getmNum: function(val, val2) {
      if(validatenull(val)||validatenull(val2)){
        return
      }
      return floatNumMinus(val2, val)
    }
  },
  methods: {
    forbid(){
      //禁止软键盘弹出
      document.activeElement.blur();
    },
    getCategoryId(val) {
      if(validatenull(val)){
        return
      }
      const arr = ['热水（方）','冷水（方）','电（度）']
      return  arr[val-1]
    },
    setNum(categoryId,meter){
      let item = this.getCategoryId(categoryId)
      if(!this.validatenull(meter.preMonthNum)&&!this.validatenull(meter.curMonthNum)){
        if(Number(meter.preMonthNum)>Number(meter.curMonthNum)){
          this.$message.error(item+'，当前月表读数应大于上一月表读数');
          if(!this.errArr.includes(item)){
            this.errArr.push(item)
          }
        }else{
          if(this.errArr.includes(item)){
            let indexTemp = this.errArr.indexOf(item)
            this.errArr.splice(indexTemp, 1)
          }
        }
      }else{
        if(this.errArr.includes(item)){
          let indexTemp = this.errArr.indexOf(item)
          this.errArr.splice(indexTemp, 1)
        }
      }
    },
    initData() {
      this.setFormVisible = this.visible;
    },
    //查询标记状态
    async checkStatus(){
      let obj = {
        meterMonth: this.curMonth,
        roomIds: this.roomIds
      }
      const res = await meterreadStatus(obj)
      if(res.data.code==0){
        this.statusArr = res.data.data.roomMeterStatuses
        if(this.statusArr&&this.statusArr.length>0){
          this.rowsOther.forEach(el=>{
            this.statusArr.forEach(el2=>{
              if(el.roomId == el2.roomId){
                el.status = el2.status
              }
            })
          })
        }
      }
    },
    initForm(){
      this.curMonth = ''
      this.records =[]
      this.curRoomId = ''
      this.curMonth = ''
      this.dataform = {
        meterReadDetailList: []
      }
    },
    async getDetail(e) {
      if(e=='m'){
        this.checkStatus()
      }
      if(!validatenull(this.curMonth) && !validatenull(this.curRoomId)){
        this.dataform.meterReadDetailList = []
        let obj = {
          meterMonth: this.curMonth
        }
        const res = await meterreadRoom(this.curRoomId,obj)
        if(res.data.code==0){
          const resObj = res.data.data
          resObj.statementStatus==1?this.account=true:this.account=false
          let addObj = {
            meterMonth: resObj.meterMonth,
            roomId: resObj.roomId,
            roomName: resObj.roomName,
            meterReadDetailList: []
          }
          let objS = []
          let objC = []

          // 公摊热水、公摊冷水、公摊电
          if(!resObj.commonCates || resObj.commonCates.length==0){
            for(let i = 0; i<3; i++){
              objC.push({
                categoryId: i+1,
                avgNum: null
              })
            }
          }else{
            resObj.commonCates.forEach(el=>{
              objC.push({
                categoryId: el.categoryId,
                avgNum: el.avgNum
              })
            })
            if(objC.length<3){
              let arr = []
              objC.forEach(el=>{
                arr.push(el.categoryId)
              })
              let arrB = [1,2,3].filter(function(v){ return arr.indexOf(v) == -1 })
              arrB.forEach(el=>{
                objC.push({
                  categoryId: el,
                  avgNum: null
                })
              })
            }
          }

          this.commonCates = objC

          // 热水、冷水、电
          if(!resObj.dormitoryCates || resObj.dormitoryCates.length==0){
            for(let i = 0; i<3; i++){
              objS.push({
                categoryId: i+1,
                curMonthNum: null,
                preMonthNum: null,
                avgNum: null
              })
            }
          }else{
            resObj.dormitoryCates.forEach(el=>{
              objS.push({
                categoryId: el.categoryId,
                curMonthNum: el.curMonthNum,
                preMonthNum: el.preMonthNum,
                avgNum: el.avgNum
              })
            })
            if(objS.length<3){
              let arr = []
              objS.forEach(el=>{
                arr.push(el.categoryId)
              })
              let arrB = [1,2,3].filter(function(v){ return arr.indexOf(v) == -1 })
              arrB.forEach(el=>{
                objS.push({
                  categoryId: el,
                  curMonthNum: null,
                  preMonthNum: null,
                  avgNum: null
                })
              })
            }
          }
          addObj.meterReadDetailList = objS
          this.dataform = addObj
          if(!(this.dataform && this.dataform.meterReadDetailList.length>0)){
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
      // this.initForm()
      this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    async editSubmit(formName) {
      if(this.errArr&&this.errArr.length>0){
        this.$message.error('请检查'+this.errArr.toString()+'的当前月表读数，应大于对应的上月的表读数');
        return
      }
      await this.$refs[formName].validate(valid =>{
        if (valid) {
          meterreadAdd(this.dataform).then(res=>{
            if(res.data.code==0){
              this.resetSetForm(formName)
              this.$emit("dlgdoSuccess");
              this.$notify({
                title: '成功',
                message: '水电抄表成功',
                type: 'success'
              });
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
.sdcb_form ::v-deep {
  .tagi{
    width: 10px;
    height: 10px;
    display: inline-block;
    border-radius: 50%;
  }
  .tag1{
    background: #0dbc82;
  }
  .tag2{
    background: orange;
  }
  .tip2{
    display: inline-block;
    margin-left: 30px;
    line-height: 30px;
    color: #999;
  }
  .statusTip{
    display: inline-block;
    margin-left: 25px;
    >div{
      display: inline-block;
      margin-right: 15px;
      .tagi{
        margin-right: 5px;
        width: 8px;
        height: 8px;
      }
      line-height: 30px;
      font-size: 12px;
    }
  }
  .tips{
    padding: 30px 0;
    text-align: center;
    color: #999;
  }
  .el-dialog__body {
    padding: 10px 0 0 0;
  }
  .el-form--inline .el-form-item{
    margin-right: 0;
  }
  .sdcb_tp {
    border-bottom: 10px solid #f0f2f5;
    padding: 0 20px;
  }
  .sdcb_btm {
    padding: 10px 20px 50px;
    .sdcb_tb{
      .sdcbt_row, .sdcbt_t{
        display: flex;
        height: 40px;
        div{
          // line-height: 40px;
          text-align: center;
        }
        div:not(.c1){
          flex: 1;
        }
        .c1{
          width: 110px;
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

  .swiper_outer {
    position: relative;
    padding: 0 30px;
    margin-bottom: 20px;
    .rdGroup{
      width: 100%;
    }
    .room-item{
      position: relative;
      padding: 4px 4px 0 0;
      .tagi{
        position: absolute;
        top: 0px;
        right: 0px;
      }
    }
    .room-item:last-child .el-radio-button__inner{
      border-radius: 0;
    }
    .el-radio-button:last-child .el-radio-button__inner{
      border-radius: 0;
    }
    .el-radio-button__inner{
      color: #333;
      background: #f6f6f6;
      border: none;
      padding: 6px 15px;
      cursor: pointer;
    }
    .el-radio-button__orig-radio:checked+.el-radio-button__inner{
      color: #fff;
      background: #ed6d00;
    }
    .room_num{
      display: inline-block;
      background: #f6f6f6;
      color: #333;
      padding: 5px 15px;
      cursor: pointer;
    }
    .active_num{
      color: #fff;
      background: #ed6d00;
    }
    .swiper-slide{
      width: auto;
    }
    .swiper-button-prev,
    .swiper-container-rtl .swiper-button-next {
      background-image: none;
      left: -14px;
    }
    .swiper-button-next,
    .swiper-container-rtl .swiper-button-prev {
      background-image: none;
      right: 0;
    }
    .swiper-button-prev,
    .swiper-button-next {
      margin-top: -22px;
    }
    .swiper-button-prev::before {
      position: absolute;
      left: 4px;
      top: 16px;
      content: "";
      display: inline-block;
      border: 10px solid #ed6d00;
      border-top: 6px solid transparent;
      border-left: 10px solid transparent;
      border-bottom: 6px solid transparent;
    }
    .swiper-button-next::before {
      position: absolute;
      left: 17px;
      top: 16px;
      content: "";
      display: inline-block;
      border: 10px solid #ed6d00;
      border-top: 6px solid transparent;
      border-right: 10px solid transparent;
      border-bottom: 6px solid transparent;
    }
    .swiper-button-prev.swiper-button-disabled,
    .swiper-button-next.swiper-button-disabled {
      opacity: 1;
    }
    .swiper-button-prev.swiper-button-disabled::before {
      border-right: 10px solid #c0c0c0;
    }
    .swiper-button-next.swiper-button-disabled::before {
      border-left: 10px solid #c0c0c0;
    }
  }
}
</style>
