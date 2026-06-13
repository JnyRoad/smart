<!--园区服务：调查表管理 -->
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
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加调查表</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="调查表名称" prop="title">
            <el-input v-model="searchForm.title" placeholder="调查表名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="searchForm.status" placeholder="状态" clearable>
              <template v-for="(item, index) in statusItem">
                <el-option :label="item" :value="index" :key="index"></el-option>
              </template>
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
          @row-del="rowDel"
        >
          <template slot-scope="scope" slot="status">
            <span>{{scope.row.status | statusInit}}</span>
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleEdit(scope.row,scope.$index)"
            >查看</el-button>
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleDetail(scope.row,scope.$index)"
            >调查结果</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchList,
  getById,
  putObj,
  delObj
} from "@/api/platform/park_service/paper";
import { tableOption } from "@/const/crud/platform/park_service/paper";
import { mapGetters } from "vuex";
const statusOption = ["未开始", "进行中", "已结束"];
export default {
  name: "vehicle",
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        parkId: undefined,
        title: undefined,
        status: undefined
      },
      statusItem: statusOption, //状态
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
  filters: {
    statusInit(id) {
      return statusOption[id];
    }
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    handleAdd() {
      const src = `/platform/park_service/paper/add`;
      this.$router.push({
        path: src
      });
    },
    handleEdit(row) {
      const src = `/platform/park_service/paper/add`;
      this.$router.push({
        path: src,
        query: {
          id: row.id
        }
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
        (this.tableData = response.data.data.records),
          (this.page.total = response.data.data.total),
          (this.tableLoading = false);
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
      const src = `/platform/park_service/paper/detail/${row.id}`;
      // const  src = `/platform/entrance/face/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
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
          elm("span", null, "确认删除该调查表信息？ ")
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
        this.getList(this.page);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.topForm ::v-deep {
  .el-form-item__label {
    width: 120px;
  }
}
</style>
