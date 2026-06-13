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
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="工号" prop="staffBadge">
            <el-input v-model="searchForm.staffBadge" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="staffName">
            <el-input v-model="searchForm.staffName" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="申请时间" prop="applyDate">
            <el-date-picker
              v-model="searchForm.applyDate"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="申请时间"
              clearable
            ></el-date-picker>
            <!-- <el-date-picker
              v-model="searchForm.rangTimeIn"
              type="daterange"
              range-separator="-"
              value-format="yyyy-MM-dd"
              start-placeholder="起始时间"
              end-placeholder="截止时间"
              clearable
            ></el-date-picker> -->
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="searchForm.status" clearable placeholder="状态">
              <el-option label="申请中" :value="1"></el-option>
              <el-option label="申请通过" :value="2"></el-option>
              <el-option label="已退回" :value="3"></el-option>
              <el-option label="已撤销" :value="4"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="园区">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
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
          <template slot-scope="scope" slot="compName">
            <el-tooltip placement="bottom">
              <div slot="content" style="line-height: 25px">
                部门：{{scope.row.depName}}<br/>
                职层：{{scope.row.jcheName}}<br/>
              </div>
              <div style="cursor: pointer">
                <!-- <i class="el-icon-info"></i> -->
                <span class="ft-blue">{{ scope.row.compName}}</span>
              </div>
            </el-tooltip>
          </template>
          <template slot-scope="scope" slot="status">
            {{scope.row.status | fl_getStatus}}
          </template>
          <template slot-scope="scope" slot="menu">
            <!-- 状态为申请中  才可以操作 -->
            <el-button
              type="text"
              @click="handleAuto(scope.row,scope.$index)"
              :disabled="scope.row.status!==1"
            >自动分配</el-button>
            <el-button
              type="text"
              @click="handleManual(scope.row,scope.$index)"
              :disabled="scope.row.status!==1"
            >手动分配</el-button>
            <el-button
              type="text"
              @click="handleBack(scope.row,scope.$index)"
              :disabled="scope.row.status!==1"
            >退回</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <dlgSendBack
      :visible="dlg1Visible"
      :row="curDlg1Obj"
      @dlgdo="dlg1do"
      @dlgdoSuccess="dlg1doSuccess"
    />
    <dlgDormManual
      title="分配宿舍"
      :visible="dlg5Visible"
      :row="curDlg5Obj"
      @dlgdo="dlg5do"
      @dlgdoSuccess="dlg5doSuccess"
    />
    <dlgAutoCheckIn
      :visible="dlg2Visible"
      :row="curDlg2Obj"
      @dlgdo="dlg2do"
      @dlgdoSuccess="dlg2doSuccess"
    />
  </div>
</template>

<script>
import { fetchList, recommend } from "./_service.js";
import { tableOption } from "@/const/crud/platform/dormitory/check_in_apply";
import dlgSendBack from "./compnents/dlg_send_back";
import dlgAutoCheckIn from "./compnents/dlg_auto_check_in";
import { validatenull } from "@/util/validate";
import dlgDormManual from "./compnents/dlg_dorm_manual";

export default {
  name: "type",
  components: {
    dlgSendBack,
    dlgDormManual,
    dlgAutoCheckIn
  },
  data() {
    return {
      dlg5Visible: false, //手动分配
      curDlg5Obj: {},
      dlg1Visible: false, //退回
      curDlg1Obj: null,
      dlg2Visible: false, //自动分配
      curDlg2Obj: null,
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      searchForm: {
        applyDate: ''
      }
    };
  },
  created() {
    this.getList(this.page);
  },
  filters: {
    fl_getStatus: function(val) {
      if(validatenull(val)){
        return
      }
      const arr = ['申请中','申请通过','已退回','已撤销']
      return  arr[val-1]
    }
  },
  mounted: function() {},
  computed: {},
  methods: {
    dlg5do(val){
      this.dlg5Visible = val
    },
    dlg5doSuccess(val){
      this.dlg5Visible = false
      this.getList(this.page, this.searchForm);
    },
    dlg1do(val){
      this.dlg1Visible = val
    },
    dlg1doSuccess(){
      this.dlg1Visible = false
      this.getList(this.page, this.searchForm);

    },
    dlg2do(val){
      this.dlg2Visible = val
    },
    dlg2doSuccess(){
      this.dlg2Visible = false
      this.getList(this.page, this.searchForm);
    },
    handleBack(row){
      this.dlg1Visible = true
      this.curDlg1Obj = row
    },
    //自动分配
    handleAuto(row){
      this.recommend(row.id)

    },
    async recommend(id) {
      const res = await recommend({applyId: id})
      if(res.data.code===0){
        this.dlg2Visible = true
        this.curDlg2Obj = Object.assign({applyId: id}, res.data.data)
      }
    },
    //手动
    handleManual(row){
      this.dlg5Visible = true
      this.curDlg5Obj = Object.assign( row )
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
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      this.searchForm.parkId = undefined;
      this.page.currentPage = 1;
      this.getList(this.page);
    },
    /**
     * 搜索回调
     */
    searchSubmit() {
      this.page.currentPage = 1;
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
