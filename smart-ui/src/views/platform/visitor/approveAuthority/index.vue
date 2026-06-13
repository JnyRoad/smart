<!--访客预约，访客审批权限 -->
<template>
  <div class="my-basic-container staff">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu clear">
          <span style="color:red">信息提示：设置在列表里的岗位，将不进行二级审批</span>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加岗位</el-button>
            <el-button type="primary" icon @click="batchDel">批量删除</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="岗位" prop="jobName">
            <el-input v-model="searchForm.jobName" placeholder="岗位" clearable></el-input>
          </el-form-item>
          <el-form-item label="园区" prop="parkId">
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
          @selection-change="selectChange"
          @row-del="rowDel"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>
          </template>
        </avue-crud>
        <el-dialog
          title="添加岗位"
          @close="resetJob('addForm')"
          class="dialog_form"
          width="500px"
          :visible.sync="addFormVisible"
        >
          <el-form
            ref="addForm"
            :model="addForm"
            :rules="addRules"
            label-position="left"
            label-width="80px"
          >
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="addForm.parkId" @doChange="parkChange"></parkSelect>
            </el-form-item>
            <el-form-item label="BU" prop="compId">
              <buSelect
                v-model="addForm.compId"
                :parkId="addForm.parkId"
                @getItem="getCompItem"
                @doChange="buChange"
              ></buSelect>
            </el-form-item>
            <el-form-item label="部门" prop="depId">
              <deptSelect
                v-model="addForm.depId"
                :compId="addForm.compId"
                @getItem="getDepItem"
                @doChange="deptChange"
              ></deptSelect>
            </el-form-item>
            <el-form-item label="岗位" prop="jobId">
              <jobSelect v-model="addForm.jobId" :depId="addForm.depId" @getItem="getJobItem"></jobSelect>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addFormVisible = false" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading">确 定</el-button>
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
  delObj
} from "@/api/platform/visitor/approveAuthority";
import { tableOption } from "@/const/crud/platform/visitor/approveAuthority";
import { mapGetters } from "vuex";
export default {
  name: "approveAuthority",
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        jobName: null
      },
      addLoading: false,
      addFormVisible: false,
      addForm: {
        compId: undefined,
        compName: undefined,
        depId: undefined,
        depName: undefined,
        jobId: undefined,
        jobName: undefined,
        parkId: undefined
      },
      addRules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        compId: [{ required: true, message: "请选择BU", trigger: "change" }],
        depId: [{ required: true, message: "请选择部门", trigger: "change" }],
        jobId: [{ required: true, message: "请选择岗位", trigger: "change" }]
      },
      selectJobs: [], //当前选中员工的集合
      hasSelect: false,
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
  created() {
    this.getList(this.page, this.searchForm);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  watch: {
    selectJobs: function(val) {
      val.length > 0 ? (this.hasSelect = true) : (this.hasSelect = false);
    }
  },
  methods: {
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
          elm("span", null, "确认删除该岗位信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj({ ids: [row.id] });
        })
        .then(dataResponse => {
          var msg = dataResponse.data.msg;
          var dataResult = dataResponse.data.data;
          if (dataResult == true) {
            _this.getList(_this.page, _this.searchForm);
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
        .catch(err => { console.error(err) });
    },
    batchDel() {
      //批量删除
      if (this.selectJobs.length < 0) {
        this.$message({
          message: "请先选中想要删除的岗位信息！",
          type: "warning"
        });
        return;
      }
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除所有选中的岗位信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj({ ids: _this.selectJobs });
        })
        .then(dataResponse => {
          var msg = dataResponse.data.msg;
          var dataResult = dataResponse.data.data;
          if (dataResult == true) {
            _this.getList(_this.page, _this.searchForm);
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
        .catch(err => { console.error(err) });
    },
    getList(page, params) {
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          this.searchForm
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
    getCompItem(obj) {
      this.addForm.compName = obj.label;
    },
    getDepItem(obj) {
      this.addForm.depName = obj.label;
    },
    getJobItem(obj) {
      this.addForm.jobName = obj.label;
    },
    parkChange() {
      this.addForm.compId = undefined;
      this.addForm.depId = undefined;
      this.addForm.jobId = undefined;
    },
    buChange() {
      this.addForm.depId = undefined;
      this.addForm.jobId = undefined;
    },
    deptChange() {
      this.addForm.jobId = undefined;
    },
    selectChange(val) {
      //序号那边选择事件
      var idArr = [];
      if (val.length > 0) {
        val.forEach(function(element) {
          idArr.push(element.id);
        }, this);
      }
      this.selectJobs = idArr;
    },
    addSubmit(formName) {
      //添加岗位，确认
      var _this = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.addLoading = true;
          addObj(this.addForm)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.addFormVisible = false;
                _this.getList(_this.page, _this.searchForm);
                this.$notify({
                  title: "成功",
                  message: "添加成功",
                  type: "success",
                  duration: 2000
                });
              } else if (dataResult === false) {
                this.$notify({
                  title: "失败",
                  message: msg,
                  type: "error",
                  duration: 2000
                });
              }
              this.addLoading = false;
            })
            .catch(err => {
              this.addLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    resetJob(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
      }
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
.upload-demo {
  display: inline-block;
  margin-right: 20px;
}
.tips {
  max-width: 500px;
  border: 1px solid red;
}
.num {
  padding: 0 5px;
  font-weight: bold;
}
.red {
  color: red;
}
.importBtn {
  margin-right: 20px;
}
.fileBtn {
  display: none;
}
.importCont {
  line-height: 25px;
  padding-bottom: 30px;
}
.fileTb {
  margin: 10px 0 0 0;
  width: 100%;
  .haveNo {
    color: red;
  }
  td {
    border: 1px solid #e0e0e0;
    text-align: center;
    padding: 0 10px;
    height: 35px;
  }
}
::v-deep .el-scrollbar__wrap {
  overflow-x: auto;
}
</style>
