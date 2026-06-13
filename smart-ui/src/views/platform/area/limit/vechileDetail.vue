<!--区域管理，权限策略, 关联车辆  -->
<template>
  <div class="my-basic-container limit">
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
            <el-button type="primary" icon="el-icon-delete" @click="handleDelBatch()">批量删除</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="handleClear()">清空权限</el-button>
          </div>
        </div>
        <div class="form-outer">
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="车牌号" prop="vehiclePlate">
              <el-input v-model="searchForm.vehiclePlate" placeholder="请输入车牌号" clearable></el-input>
            </el-form-item>
            <el-form-item label="车主" prop="personName">
              <el-input v-model="searchForm.personName" placeholder="请输入车主姓名" clearable></el-input>
            </el-form-item>
          </el-form>
        </div>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
          @selection-change="selectChange"
        >
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { getDetailPage, delObj, batchDel, clearAll } from "@/api/platform/area/limit";
import { tableOption } from "@/const/crud/platform/area/limit_vechile";
import { mapGetters } from "vuex";

export default {
  name: "limit",
  data() {
    return {
      searchForm: {
        vehiclePlate: "",
        personName: ""
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      deleteForm: {
        delIds: []
      }
    };
  },
  created() {
    this.page.authId = this.$route.params.id
    this.page.type = this.$route.params.type
    this.deleteForm.authId = this.$route.params.id
    this.deleteForm.type = this.$route.params.type
    this.getList(this.page, this.searchForm);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  watch: {
    $route() {
      this.getList();
    }
  },
  methods: {
    getList(page, params) {
      this.tableLoading = true;
      getDetailPage(
        Object.assign(
          {
            descs: "create_time",
            current: page.currentPage,
            size: page.pageSize,
            authId: page.authId,
            type: page.type
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
    handleDelBatch() {
      var _this = this;
      const elm = this.$createElement;
      if (this.deleteForm.delIds.length == 0) {
        _this.$notify.error({
          title: "提示信息",
          message: "请选择要删除的车辆",
          type: "success",
          duration: 2000
        });
        return;
      }
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除所选车辆信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      }).then(() => {
          return batchDel(this.deleteForm);
        })
        .then(dataResponse => {
          if (dataResponse.data.data) {
            _this.getList(this.page);
            _this.$notify({
              title: "删除成功",
              message: "删除成功",
              type: "success",
              duration: 2000
            });
          } else {
            _this.$notify.error({
              title: "删除失败",
              message: "删除失败",
              type: "error",
              duration: 2000
            });
          }
        })
        .catch(err => { console.error(err) });
    },
    handleClear() {
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认清空所有车辆权限信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      }).then(() => {
          return clearAll(this.deleteForm.authId);
        })
        .then(dataResponse => {
          if (dataResponse.data.data) {
            this.getList(this.page);
            this.$notify({
              title: "删除成功",
              message: "删除成功",
              type: "success",
              duration: 2000
            });
          } else {
            this.$notify.error({
              title: "删除失败",
              message: "删除失败",
              type: "error",
              duration: 2000
            });
          }
        })
        .catch(err => { console.error(err) });
    },
    selectChange(val) {
      //序号那边选择事件
      this.deleteForm.delIds = [];
      if (val.length > 0) {
        val.forEach(function(element) {
          this.deleteForm.delIds.push(element.id);
        }, this);
      }
    },
    handleDel(row, index) {
      //删除
      //this.$refs.crud.rowDel(row, index);
      this.rowDel(row, index);
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
    rowDel: function(row, index) {
      let _this = this;
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
          return delObj(row.id);
        })
        .then(dataResponse => {
          _this.getList(_this.page, _this.searchForm);
          if (dataResponse.data.data) {
            _this.$notify({
              title: "成功",
              message: "删除成功",
              type: "success",
              duration: 2000
            });
          } else {
            _this.$notify({
              title: "失败",
              message: dataResponse.data.msg,
              type: "fail",
              duration: 2000
            });
          }
          // _this.$notify({
          //   title: "成功",
          //   message:"删除成功",
          //   type: "success",
          //   duration: 2000
          // });
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
