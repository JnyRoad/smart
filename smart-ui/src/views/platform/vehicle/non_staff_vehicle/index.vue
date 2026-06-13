<!--车辆管理：非员工车辆 -->
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
            <el-button type="primary" :loading="exportLoading" @click="export2Excel" icon>导出车辆信息</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加车辆</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="车牌号" prop="vehiclePlate">
            <el-input v-model="searchForm.vehiclePlate" placeholder="车牌号" clearable></el-input>
          </el-form-item>
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="车辆权限" prop="authorityId">
            <authCarSelect
              :parkId="searchForm.parkId"
              v-model="searchForm.authorityId"
            ></authCarSelect>
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
          @row-del="rowDel"
        >
          <template slot-scope="scope" slot="remark">{{scope.row.remark || '-'}}</template>
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleDetail(scope.row,scope.$index)"
            >查看</el-button>
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, delObj } from "@/api/platform/vehicle/non_staff_vehicle";
import { tableOption } from "@/const/crud/platform/vehicle/non_staff_vehicle";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";

export default {
  name: "vehicle",
  data() {
    return {
      exportLoading: false,
      selectCars: [],
      searchForm: {
        //搜索菜单表单
        vehiclePlate: "",
        parkId: "",
        authorityId: ""
      },
      compId: "",
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption
    };
  },
  created: function() {
    this.$nextTick(() => {
      if (this.$route.query.queryForm != undefined) {
        let queryPage = this.$route.query.queryPage;
        let queryForm = this.$route.query.queryForm;
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {});
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {});
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
  watch: {
    $route() {
      this.getList(this.page);
    }
  },
  methods: {
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "对口人员",
          "车牌号",
          "电话",
          "所属园区",
          "备注"
        ];
        const filterVal = [
          "name",
          "vehiclePlate",
          "phone",
          "parkName",
          "remark"
        ];

        let params = Object.assign(
          {
            descs: "create_time",
            current: 1,
            size: 10000
          },
          this.searchForm
        );
        fetchList(params).then(response => {
          const list = response.data.data.records
          const data = this.formatJson(filterVal, list);
          export_json_to_excel(tHeader, data, "临时车辆信息");
          this.exportLoading = false;
        });
      });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
    getList(page, params) {
      this.tableLoading = true;

      params = Object.assign(
        {
          descs: "create_time",
          current: page.currentPage,
          size: page.pageSize
        },
        params
      );

      fetchList(params).then(response => {
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
    handleAdd() {
      const src = `/platform/vehicle/non_staff_vehicle/add`;
      this.$router.push({
        path: src
      });
    },
    handleDel(row, index) {
      this.$refs.crud.rowDel(row, index);
    },
    rowDel: function(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该车辆信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj(row.id);
        })
        .then(data => {
          this.getList(this.page, this.searchForm);
          _this.$notify({
            title: "成功",
            message: "删除成功",
            type: "success"
          });
        })
        .catch(err => { console.error(err) });
    },
    handleDetail(row, index) {
      // const src = `/platform/vehicle/staff_vehicle/detail/${row.id}`;
      const src = `/platform/vehicle/non_staff_vehicle/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          pageType: 3,
          queryPage: this.page,
          queryForm: this.searchForm
        }
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
        this.getList(this.page);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
