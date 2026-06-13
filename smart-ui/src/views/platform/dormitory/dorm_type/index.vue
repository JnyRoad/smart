<!--宿舍管理，房间管理，宿舍分类  -->
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
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加分类</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="所属园区" prop="parkId">
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
          @row-save="rowAdd"
          @row-update="rowEdit"
          @row-del="rowDel"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>
            <el-button
              type="text"
              icon="el-icon-edit"
              @click="handleEdit(scope.row,scope.$index)"
            >编辑</el-button>
          </template>
          <template slot="jchesForm" slot-scope="scope">
            <avue-crud-select
              v-model="scope.row.jches"
              placeholder="请选择所属职层"
              multiple
              :dic="jcheIdData"
              :props="defaultProps"
            ></avue-crud-select>
          </template>
          <template slot="parkIdForm" slot-scope="scope">
            <avue-crud-select
              v-model="scope.row.parkId"
              placeholder="请选择所属园区"
              :dic="parkData"
              :props="parkProps"
            ></avue-crud-select>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchList,
  addObj,
  delObj,
  putObj
} from "@/api/platform/dormitory/dorm_type";
import { allPark, getJchesObj } from "@/api/platform/_publicService";
import { tableOption } from "@/const/crud/platform/dormitory/dorm_type";
import { mapGetters } from "vuex";

export default {
  name: "type",
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
        //搜索菜单表单

        parkId: undefined
      },
      editform: {
        jches: [],
        jcheName: "",
        parkId: undefined
      },
      parkData: [],
      jcheIdData: [],
      parkProps: {
        label: "parkName",
        value: "id"
      },
      defaultProps: {
        label: "typeName",
        value: "typeCode"
      }
    };
  },
  created() {
    this.getParkData();
    this.getJches();
    this.getList(this.page);
  },
  mounted: function() {},
  computed: {},
  methods: {
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
        this.tableData.forEach(element => {
          if(element.levelNames != null && element.levelNames.length > 0 && element.levelNames[0] != null){
            element['jcheName'] = element.levelNames.join(',')
          }
        });
        // jcheName
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
      this.tableLoading = false;
    },
    getParkData() {
      allPark().then(response => {
        this.parkData = response.data.data;
      });
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
    },
    handleAdd(params, done, loading) {
      this.$refs.crud.rowAdd(params, done, loading);
    },
    rowAdd: function(params, done, loading) {
      params.jcheName = this.editform.jcheName;
      addObj(params)
        .then(response => {
          var msg = response.data.msg;
          var dataResult = response.data.data;
          if (dataResult === true) {
            done();
            this.getList(this.page);
            this.$notify({
              title: "添加宿舍分类成功",
              message: msg,
              type: "success",
              duration: 2000
            });
          } else if (dataResult === false) {
            loading();
            this.$notify({
              title: "添加宿舍分类失败",
              message: msg,
              type: "error",
              duration: 2000
            });
          }
        })
        .catch(error => {
          loading();
          this.$notify({
            title: "添加宿舍分类失败",
            message: error.message,
            type: "error",
            duration: 2000
          });
        });
    },

    // 获取职层列表
    getJches: function() {
      getJchesObj().then(response => {
        this.jcheIdData = response.data.data;
      });
    },
    handleEdit(row, index, done, loading) {
      row.jches = row.levelIds;
      this.$refs.crud.rowEdit(row, index, done, loading);
    },

    rowEdit: function(row, index, done, loading) {
      row.jcheName = this.editform.jcheName;
      putObj(row)
        .then(response => {
          var msg = response.data.msg;
          var dataResult = response.data.data;
          if (dataResult === true || dataResult === 1) {
            this.getList(this.page);
            done();
            this.$notify({
              title: "修改宿舍分类成功",
              message: msg,
              type: "success",
              duration: 2000
            });
          } else if (dataResult === false) {
            loading();
            this.$notify({
              title: "修改宿舍分类失败",
              message: msg,
              type: "error",
              duration: 2000
            });
          }
        })
        .catch(error => {
          loading();
          this.$notify({
            title: "修改宿舍分类失败",
            message: error.message,
            type: "error",
            duration: 2000
          });
        });
    },

    selectChange: function(val) {},
    handleDel(row, index) {
      this.$refs.crud.rowDel(row, index);
    },
    rowDel: function(row, index) {
      //删除
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该宿舍分类信息？ ")
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
        .then(dataResponse => {
          var msg = dataResponse.data.msg;
          var dataResult = dataResponse.data.data;
          if (dataResult == true) {
            _this.getList(this.page);
            _this.$notify({
              title: "删除成功",
              message: "删除成功",
              type: "success",
              duration: 2000
            });
          } else if (dataResult === false) {
            _this.$notify({
              title: "删除失败",
              message: msg,
              type: "error",
              duration: 2000
            });
          }
        })
        .catch(error => {
          this.$notify({
            title: "删除宿舍分类失败",
            message: error.message,
            type: "error",
            duration: 2000
          });
        });
    }
  }
};
</script>
