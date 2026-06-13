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
          <el-form-item label="年份" prop="year">
            <el-date-picker v-model="searchForm.year" type="year" value-format="yyyy"  :picker-options="pickerOptions" placeholder="选择年份" :clearable="false"></el-date-picker>
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
          <template slot-scope="scope" slot="placeType">
            {{ getPlaceType(scope.row.placeType) }}
          </template>
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
import { tableOption } from "@/const/crud/platform/dormitory/water_usage_statistics";

export default {
  name: "dorm_mng",
  data() {
    return {
      pickerOptions:{
        disabledDate(time) {
          return time.getFullYear() > (new Date()).getFullYear()
        }
      },
      exportLoading: false,
      searchForm: {
        year: ''
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
    this.searchForm.year = (new Date()).getFullYear()+'';
    this.getList(this.page, this.searchForm);
  },
  computed: {},
  methods: {
    getPlaceType(val){
      if(val === 1){
        return '厂区'
      } else if(val === 0){
        return '宿舍'
      } else {
        return '-'
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
      if(!this.searchForm.year){
        this.$message.error('请先选择一个年份进行查询');
        return
      }
      this.tableLoading = true;
      waterUsageStatisticsApi.getList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
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
      this.getList(this.page);
    },
    //导出表格
    export2Excel() {
      if(!this.searchForm.year){
        this.$message.error('请先选择一个年份进行导出');
        return
      }
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "区域类型",
          "区域名称",
          "水电类型",
          "设备名称",
          "年用量",
          "1月用量",
          "2月用量",
          "3月用量",
          "4月用量",
          "5月用量",
          "6月用量",
          "7月用量",
          "8月用量",
          "9月用量",
          "10月用量",
          "11月用量",
          "12月用量"
        ];
        const filterVal = [
          "placeType",
          "areaName",
          "sdType",
          "deviceName",
          "yearUse",
          "month1Use",
          "month2Use",
          "month3Use",
          "month4Use",
          "month5Use",
          "month6Use",
          "month7Use",
          "month8Use",
          "month9Use",
          "month10Use",
          "month11Use",
          "month12Use"
        ];
        let params = Object.assign({}, this.searchForm);
        waterUsageStatisticsApi.exportList(this.searchForm)
          .then(response => {
            if(response.data && response.data.data.length>0){
              let list = [];
              response.data.data.forEach((el)=>{
                el.placeType = this.getPlaceType(el.placeType)
                el.sdType = this.getSdType(el.sdType)
                list.push(el)
              })
              const data = this.formatJson(filterVal, list);

              export_json_to_excel(tHeader, data, `水电用量统计信息&(${this.searchForm.year})`);
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
      width: 130px;
    }
  }
  .tabs ::v-deep {
    flex: 1;
    .el-tabs__item{
      height: 50px;
    }
  }
</style>
