<template>
  <el-dialog
    ref="dialog"
    title="修改水电抄表"
    class="dialog_form sdcb_form"
    :visible.sync="currVisible"
    width="800px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <div>
      <div></div>
      <el-form ref="form" :inline="true" :model="dataform" size="mini">
        <div class="sdcb_btm">
          <template >
            <div class="sdcb_tb">
              <div class="sdcbt_t">
                <div class="c1"></div>
                <div>上月表读数</div>
                <div>当月表读数</div>
                <div>实际用量</div>
                <div>标准用量</div>
                <div>超标准用量</div>
              </div>
              <template v-if="beforeEleInfo">
                <div class="sdcbt_row">
                  <div class="c1">电（度）换表前</div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="elePreMonthNum"
                      :rules="preRule"
                    >
                      <el-input v-model="dataform.elePreMonthNum" @blur="setNum(3, dataform)"/>
                    </el-form-item>
                  </div>
                  <div class="c_bz">{{beforeEleInfo.curMonthNum}}</div>
                  <div class="c_sy">{{dataform.elePreMonthNum | fl_getmNum(beforeEleInfo.curMonthNum, dataform.eleQty, 1)}}</div>
                  <div class="c_bz">{{dataform.eleQty}}</div>
                  <div class="c_cbz">{{dataform.elePreMonthNum | fl_getmNum(beforeEleInfo.curMonthNum, dataform.eleQty, 2)}}</div>
                </div>
                <div class="sdcbt_row">
                  <div class="c1">电（度）换表后</div>
                  <div class="c_bz">0</div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="eleCurMonthNum"
                      :rules="curRule"
                    >
                      <el-input v-model="dataform.eleCurMonthNum" @blur="setNum(3, dataform)"/>
                    </el-form-item>
                  </div>
                  <div class="c_sy">{{0 | fl_getmNum(dataform.eleCurMonthNum, dataform.eleQty, 1)}}</div>
                  <div class="c_bz">{{dataform.eleQty}}</div>
                  <div class="c_cbz">{{0 | fl_getmNum(dataform.eleCurMonthNum, dataform.eleQty, 2)}}</div>
                </div>
              </template>
              <template v-else>
                <div class="sdcbt_row">
                  <div class="c1">电（度）</div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="elePreMonthNum"
                      :rules="preRule"
                    >
                      <el-input v-model="dataform.elePreMonthNum" @blur="setNum(3, dataform)"/>
                    </el-form-item>
                  </div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="eleCurMonthNum"
                      :rules="curRule"
                    >
                      <el-input v-model="dataform.eleCurMonthNum" @blur="setNum(3, dataform)"/>
                    </el-form-item>
                  </div>

                  <div class="c_sy">{{dataform.elePreMonthNum | fl_getmNum(dataform.eleCurMonthNum, dataform.eleQty, 1)}}</div>
                  <div class="c_bz">{{dataform.eleQty}}</div>
                  <div class="c_cbz">{{dataform.elePreMonthNum | fl_getmNum(dataform.eleCurMonthNum, dataform.eleQty, 2)}}</div>
                </div>
              </template>
              <template v-if="beforeColdInfo">
                <div class="sdcbt_row">
                  <div class="c1">冷水（方）换表前</div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="coldPreMonthNum"
                      :rules="preRule"
                    >
                      <el-input v-model="dataform.coldPreMonthNum" @blur="setNum(2, dataform)"/>
                    </el-form-item>
                  </div>
                  <div class="c_bz">{{beforeColdInfo.curMonthNum}}</div>
                  <div class="c_sy">{{dataform.coldPreMonthNum | fl_getmNum(beforeColdInfo.curMonthNum, dataform.coldQty, 1)}}</div>
                  <div class="c_bz">{{dataform.coldQty}}</div>
                  <div class="c_cbz">{{dataform.coldPreMonthNum | fl_getmNum(beforeColdInfo.curMonthNum, dataform.coldQty, 2)}}</div>
                </div>
                <div class="sdcbt_row">
                  <div class="c1">冷水（方）换表后</div>
                  <div class="c_bz">0</div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="coldCurMonthNum"
                      :rules="curRule"
                    >
                      <el-input v-model="dataform.coldCurMonthNum" @blur="setNum(2, dataform)"/>
                    </el-form-item>
                  </div>
                  <div class="c_sy">{{0 | fl_getmNum(dataform.coldCurMonthNum, dataform.coldQty, 1)}}</div>
                  <div class="c_bz">{{dataform.coldQty}}</div>
                  <div class="c_cbz">{{0 | fl_getmNum(dataform.coldCurMonthNum, dataform.coldQty, 2)}}</div>
                </div>
              </template>
              <template v-else>
                <div class="sdcbt_row">
                  <div class="c1">冷水（方）</div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="coldPreMonthNum"
                      :rules="preRule"
                    >
                      <el-input v-model="dataform.coldPreMonthNum" @blur="setNum(2, dataform)"/>
                    </el-form-item>
                  </div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="coldCurMonthNum"
                      :rules="curRule"
                    >
                      <el-input v-model="dataform.coldCurMonthNum" @blur="setNum(2, dataform)"/>
                    </el-form-item>
                  </div>
                  <div class="c_sy">{{dataform.coldPreMonthNum | fl_getmNum(dataform.coldCurMonthNum, dataform.coldQty, 1)}}</div>
                  <div class="c_bz">{{dataform.coldQty}}</div>
                  <div class="c_cbz">{{dataform.coldPreMonthNum | fl_getmNum(dataform.coldCurMonthNum, dataform.coldQty, 2)}}</div>
                </div>
              </template>
              <template v-if="beforeHotInfo">
                <div class="sdcbt_row">
                  <div class="c1">热水（方）换表前</div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="hotPreMonthNum"
                      :rules="preRule"
                    >
                      <el-input v-model="dataform.hotPreMonthNum" @blur="setNum(1, dataform)"/>
                    </el-form-item>
                  </div>
                  <div class="c_bz">{{beforeHotInfo.curMonthNum}}</div>
                  <div class="c_sy">{{dataform.hotPreMonthNum | fl_getmNum(beforeHotInfo.curMonthNum, dataform.hotQty, 1)}}</div>
                  <div class="c_bz">{{dataform.hotQty}}</div>
                  <div class="c_cbz">{{dataform.hotPreMonthNum | fl_getmNum(beforeHotInfo.curMonthNum, dataform.hotQty, 2)}}</div>
                </div>
                <div class="sdcbt_row">
                  <div class="c1">热水（方）换表后</div>
                  <div class="c_bz">0</div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="hotCurMonthNum"
                      :rules="curRule"
                    >
                      <el-input v-model="dataform.hotCurMonthNum" @blur="setNum(1, dataform)"/>
                    </el-form-item>
                  </div>
                  <div class="c_sy">{{0 | fl_getmNum(dataform.hotCurMonthNum, dataform.hotQty, 1)}}</div>
                  <div class="c_bz">{{dataform.hotQty}}</div>
                  <div class="c_cbz">{{0 | fl_getmNum(dataform.hotCurMonthNum, dataform.hotQty, 2)}}</div>
                </div>
              </template>
              <template v-else>
                <div class="sdcbt_row">
                  <div class="c1">热水（方）</div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="hotPreMonthNum"
                      :rules="preRule"
                    >
                      <el-input v-model="dataform.hotPreMonthNum" @blur="setNum(1, dataform)"/>
                    </el-form-item>
                  </div>
                  <div>
                    <el-form-item
                      class="row_ipt"
                      prop="hotCurMonthNum"
                      :rules="curRule"
                    >
                      <el-input v-model="dataform.hotCurMonthNum" @blur="setNum(1, dataform)"/>
                    </el-form-item>
                  </div>
                  <div class="c_sy">{{dataform.hotPreMonthNum | fl_getmNum(dataform.hotCurMonthNum, dataform.hotQty, 1)}}</div>
                  <div class="c_bz">{{dataform.hotQty}}</div>
                  <div class="c_cbz">{{dataform.hotPreMonthNum | fl_getmNum(dataform.hotCurMonthNum, dataform.hotQty, 2)}}</div>
                </div>
              </template>
            </div>
          </template>
        </div>
      </el-form>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { meterreadAdd } from '../_service'
import { validatenull } from "@/util/validate";
import { getDateMonth, floatNumMinus} from "@/util/util";
export default {
  data() {
    return {
      account: false, //当前是否已结算
      dataform: {},
      curRule: { required: true, message: '请输入当月表读数', trigger: 'blur' },
      preRule: { required: true, message: '请输入上月表读数', trigger: 'blur' },
      btnLoading: false,
      currVisible: false,
      errArr: [],
      beforeEleInfo: undefined,
      beforeHotInfo: undefined,
      beforeColdInfo: undefined
    }
  },
  props: {
    visible: Boolean,
    curItem: Object
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
        this.dataform = Object.assign({}, this.curItem)
        const list = this.dataform.changeList
        if(list.length > 0){
          list.forEach(element => {
            if(element.categoryId === 1){
              this.beforeHotInfo = element
            }else if(element.categoryId === 2){
              this.beforeColdInfo = element
            }else{
              this.beforeEleInfo = element
            }
          });
        }
      }
    },
    curItem:{
			handler: function(){},
			immediate: true
    },

  },
  filters: {
    fl_getCategory: function(val) {
      if(validatenull(val)){
        return
      }
      const arr = ['热水（方）','冷水（方）','电（度）']
      return  arr[val-1]
    },
    fl_getmNum: function(val, val2, val3, type) {
      //val3 标准用量
      //type 1 实际用量， 2超标准用量
      if(type===1){
        if(validatenull(val)||validatenull(val2)){
          return
        }
        return floatNumMinus(val2, val)
      }else if(type===2){
        if(validatenull(val)||validatenull(val2)||validatenull(val3)){
          return
        }
        let bz = floatNumMinus(val2, val)
        let cbz = floatNumMinus(bz, val3)
        if(Number(cbz)<0){
          return 0
        }else {
          return cbz
        }
      }else{
        return
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
      if(this.errArr&&this.errArr.length>0){
        this.$message.error('请检查'+this.errArr.toString()+'的当月表读数，应大于对应的上月的表读数');
        return
      }
      await this.validateForm()
      this.btnLoading = true
      let hotObj = {
        categoryId: 1,
        curMonthNum: this.dataform.hotCurMonthNum,
        preMonthNum: this.dataform.hotPreMonthNum
      }
      let coldObj = {
        categoryId: 2,
        curMonthNum: this.dataform.coldCurMonthNum,
        preMonthNum: this.dataform.coldPreMonthNum
      }
      let eleObj = {
        categoryId: 3,
        curMonthNum: this.dataform.eleCurMonthNum,
        preMonthNum: this.dataform.elePreMonthNum
      }
      // if(this.beforeHotInfo){
      //   hotObj.preMonthNum = this.beforeHotInfo.preMonthNum
      // }
      // if(this.beforeColdInfo){
      //   coldObj.preMonthNum = this.beforeColdInfo.preMonthNum
      // }
      // if(this.beforeEleInfo){
      //   eleObj.preMonthNum = this.beforeEleInfo.preMonthNum
      // }
      // 1热水 2冷水 3电
      let meterReadDetailList = [hotObj, coldObj, eleObj]
      let obj = {
        meterMonth: this.dataform.meterMonth,
        meterReadDetailList: meterReadDetailList,
        roomId: this.dataform.roomId,
        roomName: this.dataform.roomName,
      }
      const res = await meterreadAdd(obj)
      this.btnLoading = false
      if(res.data.code===0){
        this.$message({
          message: '修改抄表数据成功',
          type: 'success'
        });
        this.refresh()
      }
    },
    getCategoryId(val) {
      if(validatenull(val)){
        return
      }
      const arr = ['热水（方）','冷水（方）','电（度）']
      return  arr[val-1]
    },
    setNum(categoryId, meter) {
      let item = this.getCategoryId(categoryId)
      let preMonthNum = undefined
      let curMonthNum = undefined
      if(categoryId===1){ //热水
        preMonthNum = meter.hotPreMonthNum
        curMonthNum = meter.hotCurMonthNum
        if(this.beforeHotInfo) return
      }else if (categoryId===2){ //冷水
        preMonthNum = meter.coldPreMonthNum
        curMonthNum = meter.coldCurMonthNum
        if(this.beforeColdInfo) return
      }else if (categoryId===3){ //电
        preMonthNum = meter.elePreMonthNum
        curMonthNum = meter.eleCurMonthNum
        if(this.beforeEleInfo) return
      }
      if (!this.validatenull(preMonthNum) && !this.validatenull(curMonthNum)) {
        if (Number(preMonthNum) > Number(curMonthNum)) {
          this.$message.error(item + '，当月表读数应大于上一月表读数')
          if (!this.errArr.includes(item)) {
            this.errArr.push(item)
          }
        } else {
          if (this.errArr.includes(item)) {
            let indexTemp = this.errArr.indexOf(item)
            this.errArr.splice(indexTemp, 1)
          }
        }
      } else {
        if (this.errArr.includes(item)) {
          let indexTemp = this.errArr.indexOf(item)
          this.errArr.splice(indexTemp, 1)
        }
      }
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.dataform = {}
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.dataform = {}
      this.btnLoading = false
      this.currVisible = false
      this.beforeEleInfo = null,
      this.beforeHotInfo = null,
      this.beforeColdInfo = null
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
  .sdcb_form ::v-deep {
    $c1: #10cc8e;
    $c2: #999;
    $c3: #e7292e;
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
            width: 130px;
            text-align: left
          }
        }
        .sdcbt_row{
          height: 50px;
          .row_ipt{
            width: 100px;
          }
          .el-input--mini .el-input__inner{
            text-align: center;
            background: #fafafa;
            border: 1px solid #dcdfe6;
            border-radius: 0;
          }
        }
        .c_sy{
          color: $c1;
        }
        .c_bz{
          color: $c2;
          line-height: 32px;
        }
        .c_cbz{
          color: $c3;
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
