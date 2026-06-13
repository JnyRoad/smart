<!--物流车预约：通行记录 -->
<template>
  <div class="my-basic-container logistics_vehicle">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-radio-group v-model="reverve_status" @change="radioChange">
            <el-radio-button :label="1">已预约</el-radio-button>
            <el-radio-button :label="2">已到达</el-radio-button>
            <!-- <el-radio-button :label="4">今日超时</el-radio-button> -->
            <el-radio-button :label="3">已离开</el-radio-button>
          </el-radio-group>
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
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="供应商" prop="supplier">
            <el-input v-model="searchForm.supplier" placeholder="供应商" clearable></el-input>
          </el-form-item>
          <el-form-item label="车牌号" prop="vehiclePlate">
            <el-input v-model="searchForm.vehiclePlate" placeholder="车牌号" clearable></el-input>
          </el-form-item>
          <el-form-item label="司机姓名" prop="driverName">
            <el-input v-model="searchForm.driverName" placeholder="司机姓名" clearable></el-input>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          @size-change="sizeChange"
          @current-change="currentChange"
          :option="tableOption"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleDetail(scope.row,scope.$index)"
            >详情</el-button>
            <!-- <el-button type="text"
                          v-if="!orderHide"
                          icon="icon-yutong-manualIn"
                          @click="manualEnter(scope.row,scope.$index)">手动进厂
                </el-button>
                <template v-if="!arivalHide">
                  <el-button type="text"
                            icon="icon-yutong-tdBack"
                            @click="goOrder(scope.row,scope.$index)">返回预约
                  </el-button>
                  <el-button type="text"
                            v-if="!arivalButtonHide"
                            icon="icon-yutong-manualIn"
                            @click="manualLeave(scope.row,scope.$index)">手动离厂
                  </el-button>
                </template>
                <el-button type="text"
                          v-if="reverve_status==4"
                          icon="icon-yutong-cancelOrder"
                          @click="cancelOrder(scope.row,scope.$index)">取消预约
                </el-button>
                <el-button type="text"
                          v-if="!leaveHide"
                          icon="icon-yutong-tdBack"
                          @click="goIn(scope.row,scope.$index)">返回在厂
            </el-button>-->
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchList,
  putObj,
  manualEnter,
  goOrder,
  manualLeave,
  cancelOrder,
  goIn
} from "@/api/platform/logistics_vehicle/reserve_record";
// import {tableOption} from '@/const/crud/platform/logistics_vehicle/reserve_record'
import { mapGetters } from "vuex";

export default {
  name: "logistics_vehicle",
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        supplier: "",
        vehiclePlate: "",
        driverName: "",
        status: 1
      },
      reverve_status: 1,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableLoading: false,
      tableData: []
    };
  },
  created: function() {
    this.getList(this.page);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"]),
    tableOption() {
      return {
        border: false,
        index: true,
        indexLabel: "序号",
        stripe: true,
        menuAlign: "center",
        menuWidth: 200,
        align: "center",
        refreshBtn: false,
        columnBtn: false,
        searchBtn: false,
        showClomnuBtn: false,
        searchSize: "mini",
        addBtn: false,
        editBtn: false,
        delBtn: false,
        viewBtn: false,
        props: {
          label: "label",
          value: "value"
        },
        column: [
          {
            label: "主键",
            prop: "id",
            type: "input",
            hide: true
          },
          {
            label: "所属园区",
            prop: "parkName",
            type: "input"
          },
          {
            label: "供应商",
            prop: "supplier",
            type: "input"
          },
          {
            label: "车牌号",
            prop: "vehiclePlate",
            type: "input"
          },
          {
            label: "司机姓名",
            prop: "driverName",
            type: "input"
          },
          {
            label: "司机手机号",
            prop: "driverPhone"
          },
          {
            label: "预约开始时间",
            prop: "startTime"
          },
          // {
          //   label: '预约结束时间',
          //   prop: 'endTime'
          // },
          {
            label: "提交时间",
            prop: "createTime",
            hide: this.orderHide
          },
          {
            label: "进厂时间",
            prop: "arrivalTime",
            hide: this.arivalHide
          },
          {
            label: "离厂时间",
            prop: "leaveTime",
            hide: this.leaveHide
          }
        ]
      };
    },
    orderHide: function() {
      //已预约
      return this.reverve_status == 1 ? null : true;
    },
    arivalHide: function() {
      //已到达时，要将达到时间显示出来，将该状态更新为null
      return this.reverve_status == 2 || this.reverve_status == 3 ? null : true;
    },
    arivalButtonHide: function() {
      //已到达时，要将达到时间显示出来，将该状态更新为null
      return this.reverve_status == 2 && this.reverve_status != 3 ? null : true;
    },
    leaveHide: function() {
      //已离开
      return this.reverve_status == 3 ? null : true;
    }
  },
  methods: {
    getList(page, params) {
      this.searchForm.status = this.reverve_status;
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            descs: "create_time",
            current: page.currentPage,
            size: page.pageSize,
            status: 1
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
    radioChange() {
      // this.resetFrom('searchForm');
      this.searchForm.status = this.reverve_status;
      this.tableLoading = true;
      fetchList(
        Object.assign({
          descs: "create_time",
          current: 1,
          size: 20,
          status: this.reverve_status
        })
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
      this.tableLoading = false;
    },
    // 手动进厂
    manualEnter(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        title: "",
        message: elm("div", { attrs: { class: "imgDialogInner" } }, [
          elm("span", { attrs: { class: "imgInfo" } }, ""),
          elm("span", null, "是否手动确认本预约车辆进厂？")
        ]),
        confirmButtonText: "确定",
        customClass: "img_dialog",
        center: true
      })
        .then(function() {
          return manualEnter(row.id);
        })
        .then(data => {
          this.getList(this.page, this.searchForm);
          _this.$message({
            showClose: true,
            message: "手动进厂成功",
            type: "success"
          });
        })
        .catch(err => { console.error(err) });
    },
    // 返回预约
    goOrder(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        title: "",
        message: elm("div", { attrs: { class: "imgDialogInner" } }, [
          elm("span", { attrs: { class: "imgInfo" } }, ""),
          elm("span", null, "是否将该车辆退回预约列表？")
        ]),
        confirmButtonText: "确定",
        customClass: "img_dialog",
        center: true
      })
        .then(function() {
          return goOrder(row.id);
        })
        .then(data => {
          this.getList(this.page, this.searchForm);
          _this.$message({
            showClose: true,
            message: "退回预约成功",
            type: "success"
          });
        })
        .catch(err => { console.error(err) });
    },
    // 手动离厂
    manualLeave(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        title: "",
        message: elm("div", { attrs: { class: "imgDialogInner" } }, [
          elm("span", { attrs: { class: "imgInfo" } }, ""),
          elm("span", null, "是否确认本车辆已经离开厂区？")
        ]),
        confirmButtonText: "确定",
        customClass: "img_dialog",
        center: true
      })
        .then(function() {
          return manualLeave(row.id);
        })
        .then(data => {
          this.getList(this.page, this.searchForm);
          _this.$message({
            showClose: true,
            message: "手动离厂成功",
            type: "success"
          });
        })
        .catch(err => { console.error(err) });
    },
    // 取消预约
    cancelOrder(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        title: "",
        message: elm("div", { attrs: { class: "imgDialogInner" } }, [
          elm("span", { attrs: { class: "imgInfo" } }, ""),
          elm("span", null, "是否取消本条预约？")
        ]),
        confirmButtonText: "确定",
        customClass: "img_dialog",
        center: true
      })
        .then(function() {
          return cancelOrder(row.id);
        })
        .then(data => {
          this.getList(this.page, this.searchForm);
          _this.$message({
            showClose: true,
            message: "取消预约成功",
            type: "success"
          });
        })
        .catch(err => { console.error(err) });
    },
    // 返回在厂
    goIn(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        title: "",
        message: elm("div", { attrs: { class: "imgDialogInner" } }, [
          elm("span", { attrs: { class: "imgInfo" } }, ""),
          elm("span", null, "是否将本条离厂车辆回退到在厂状态？")
        ]),
        confirmButtonText: "确定",
        customClass: "img_dialog",
        center: true
      })
        .then(function() {
          return goIn(row.id);
        })
        .then(data => {
          this.getList(this.page, this.searchForm);
          _this.$message({
            showClose: true,
            message: "手动进厂成功",
            type: "success"
          });
        })
        .catch(err => { console.error(err) });
    },

    handleDetail(row, index) {
      const src = `/platform/logistics_vehicle/reserve_record/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {}
      });
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1;
      this.getList(this.page, form);
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
        this.page.currentPage = 1;
        this.getList(this.page, this.searchForm);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
