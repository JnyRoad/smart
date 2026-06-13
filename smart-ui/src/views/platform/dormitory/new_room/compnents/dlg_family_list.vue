<!--家属列表-->
<template>
  <div>
    <avue-crud
      :data="tableData"
      :option="tableOption"
    >
      <template slot-scope="scope" slot="relation">
      <span>{{ scope.row.relation|fl_relation }}</span>
      </template>
      <template slot-scope="scope" slot="menu">
        <template v-if="scope.row.staffBadge">
          <el-button
            type="text"
            @click="editFamily(scope.row, scope.$index)"
            ><i class="el-icon-edit"></i> 编辑 </el-button
          >
          <el-button
            type="text"
            class="check_out_btn"
            @click="delFamily(scope.row, scope.$index)"
            ><i class="el-icon-delete"></i> 删除 </el-button
          >
        </template>
      </template>
    </avue-crud>
    <div class="btns" >
      <el-button  type="text" @click="addFamily()">
        <i class="el-icon-plus"></i>添加家属
      </el-button>
    </div>
    <dlgAddFamily :row="row" :curFamiley="curFamiley" :mTitle="mTitle" ref="dlgaddfamily" @refresh="getFamily(row.staffBadge)"/>
  </div>
</template>

<script>

import {  delFamily, getFamily } from "../_service.js";
import { tableOption } from "@/const/crud/platform/dormitory/family";
import dlgAddFamily from "./dlg_add_family";

const RELATIONSHIPS = [ '夫妻', '直系血亲','旁系血亲','近姻亲','其他' ]
export default {
  components: {
    dlgAddFamily
  },
  data() {
    return {
      tableOption: tableOption,
      tableData: [],
      curFamiley: {},
      mTitle: '添加家属',
    };
  },
  props: {
    row: undefined,
  },
  watch:{
    row:{
      handler:function(newVal, oldVal) {
        if(newVal&&newVal.staffBadge){
          this.getFamily(newVal.staffBadge);
        }else{
          this.tableData = []
        }
      },
      immediate: true
    }
  },
  filters: {
    fl_relation: function(val) {
      if(val&&val>0){
        return RELATIONSHIPS[val-1]
      }
    },
  },
  created() {},
  mounted() {},
  methods: {
    async delFamily(row){
      const res = await delFamily(row.id)
      this.getFamily(this.row.staffBadge);
    },
    async getFamily(staffBadge){
      const res = await getFamily(staffBadge)
      this.tableData = res.data.data
    },
    //添加家属
    addFamily(){
      this.mTitle = '添加家属'
      this.curFamiley = {}
      this.$refs.dlgaddfamily && this.$refs.dlgaddfamily.open()
    },
    //编辑家属
    editFamily(row){
      this.mTitle = '编辑家属'
      this.curFamiley = row
      this.$refs.dlgaddfamily && this.$refs.dlgaddfamily.open()
    }
  },
};
</script>
<style lang="scss" scoped>
  ::v-deep .el-card__body {
    padding: 0 !important;
  }
  .btns {
    text-align: center;
    margin-bottom: 15px;
  }
</style>