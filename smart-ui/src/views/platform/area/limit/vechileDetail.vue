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
            <el-button
              :loading="batchDeleting"
              :disabled="batchDeleting || clearing"
              type="primary"
              icon="el-icon-delete"
              @click="handleDelBatch()"
            >批量删除</el-button>
            <el-button
              :loading="clearing"
              :disabled="batchDeleting || clearing"
              type="primary"
              icon="el-icon-delete"
              @click="handleClear()"
            >清空权限</el-button>
            <el-button
              plain
              icon="el-icon-time"
              @click="operationProgressVisible = true"
            >权限任务</el-button>
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
    <AuthOperationProgress v-model="operationProgressVisible" />
  </div>
</template>

<script>
import { getDetailPage, delObj, batchDel, clearAll } from "@/api/platform/area/limit";
import { tableOption } from "@/const/crud/platform/area/limit_vechile";
import { mapGetters } from "vuex";
import AuthOperationProgress from "./AuthOperationProgress";

export default {
  name: "limit",
  components: {
    AuthOperationProgress
  },
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
        pageSize: 20, // 每页显示多少条
        authId: "",
        type: ""
      },
      deleteForm: {
        delIds: [],
        authId: "",
        type: ""
      },
      operationProgressVisible: false,
      batchDeleting: false,
      clearing: false,
      routeContextVersion: 0,
      listRequestSequence: 0
    };
  },
  created() {
    this.syncRouteContext(this.$route);
    this.loadRouteList();
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  watch: {
    $route(route) {
      this.syncRouteContext(route);
      this.loadRouteList();
    }
  },
  methods: {
    syncRouteContext(route) {
      this.routeContextVersion += 1;
      this.listRequestSequence += 1;
      this.page.authId = route.params.id;
      this.page.type = route.params.type;
      this.page.currentPage = 1;
      this.page.total = 0;
      this.deleteForm.authId = route.params.id;
      this.deleteForm.type = route.params.type;
      this.deleteForm.delIds = [];
      this.tableData = [];
    },
    loadRouteList() {
      return this.getList().catch(error => {
        this.$notify.error({
          title: "列表加载失败",
          message: this.errorMessage(error, "权限明细加载失败"),
          type: "error",
          duration: 3000
        });
      });
    },
    async getList(page = this.page, params = this.searchForm) {
      const requestSequence = ++this.listRequestSequence;
      const routeContextVersion = this.routeContextVersion;
      this.tableLoading = true;
      try {
        const response = await getDetailPage(
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
        );
        if (requestSequence !== this.listRequestSequence || routeContextVersion !== this.routeContextVersion) return;
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
      } catch (error) {
        if (requestSequence !== this.listRequestSequence || routeContextVersion !== this.routeContextVersion) return;
        throw error;
      } finally {
        if (requestSequence === this.listRequestSequence) this.tableLoading = false;
      }
    },
    operationContext() {
      return {
        version: this.routeContextVersion,
        authId: this.deleteForm.authId,
        type: this.deleteForm.type,
        name: (this.$route.query && this.$route.query.name) || ""
      };
    },
    isOperationContextCurrent(context) {
      return context.version === this.routeContextVersion &&
        context.authId === this.deleteForm.authId &&
        context.type === this.deleteForm.type;
    },
    async refreshAfterAccepted(context) {
      if (!this.isOperationContextCurrent(context)) return;
      try {
        await this.getList();
      } catch (error) {
        if (this.isOperationContextCurrent(context)) this.notifyAcceptedRefreshFailed(error, context);
      }
    },
    operationLabel(context) {
      const name = context.name ? `“${context.name}”` : "未命名权限组";
      return `${name}（ID：${context.authId}）`;
    },
    notifyAcceptedRefreshFailed(error, context) {
      this.$notify({
        title: "请求已受理，列表刷新失败",
        message: `权限组${this.operationLabel(context)}的设备结果仍待确认，请手动刷新列表后再操作。${this.errorMessage(error, "")}`,
        type: "warning",
        duration: 5000
      });
    },
    notifyRouteChanged(context) {
      this.$notify({
        title: "权限组已切换",
        message: `权限组${this.operationLabel(context)}的本次操作已取消，请在当前权限组重新选择后提交。`,
        type: "warning",
        duration: 3000
      });
    },
    async handleDelBatch() {
      const elm = this.$createElement;
      if (this.batchDeleting) return;
      if (this.deleteForm.delIds.length == 0) {
        this.$notify.error({
          title: "提示信息",
          message: "请选择要删除的车辆",
          type: "error",
          duration: 2000
        });
        return;
      }
      const context = this.operationContext();
      const request = {
        authId: context.authId,
        type: context.type,
        delIds: [...this.deleteForm.delIds]
      };
      this.batchDeleting = true;
      try {
        await this.$msgbox({
          message: elm("p", { attrs: { class: "smallp" } }, [
            elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
            elm("span", null, "确认删除所选车辆信息？ ")
          ]),
          showCancelButton: true,
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          customClass: "small_dialog",
          center: true
        });
        if (!this.isOperationContextCurrent(context)) {
          this.notifyRouteChanged(context);
          return;
        }
        const dataResponse = await batchDel(request);
        if (dataResponse.data.data) {
          this.notifyDeleteAccepted(context);
          if (this.isOperationContextCurrent(context)) {
            this.deleteForm.delIds = [];
            await this.refreshAfterAccepted(context);
          }
        } else {
          this.notifyDeleteFailed("删除请求提交失败", context);
        }
      } catch (error) {
        if (!this.isConfirmCanceled(error)) {
          this.notifyDeleteFailed(this.errorMessage(error, "删除请求提交失败"), context);
        }
      } finally {
        this.batchDeleting = false;
      }
    },
    async handleClear() {
      if (this.clearing) return;
      const elm = this.$createElement;
      const context = this.operationContext();
      this.clearing = true;
      try {
        await this.$msgbox({
          message: elm("p", { attrs: { class: "smallp" } }, [
            elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
            elm("span", null, "确认清空所有车辆权限信息？ ")
          ]),
          showCancelButton: true,
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          customClass: "small_dialog",
          center: true
        });
        if (!this.isOperationContextCurrent(context)) {
          this.notifyRouteChanged(context);
          return;
        }
        const dataResponse = await clearAll(context.authId);
        if (dataResponse.data.data) {
          this.notifyDeleteAccepted(context);
          if (this.isOperationContextCurrent(context)) await this.refreshAfterAccepted(context);
        } else {
          this.notifyDeleteFailed("删除请求提交失败", context);
        }
      } catch (error) {
        if (!this.isConfirmCanceled(error)) {
          this.notifyDeleteFailed(this.errorMessage(error, "删除请求提交失败"), context);
        }
      } finally {
        this.clearing = false;
      }
    },
    notifyDeleteAccepted(context) {
      this.$notify({
        title: "删除请求已提交",
        message: `权限组${this.operationLabel(context)}的请求已受理，设备结果仍待确认。旧链路暂不能自动定位批次，请手动查看可访问园区的权限任务。`,
        type: "info",
        duration: 5000
      });
    },
    notifyDeleteFailed(message, context) {
      this.$notify.error({
        title: "删除失败",
        message: `权限组${this.operationLabel(context)}：${message}`,
        type: "error",
        duration: 3000
      });
    },
    isConfirmCanceled(error) {
      return error === "cancel" || error === "close";
    },
    errorMessage(error, fallback) {
      return (error && error.response && error.response.data && error.response.data.msg) || (error && error.message) || fallback;
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
