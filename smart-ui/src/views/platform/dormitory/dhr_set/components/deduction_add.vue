
<!--
- @name BU
-->
<template>
  <el-dialog ref="dialog" title="添加水电扣费项目" :visible.sync="currVisible" width="1200px" @open="open" @close="close" :close-on-click-modal="false" :append-to-body="true"  :custom-class="'approve-detail-dialog'">
    <el-form label-width="200px" label-position="left">
      <el-form-item label="选择职层">
        <el-checkbox-group v-model="jchesCheckList">
          <el-checkbox v-for="item in jchesOptions" :label="item" :key="item.typeCode" :disabled="item.disabled">{{item.typeName}}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="选择日平均水计算标准1-12月">
        <div class="table-outer">
          <div class="t_head">
            <div class="t_tr t_">
              <div class="t_td" v-for="(item, index) in sMonthList" :key="index">{{item.label}}</div>
              <div class="t_td">操作</div>
            </div>
          </div>
          <el-scrollbar class="my-scrollbar" :native="false">
            <div class="t_body">
              <div class="t_tr">
                <div class="t_td" v-for="(item, index) in sMonthList" :key="index">
                  <el-input v-model="item.value" placeholder=""></el-input>
                </div>
                <div class="t_td">
                  <el-popover
                    placement="bottom"
                    width="160"
                    v-model="sVisible">
                    <el-input v-model="allNum" placeholder=""></el-input>
                    <div style="text-align: center; margin-top: 4px">
                      <el-button size="mini" type="text" @click="sVisible = false">取消</el-button>
                      <el-button type="primary" size="mini" @click="sAllNumSave('sMonthList', 'sVisible')">确定</el-button>
                    </div>
                    <el-button type="text" slot="reference">批量修改</el-button>
                  </el-popover>
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>
      </el-form-item>
      <el-form-item label="选择日平均电计算标准1-12月">
        <div class="table-outer">
          <div class="t_head">
            <div class="t_tr t_">
              <div class="t_td" v-for="(item, index) in dMonthList" :key="index">{{item.label}}</div>
              <div class="t_td">操作</div>
            </div>
          </div>
          <el-scrollbar class="my-scrollbar" :native="false">
            <div class="t_body">
              <div class="t_tr">
                <div class="t_td" v-for="(item, index) in dMonthList" :key="index">
                  <el-input v-model="item.value" placeholder=""></el-input>
                </div>
                <div class="t_td">
                  <el-popover
                    placement="bottom"
                    width="160"
                    v-model="dVisible">
                    <el-input v-model="allNum" placeholder=""></el-input>
                    <div style="text-align: center; margin-top: 4px">
                      <el-button size="mini" type="text" @click="dVisible = false">取消</el-button>
                      <el-button type="primary" size="mini" @click="sAllNumSave('dMonthList', 'dVisible')">确定</el-button>
                    </div>
                    <el-button type="text" slot="reference">批量修改</el-button>
                  </el-popover>
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" plain @click="close">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">确 定</el-button>
    </div>
  </el-dialog>
</template>


<script>
import { getJche, addDeductionData } from '../_service.js'
/**
 * 判断是否为整数
 */
export function validatenum (num) {
  let regName = /^[0-9]*$/;
  if (num < 0) {
    return false
  }
  if (!regName.test(num)) return false
  return true
}
export default {
  data() {
    return {
      currVisible: false,
      btnLoading: false,
      jchesCheckList: [],
      sMonthList:[],
      dMonthList: [],
      sVisible: false,
      dVisible: false,
      allNum: null,
      itemId: null
    }
  },
  props: {
    tempId: [Number, String],
    parkId: [Number, String],
    itemObj: Object,
    jchesOptions: Array
  },
  created() {},
  watch: {
    currVisible() {
      this.initMonth()
    },
    itemObj(val){
      if(val){
        const jches = val.jches
        this.itemId = val.itemId
        const rules = val.rules
        const d = this.jchesOptions.filter(item => jches.some(ele => ele.jcheId === item.typeCode))
        d.forEach(element => {
          element.disabled = false
        });
        this.jchesCheckList = d
        this.sMonthList.forEach(el1 => {
          rules.forEach(el2 => {
            if(el2.categoryId === 2 && el1.monthNum === el2.monthNum) el1.value = el2.standardQty
          });
        });
        this.dMonthList.forEach(el1 => {
          rules.forEach(el2 => {
            if(el2.categoryId === 3 && el1.monthNum === el2.monthNum) el1.value = el2.standardQty
          });
        });
      }
    }
  },
  mounted: function () {},
  computed: {},
  methods: {
    sAllNumSave(t, k){
      if(this.allNum === null || this.allNum === ''){
        this.$message.error("内容不能为空！");
        return
      }
      if(!validatenum(this.allNum)){
        this.$message.error("请输入大于0的整数");
        return
      }
      this[t].forEach(element => {
        element.value = this.allNum
      });
      this.allNum = null
      this[k] = false
    },
    async getJcheData(){
      const res = await getJche()
      const d = res.data.data
      this.jchesOptions = d
    },
    initMonth() {
      this.sMonthList = []
      this.dMonthList = []
      for (let i = 1; i <= 12; i++) {
        this.sMonthList.push({
          label: i + '月',
          monthNum: i,
          value: null
        })
        this.dMonthList.push({
          label: i + '月',
          monthNum: i,
          value: null
        })
      }
    },
    validateMonthList(){
      for(let i in this.sMonthList){
        if(this.sMonthList[i].value === null ||this.sMonthList[i].value === ''){
          return false
        }
      }
      for(let i in this.dMonthList){
        if(this.dMonthList[i].value === null ||this.dMonthList[i].value === ''){
          return false
        }
      }
      return true
    },
    async formSumit() {
      if(this.jchesCheckList.length === 0){
        this.$message.error("请选择职层！");
        return
      }
      const s = this.validateMonthList()
      if(!s){
        this.$message.error("计算标准不能为空！");
        return
      }
      const jches = [], rules = []
      this.jchesCheckList.forEach(element => {
        const obj ={
          jcheId: element.typeCode,
          jcheName: element.typeName
        }
        jches.push(obj)
      });
      this.sMonthList.forEach(element => {
        const obj ={
          categoryId: 2,
          monthNum: element.monthNum,
          standardQty: element.value
        }
        rules.push(obj)
      });
      this.dMonthList.forEach(element => {
        const obj ={
          categoryId: 3,
          monthNum: element.monthNum,
          standardQty: element.value
        }
        rules.push(obj)
      });
      const data = {
        tempId: this.tempId,
        rule:{
          jches: jches,
          rules: rules
        }
      }
      if(this.itemId){
        data['itemId'] = this.itemId
      }
      const res = await addDeductionData(data)
      if(res.data.code === 0){
        this.$notify({
          title: '成功',
          message: '设置成功',
          type: 'success'
        });
        this.close()
        this.$emit("initData");
      }
    },
    open(val) {
      if(val === 'add'){
        this.jchesCheckList = []
      }
      this.currVisible = true
    },
    close() {
      this.jchesCheckList.forEach(element => {
          element.disabled = true
      });
      this.itemId = null
      this.$emit("initJchesCheck");
      this.currVisible = false
    }
  }
}
</script>

<style lang="scss" scoped>
.top_dv {
  padding: 0 20px;
}
.my-lit-scrollbar {
  height: 100%;
}
.el-dialog__body {
  padding: 10px 0 0 0;
}
.table-outer{
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  .t_tr{
    width: 100%;
    display: flex;
    justify-content: space-between;
    border-right: 1px solid #e0e0e0;
  }
  .t_td{
    text-align: center;
    padding: 4px;
    flex: 1;
    border: 1px solid #e0e0e0;
    border-bottom: none;
    border-right: none;
  }
}
.t_head{
  width: 100%;
  border-bottom: 1px solid #e0e0e0;
  .t_td{
    font-weight: bold;
    background: #eef1f6;
  }
}
.my-scrollbar{
  flex: 1;
}
.t_body{
  width: 100%;
}
.t_body:last-child{
  border-bottom: 1px solid #e0e0e0;
}
.t_foot{
  .t_td{
    color: #333;
    background: #FDF6EC;
  }
}
.t_td_1{
  flex: none !important;
  width: 70px !important;
}
.t_td_3{
  flex: none !important;
  width: 8% !important;
}
.info-table{
  width: 100%;
  border-collapse: collapse;
  th{
    background: #eef1f6;
  }
  th, td{
    line-height: 38px;
    text-align: center;
    border: 1px solid #e0e0e0;
  }
  tr:last-child td{
    border-bottom: none;
  }
  &:last-child tr:last-child td{
    border-bottom: 1px solid #e0e0e0;
  }
  .sum-row td{
    color: #333;
    background: #FDF6EC;
  }
}
</style>