<!--车辆管理：员工车辆 -->
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
            <!-- <el-button type="primary" :loading="exportLoading" @click="export2Excel" icon>导出车辆信息</el-button> -->
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加车辆</el-button>
          </div>
        </div>
        <tce-Search-bar>
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="车牌号" prop="vehiclePlate">
              <el-input v-model="searchForm.vehiclePlate" placeholder="车牌号" clearable></el-input>
            </el-form-item>
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="searchForm.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="车辆状态" prop="cardState">
              <el-select v-model="searchForm.cardState" placeholder="车辆状态" clearable>
                <el-option
                  v-for="item in staffStatusiItem"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="创建时间" prop="rangTimeIn">
              <el-date-picker
                v-model="searchForm.rangTimeIn"
                type="daterange"
                range-separator="-"
                value-format="yyyy-MM-dd"
                start-placeholder="起始时间"
                end-placeholder="截止时间"
                clearable
              ></el-date-picker>
            </el-form-item>
          </el-form>
        </tce-Search-bar>
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
          <template slot-scope="scope" slot="staffStatus">
            <span>{{scope.row.staffStatus | staffStatusInit}}</span>
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleDetail(scope.row,scope.$index)"
            >查看</el-button>
            <el-button
              v-if="scope.row.isDelete == 0"
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
import {
  fetchList,
  delObj,
} from "./_service";
import { tableOption } from "@/const/crud/platform/vehicle/staff_vehicle";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
import { staffStatusInit } from '@/filters/index'
export default {
  name: "vehicle",
  data() {
    return {
      exportLoading: false,
      selectCars: [],
      searchForm: {
        //搜索菜单表单
        vehiclePlate: null,
        parkId: null,
        staffStatus: null,
        cardState: null
      },
      depIds: [],
      staffStatusiItem: [{
        label: '挂失', value: 0
      },{
        label: '过期', value: 1
      }],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      deps: []
    };
  },
  watch: {
    depIds(newVal, oldVal) {
      if (isArrayFn(newVal) && newVal.length > 0) {
        const depLength = newVal.length;
        if (depLength == 3) {
          this.searchForm.depId = this.depIds[2];
          this.searchForm.compId = this.depIds[1];
          this.searchForm.parkId = this.depIds[0];
        } else if (depLength >= 2) {
          this.searchForm.depId = undefined;
          this.searchForm.compId = this.depIds[1];
          this.searchForm.parkId = this.depIds[0];
        } else if (depLength >= 1) {
          this.searchForm.depId = undefined;
          this.searchForm.compId = undefined;
          this.searchForm.parkId = this.depIds[0];
        }
      } else {
        this.searchForm.depId = undefined;
        this.searchForm.compId = undefined;
        this.searchForm.parkId = undefined;
      }
    }
  },
  created() {
    this.$nextTick(() => {
      // 详情带参数返回
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
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    //导出
    export2Excel() {
      // require.ensure([], () => {
      //   this.exportLoading = true;
      //   const { export_json_to_excel } = require("@/vendor/Export2Excel");
      //   const tHeader = [
      //     "车主"
      //   ];
      //   const filterVal = [
      //     "name"
      //   ];
      //   let params = Object.assign(
      //     {
      //       descs: "create_time",
      //       current: 1,
      //       size: 10000,
      //       vehicleAscription: 1
      //     },
      //     this.searchForm
      //   );
      //   fetchList(params).then(response => {
      //     const list = response.data.data.records
      //     list.forEach(function(item) {
      //       if (item.isDelete === 0) {
      //         item.isDelete = "已添加";
      //       } else if (item.isDelete === 1) {
      //         item.isDelete = "已删除";
      //       }
      //       item.staffStatus = staffStatusInit(item.staffStatus)
      //     });
      //     const data = this.formatJson(filterVal, list);
      //     export_json_to_excel(tHeader, data, "员工车辆信息");
      //     this.exportLoading = false;
      //   });
      // });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
    handleAdd() {
      const src = `/platform/vehicle/vehicle_maintain/add`;
      this.$router.push({
        path: src
      });
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
        this.$nextTick(() => {
          this.tableData = response.data.data.records;
          this.page.total = response.data.data.total;
          this.tableLoading = false;
        });
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

    handleDetail(row, index) {
      // const src = `/platform/vehicle/vehicle_maintain/detail/${row.id}`;
      // this.$router.push({
      //   path: src,
      //   query: {
      //     pageType: 1,
      //     queryPage: this.page,
      //     queryForm: this.searchForm
      //   }
      // });
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
        this.depIds = [];
        this.getList(this.page);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.topForm ::v-deep {
  .el-form-item__label {
    width: 130px;
  }
}
</style>
