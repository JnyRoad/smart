<!--车辆管理：入园审批 -->
<template>
  <div class="my-basic-container vehicle">
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
          <el-form-item label="车牌号" prop="vehiclePlate">
            <el-input v-model="searchForm.vehiclePlate" placeholder="车牌号" clearable></el-input>
          </el-form-item>
          <el-form-item label="申请园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="申请时间" prop="createTime">
            <el-date-picker
              v-model="queryTime"
              type="daterange"
              range-separator="-"
              value-format="yyyy-MM-dd"
              start-placeholder="起始时间"
              end-placeholder="截止时间"
              clearable
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="申请状态" prop="status">
            <el-select v-model="searchForm.status" placeholder="申请状态" clearable>
              <el-option label="审批中" value="0"></el-option>
              <el-option label="已审批" value="1"></el-option>
              <el-option label="已拒绝" value="2"></el-option>
            </el-select>
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
          <template slot-scope="scope" slot="examineStatus">
            <span class="common-status-dot" :class="scope.row.applyStatus|entryExamineClassFormat">
              <i></i>
              {{scope.row.applyStatus|entryExamineFormat}}
            </span>
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleDetail(scope.row,scope.$index)"
            >查看</el-button>
            <el-button
              v-if="scope.row.applyStatus == 0"
              type="text"
              icon="el-icon-circle-check-outline"
              @click="passExamin(scope.row,scope.$index)"
            >通过</el-button>
            <el-button
              v-if="scope.row.applyStatus == 0"
              type="text"
              icon="icon-yutong-forbid"
              @click="refuseSureButton(scope.row,scope.$index)"
            >拒绝</el-button>
          </template>
        </avue-crud>
        <!-- 拒绝原因 -->
        <el-dialog
          title="拒绝原因"
          @close="resetRefuseForm('refuseForm')"
          class="dialog_form"
          :visible.sync="refuseVisible"
          width="400px"
        >
          <el-form :model="refuseForm" :rules="refuseRule" ref="refuseForm">
            <el-form-item label="请输入拒绝原因" prop="reason">
              <el-input type="textarea" v-model="refuseForm.reason" rows="4"></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button @click="resetRefuseForm('refuseForm')">取 消</el-button>
            <el-button type="primary" @click="refuseSure('refuseForm')">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, putObj } from "@/api/platform/vehicle/entry_examine";
import { tableOption } from "@/const/crud/platform/vehicle/entry_examine";
import { mapGetters } from "vuex";

export default {
  name: "vehicle",
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        vehiclePlate: "",
        parkId: "",
        startTime: "",
        endTime: "",
        status: ""
      },
      refuseVisible: false, //拒绝原因
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      queryTime: [],
      refuseForm: {
        reason: "",
        id: "",
        status: ""
      },
      refuseRule: {
        reason: [{ required: true, message: "请输入拒绝原因", trigger: "blur" }]
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption
    };
  },
  created() {
    this.$nextTick(() => {
      if (this.$route.query.queryForm != undefined) {
        let queryPage = this.$route.query.queryPage;
        let queryForm = this.$route.query.queryForm;
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {});
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {});
          this.queryTime = [this.searchForm.startTime, this.searchForm.endTime];
        }
        this.getList(this.page, this.searchForm);
      } else {
        this.getList(this.page);
      }
    });
    // this.getList(this.page);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    getList(page, params) {
      if (this.queryTime.length == 2) {
        this.searchForm.startTime = this.queryTime[0];
        this.searchForm.endTime = this.queryTime[1];
      }
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            descs: "create_time",
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
    // 查看
    handleDetail(row, index) {
      const src = `/platform/vehicle/entry_examine/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
    },
    //通过审核
    passExamin(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("span", null, "是否通过该车辆的入园权限？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          _this.refuseForm.id = row.id;
          _this.refuseForm.status = 1;
          return putObj(_this.refuseForm);
        })
        .then(data => {
          this.getList(this.page, this.searchForm);
        })
        .catch(err => { console.error(err) });
    },
    //点击拒绝按钮
    refuseSureButton(row, index) {
      this.refuseForm.id = row.id;
      this.refuseForm.status = 2;
      this.refuseVisible = true;
    },
    //拒绝
    refuseSure(formName) {
      var _this = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          putObj(this.refuseForm).then(function() {
            _this.refuseVisible = false;
            _this.getList(_this.page, _this.searchForm);
          });
        } else {
          return false;
        }
      });
    },
    resetRefuseForm(formName) {
      //重置，入职邀请
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
      }
      this.refuseVisible = false;
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1;
      this.getList(this.page, this.searchForm);
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
        this.queryTime = [];
        this.page.currentPage = 1;
        this.getList(this.page);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
