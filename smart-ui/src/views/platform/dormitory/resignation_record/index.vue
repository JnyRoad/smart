<!--入住申请  -->
<template>
  <div class="my-basic-container">
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
            >清空</el-button>
            <el-button type="primary" :loading="exportLoading" @click="export2Excel" icon>导出</el-button>
            <el-button type="primary" @click="settlementRule">日志存储策略</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
           <el-form-item label="BU" prop="bu">
            <el-input v-model="searchForm.bu" placeholder="BU" clearable></el-input>
          </el-form-item>
          <el-form-item label="工号" prop="badge">
            <el-input v-model="searchForm.badge" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="searchForm.name" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="离职日期" prop="leaveDate">
            <el-date-picker
              v-model="searchForm.leaveDate"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="离职日期"
              clearable
            ></el-date-picker>
          </el-form-item>
          <!-- <el-form-item label="状态" prop="status">
            <el-select v-model="searchForm.status" clearable placeholder="状态">
              <el-option label="申请中" :value="1"></el-option>
              <el-option label="申请通过" :value="2"></el-option>
              <el-option label="已退回" :value="3"></el-option>
              <el-option label="已撤销" :value="4"></el-option>
            </el-select>
          </el-form-item> -->
          <el-form-item label="结算时间" prop="times">
            <el-date-picker
              v-model="searchForm.times"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="结算时间"
              clearable
            ></el-date-picker>
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
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              @click="handleDetail(scope.row,scope.$index)"
            >详情</el-button>
            <!-- <el-button
              type="text"
              icon="el-icon-edit"
              @click="handleDetail(scope.row,scope.$index)"
            >调用日志</el-button> -->
          </template>
        </avue-crud>
        <!-- 结算规则 -->
        <SettlementRules ref="settlementRules"/>
        <!-- 详情 -->
        <DetailsItem ref="detailsItem" :detailInfo="detailInfo"/>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, recommend } from "./_service.js";
import { tableOption } from "@/const/crud/platform/dormitory/resignation_record";
import SettlementRules from './components/settlement_rule'
import DetailsItem from './components/detail'

export default {
  name: "type",
  components: {
    SettlementRules,
    DetailsItem
  },
  data() {
    return {
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      searchForm: {
      },
      exportLoading: false,
      detailInfo: {}
    };
  },
  created() {
    this.getList(this.page);
  },
  filters: {
  },
  mounted: function() {},
  computed: {},
  methods: {
    handleDetail(row){
      this.detailInfo = row
      this.$refs.detailsItem.open()
    },
    settlementRule(){
      this.$refs.settlementRules.open()
    },
    getList(page, params) {
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            // descs: 'create_time',
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
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true
        const { export_json_to_excel } = require('@/vendor/Export2Excel')
        const tHeader = ['园区', '工号', '姓名', 'BU', '部门', '个人扣款', '离职日期', '状态', '生成时间']
        const filterVal = ['parkName', 'badge','name', 'bu',  'dept', 'fee', 'leaveDate', 'status', 'createTime']

        let params = Object.assign(
          {
            current: 1,
            size: 10000
          },
          this.searchForm
        )
        var _this = this
        fetchList(params)
          .then((response) => {
            const list = response.data.data.records
            // list.forEach(function (item) {
            //   item.status = staffStatusInit(item.status)
            //   item.facePicId = item.facePicId ? '有' : '无'
            //   item.deviceAuth = item.deviceAuth ? item.deviceAuth : '未分配'
            //   item.appAuth = item.appAuth ? item.appAuth : '未分配'
            // })
            const data = this.formatJson(filterVal, list)
            export_json_to_excel(tHeader, data, '离职水电生成记录')
            this.exportLoading = false
          })
          .catch((err) => {
            this.exportLoading = false
          })
      })
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map((v) => filterVal.map((j) => v[j]))
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
        this.page.currentPage = 1;
        this.getList(this.page);
      }
    },
    /**
     * 搜索回调
     */
    searchSubmit() {
      this.page.currentPage = 1;
      if(this.searchForm.leaveDate){
        this.searchForm['startLeaveDate'] = this.searchForm.leaveDate + ' 00:00:00'
        this.searchForm['endLeaveDate'] = this.searchForm.leaveDate + ' 23:59:59'
      }else{
        this.searchForm['startLeaveDate'] = null
        this.searchForm['endLeaveDate'] = null
      }
      if(this.searchForm.times){
        this.searchForm['startTime'] = this.searchForm.times + ' 00:00:00'
        this.searchForm['endTime'] = this.searchForm.times + ' 23:59:59'
      }else{
        this.searchForm['startTime'] = null
        this.searchForm['endTime'] = null
      }
      this.getList(this.page, this.searchForm);
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page);
    }
  }
};
</script>
