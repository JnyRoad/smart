<!-- 水电用量统计  -->
<template>
  <div class="my-basic-container room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >重置</el-button>
            <el-button
              type="primary"
              :loading="exportLoading"
              @click="export2Excel"
              icon="icon-yutong-download"
            >导出</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" size="mini" class="topForm">
          <el-form-item label="时间" prop="year" style="display: block">
            <div style="display: flex; align-items: center; width: 530px;">
              <el-radio-group v-model="dateType" @change="dateTypeChange">
                <el-radio-button :label="1">今日</el-radio-button>
                <el-radio-button :label="2">本月</el-radio-button>
                <el-radio-button :label="3">上月</el-radio-button>
                <el-radio-button :label="4">今年</el-radio-button>
                <el-radio-button :label="5">去年</el-radio-button>
              </el-radio-group>
              <el-date-picker
                v-model="dates"
                type="daterange"
                range-separator="-"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :picker-options="pickerOptions"
                @change="datesChange"
                value-format="yyyy-MM-dd"
                :clearable="false"
                style="width: 230px !important; margin-left: 10px;">
              </el-date-picker>
            </div>
          </el-form-item>
          <el-form-item label="类型" prop="year">
            <el-radio-group v-model="searchForm.sdType">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button :label="2">冷水</el-radio-button>
              <el-radio-button :label="1">热水</el-radio-button>
              <el-radio-button :label="3">电表</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="所在区域" prop="areaId">
            <areaCascader v-model="areaId" :changeOnSelect="true" placeholder="所在区域"></areaCascader>
          </el-form-item>
          <el-form-item label="设备标签" prop="deviceTagList">
            <deviceTagSelect v-model="searchForm.deviceTagList"></deviceTagSelect>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
        >
          <template slot-scope="scope" slot="sdType">
            {{ getSdType(scope.row.sdType) }}
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { waterUsageStatisticsApi } from './_service'
import { tableOption } from "@/const/crud/platform/dormitory/water_usage_statistics_new";
import { dateFormat2 } from "@/util/date";
import deviceTagSelect from "../../device/components/device-tag-select"
export default {
  name: "dorm_mng",
  components: {
      deviceTagSelect
    },
  data() {
    return {
      dateType: 1,
      areaId: [],
      dates: [],
      pickerOptions:{
        disabledDate(time) {
          return time.getFullYear() > (new Date()).getFullYear()
        }
      },
      areaOptions: [],
      exportLoading: false,
      searchForm: {
        startDate: '',
        endDate: '',
        areaId: '',
        sdType: '', //1热水，2冷水，3电
        deviceTagList: []
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    };
  },
  created() {},
  mounted: function() {
    this.initDate()
    this.getList(this.page, this.searchForm);
  },
  computed: {},
  methods: {
    initDate(){
      this.dateType = 1
      this.searchForm.startDate = dateFormat2(new Date())
      this.searchForm.endDate = dateFormat2(new Date())
      this.dates = [this.searchForm.startDate, this.searchForm.endDate]
    },
    dateTypeChange(val){
      let start = ''
      let end = ''
      switch(val){
        case 1: //今日
          start = dateFormat2(new Date())
          end = dateFormat2(new Date())
          break
        case 2: //本月
          start = dateFormat2(new Date()).slice(0, 8)+ '01'
          end = dateFormat2(new Date())
          break
        case 3: //上月
          let year = (new Date()).getFullYear()
          let month = (new Date()).getMonth()
          if(month > 0){
            start = `${year}-${(month<10) ? ('0' + month) : month}-01`
            let lastDay = new Date(year, month, 0).getDate()
            end = `${year}-${(month<10) ? ('0' + month) : month}-${(lastDay<10) ? ('0' + lastDay) : lastDay}`
          }else{
            start = `${year-1}-12-01`
            let lastDay = new Date(year-1, 12, 0).getDate()
            end = `${year-1}-12-${(lastDay<10) ? ('0' + lastDay) : lastDay}`
          }
          break
        case 4: //今年
          start = dateFormat2(new Date()).slice(0, 5)+ '01-01'
          end = dateFormat2(new Date())
          break
        case 5: //去年
          start = (new Date()).getFullYear() - 1 + '-01-01'
          end = (new Date()).getFullYear() - 1 + '-12-31'
          break
      }
      this.searchForm.startDate = start
      this.searchForm.endDate = end
      this.dates = [start, end]
    },
    datesChange(val){
      if(val){
        this.dateType = ''
        this.searchForm.startDate = val[0]
        this.searchForm.endDate = val[1]
      }else{
        this.dateType = 1
      }
    },
    getSdType(val){
      if(val === 1){
        return '热水'
      } else if(val === 2){
        return '冷水'
      } else if(val === 3){
        return '电'
      } else {
        return '-'
      }
    },
    getList(page, params) {
      this.tableLoading = true;
      if(!params){
        params = {}
      }
      if(this.areaId.length>0){
        params.areaId = this.areaId[this.areaId.length-1]
      }else{
        params.areaId = undefined
      }
      waterUsageStatisticsApi.getList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then(response => {
        if(response.data.data){
          this.tableData = response.data.data.records;
          this.page.total = response.data.data.total;
        }else{
          this.tableData = [];
          this.page.total = 0;
        }
        this.tableLoading = false;
      });

      this.tableLoading = false;
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page, this.searchForm);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page, this.searchForm);
    },
    searchSubmit(form) {
      //搜索
      this.page.currentPage = 1;
      this.getList(this.page, form);
    },
    resetFrom(formName) {
      //清空
      this.$refs[formName].resetFields();
      this.page.currentPage = 1;
      this.initDate()
      this.areaId = []
      this.searchForm.sdType = ''
      this.getList(this.page, this.searchForm);
    },
    //导出表格
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "所在区域",
          "设备名称",
          "表类型",
          "设备标签",
          "通讯地址",
          "关联集中器",
          "查询开始时间",
          "查询结束时间",
          "查询段起数",
          "查询段止数",
          "查询段累计用量"
        ];
        const filterVal = [
          "areaName",
          "deviceName",
          "sdType",
          "deviceTag",
          "commAddress",
          "concentratorName",
          "startDate",
          "endDate",
          "startNum",
          "endNum",
          "sumNum"
        ];
        let params = Object.assign({}, this.searchForm);
        waterUsageStatisticsApi.exportList(params)
          .then(response => {
            if(response.data && response.data.data.length>0){
              let list = [];
              response.data.data.forEach((el)=>{
                el.sdType = this.getSdType(el.sdType)
                list.push(el)
              })
              const data = this.formatJson(filterVal, list);

              export_json_to_excel(tHeader, data, `水电用量统计信息&(${this.dates})`);

            }else{
              this.$message({
                message: '当前没有符合条件的数据可导出',
                type: 'warning'
              });
            }
            this.exportLoading = false;
          })
          .catch(err => {
            this.exportLoading = false;
          });
      });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
  }
};
</script>
<style lang="scss" scoped>
  .topForm ::v-deep {
    .el-form-item__label {
      width: 70px;
    }
    .el-radio-button--mini .el-radio-button__inner{
      padding: 9px 15px;
    }
    .el-radio-button__orig-radio:checked+.el-radio-button__inner{
      color: #fff;
      background: #ed6d00;
      border-color: #ed6d00;
      box-shadow: -1px 0 0 0 #ed6d00;
    }
    .el-radio-button__inner:hover{
      color: #ed6d00;
      border-color: #ed6d00;
      box-shadow: -1px 0 0 0 #ed6d00;
    }
  }
  .tabs ::v-deep {
    flex: 1;
    .el-tabs__item{
      height: 50px;
    }
  }
</style>
