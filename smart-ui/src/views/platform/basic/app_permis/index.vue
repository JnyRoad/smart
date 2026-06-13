<!--基础信息，APP权限  -->
<template>
  <div class="my-basic-container app_permis">
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
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加APP权限</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="权限名称" prop="authName">
            <el-input v-model="searchForm.authName" placeholder="权限名称" clearable></el-input>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          :page="page"
          @row-del="rowDel"
          @size-change="sizeChange"
          @current-change="currentChange"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-edit"
              @click="handleEdit(scope.row,scope.$index)"
            >编辑</el-button>
            <el-button
              type="text"
              icon="el-icon-delete"
              :disabled="scope.row.isFix"
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, delById } from "@/api/platform/basic/app_permis";
import { tableOption } from "@/const/crud/platform/basic/app_permis";
import { mapGetters } from "vuex";

export default {
  name: "app_permis",
  data() {
    return {
      searchForm: {
        parkId: null,
        authName: null
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
  created: function() {
    this.getList(this.page);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    getList(page, params) {
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then(response => {
        if (response.data.data) {
          this.tableData = response.data.data.records;
          this.page.total = response.data.data.total;
        }
        this.tableLoading = false;
      });
      this.tableLoading = false;
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
    handleAdd() {
      //点击添加
      const src = `/platform/basic/app_permis/add`;
      this.$router.push({
        path: src
      });
    },
    handleEdit(row, index) {
      //点击编辑
      const src = `/platform/basic/app_permis/edit/${row.id}`;
      this.$router.push({
        path: src
      });
    },
    handleDel(row, index) {
      //删除
      this.$refs.crud.rowDel(row, index);
    },
    rowDel: function(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该权限策略信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delById(row.id);
        })
        .then(response => {
          var msg = response.data.msg;
          var dataResult = response.data.data;
          if (dataResult === true) {
            _this.getList(this.page);
            _this.$notify({
              title: "成功",
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
        .catch(error => { console.error(error) });
    },
    searchSubmit(form) {
      this.page.currentPage = 1;
      this.getList(this.page, form);
    },
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
