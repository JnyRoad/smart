<!--招聘管理，招聘岗位  -->
<template>
  <div class="my-basic-container recruit">
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
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">招聘岗位</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="岗位名称" prop="jobName">
            <el-input v-model="searchForm.jobName" placeholder="岗位名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="职层" prop="jcheId">
            <jcheSelect v-model="searchForm.jcheId"></jcheSelect>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="searchForm.status" placeholder="状态" clearable>
              <el-option label="招聘中" value="1"></el-option>
              <el-option label="招聘结束" value="0"></el-option>
              <el-option label="招聘暂停" value="2"></el-option>
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
            <span class="recruit-status-dot" :class="scope.row.status|recruitStatusClassFormat">
              <i></i>
              {{scope.row.status|recruitStatusFormat}}
            </span>
          </template>
          <template slot-scope="scope" slot="menu">
            <!-- 传1:刷新
                        传0：取消刷新
                        当前值为1时，要传0
            当前值为0时，要传1-->
            <el-button
              type="text"
              icon="el-icon-link"
              @click="refresh(scope.row,scope.$index)"
            >{{scope.row.isUp===1?'取消刷新':'刷新'}}</el-button>
            <el-button type="text" icon="el-icon-link" @click="handleUrl(scope.row,scope.$index)">链接</el-button>
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
        <el-dialog title="链接" class="dialog_form" width="480px" :visible.sync="linkVisible">
          <div class="linkdv">
            <p>
              {{link}}
              <el-button
                class="copybtn"
                type="primary"
                v-clipboard:copy="link"
                v-clipboard:success="onCopy"
                v-clipboard:error="onError"
              >复制</el-button>
            </p>
            <p class="tips">提示：链接可用于其他招聘渠道录取，提供给入职员工收集简历信息</p>
            <p class="tips" style="color: red">可以将当前岗位链接制作为二维码，方便该岗位员工扫码登记</p>
            <p class="tips" style="color: red">
              请复制当前链接并访问外网制作地址：
              <a
                href="https://cli.im"
                target="_blank"
                style="text-decoration: underline"
              >https://cli.im</a>
            </p>
          </div>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="linkVisible=false" plain>关闭</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchList,
  addObj,
  delObj,
  getById,
  putObj,
  refreshJob
} from "@/api/platform/recruit/recruitment";
import { tableOption } from "@/const/crud/platform/recruit/recruitment";
import { mapGetters } from "vuex";

export default {
  name: "recruit",
  data() {
    return {
      linkVisible: false,
      searchForm: {
        //搜索菜单表单
        parkId: undefined,
        jobName: undefined,
        // compId: undefined,
        // depId: undefined,
        jcheId: undefined,
        status: undefined
      },
      link: "",
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
      this.getList(this.page, this.searchForm);
    }
  },
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
      const src = `/platform/recruit/recruitment/add`;
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
      var msg = "";
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该招聘岗位信息？ ")
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
        .then(response => {
          msg = response.data.msg;
          var dataResult = response.data.data;
          if (dataResult === true) {
            _this.getList(this.page, this.searchForm);
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
        .catch(err => {
          _this.$notify({
            title: "删除失败",
            message: msg,
            type: "error",
            duration: 2000
          });
        });
    },
    handleDetail(row, index) {
      var id = row.id;
      const src = `/platform/recruit/recruitment/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
    },
    // 传1: 执行刷新
    // 传0：执行取消刷新
    // 当前值为1时，要传0
    // 当前值为0时，要传1
    refresh(row, index) {
      let isUP;
      let msgSuccess = "";
      let msgFailed = "";

      if (row.isUp === 1) {
        isUP = 0;
        msgSuccess = "已取消刷新!";
        msgFailed = "取消刷新失败!";
      } else {
        isUP = 1;
        msgSuccess = "刷新成功!";
        msgFailed = "刷新失败!";
      }
      refreshJob({ id: row.id, isUp: isUP })
        .then(response => {
          if (response.data.data) {
            this.$notify({
              title: msgSuccess,
              message: msgSuccess,
              type: "success",
              duration: 2000
            });
            this.getList(this.page, this.searchForm); //重新查询
          } else {
            this.$notify({
              title: msgFailed,
              message: msgFailed,
              type: "error",
              duration: 2000
            });
          }
        })
        .catch(err => { console.error(err) });
    },
    handleUrl(row, index) {
      this.linkVisible = true;
      var id = row.id;
      this.link = "https://tech.szyuto.com/#/gangwei/" + id;
    },
    onCopy(e) {
      this.$notify({
        title: "复制成功",
        message: "已复制到粘贴板,去粘贴吧！",
        type: "success",
        duration: 2000
      });
    },
    onError(e) {
      this.$notify({
        title: "复制失败",
        message: "复制失败，请尝试手动选择链接地址复制！",
        type: "success",
        duration: 2000
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
.linkdv {
  text-align: center;
  padding: 20px 0 30px 0;
}
.copybtn {
  padding: 2px;
}
.tips {
  font-size: 12px;
  color: #999;
  text-align: center;
  margin-top: 15px;
}
.dialog-footer {
  text-align: center;
}
</style>
