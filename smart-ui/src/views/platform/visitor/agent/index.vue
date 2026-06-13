<!--代理审批人 -->
<template>
  <div class="my-basic-container agent">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu clear">
          <span style="color:red">信息提示：经理以上才能设置代理人，设置代理人，访客审批会同时发送给代理审批人，审批后通过</span>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加代理人</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="被访人工号" prop="interVieweeBadge">
            <el-input v-model="searchForm.interVieweeBadge" placeholder="被访人工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="被访人姓名" prop="interVieweeName">
            <el-input v-model="searchForm.interVieweeName" placeholder="被访人姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="代理审批人工号" prop="proxyBadge">
            <el-input v-model="searchForm.proxyBadge" placeholder="代理审批人工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="代理审批人姓名" prop="proxyName">
            <el-input v-model="searchForm.proxyName" placeholder="代理审批人姓名" clearable></el-input>
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
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>
          </template>
        </avue-crud>
        <el-dialog
          title="添加代理人"
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
            label-width="120px"
          >
            <el-form-item label="被访人工号" prop="interVieweeBadge">
              <el-input v-model="addForm.interVieweeBadge" placeholder="请输入">
                <el-button
                  type="info"
                  slot="append"
                  :loading="loading1"
                  @click="getStaffDetail('addForm', 1)"
                  class="noPading"
                  icon="el-icon-search"
                ></el-button>
              </el-input>
            </el-form-item>
            <el-form-item label="被访人姓名" prop="interVieweeName">
              <el-input v-model="addForm.interVieweeName"  disabled></el-input>
            </el-form-item>
            <el-form-item label="代理审批人工号" prop="proxyBadge">
              <el-input v-model="addForm.proxyBadge" placeholder="请输入">
                <el-button
                  type="info"
                  slot="append"
                  :loading="loading2"
                  @click="getStaffDetail('addForm', 2)"
                  class="noPading"
                  icon="el-icon-search"
                ></el-button>
              </el-input>
            </el-form-item>
            <el-form-item label="代理审批人姓名" prop="proxyName">
              <el-input v-model="addForm.proxyName"  disabled></el-input>
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
import { fetchList, addObj, delObj, getStaffDetail } from "./_service";
import { tableOption } from "@/const/crud/platform/visitor/agent";
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
      loading1: false,
      loading2: false,
      addForm: {
        interVieweeName: '',
        interVieweeBadge: '',
        proxyName: '',
        proxyBadge: ''
      },
      addRules: {
        interVieweeName: [{ required: true, message: "请设置被访人姓名", trigger: "blur" }],
        interVieweeBadge: [{ required: true, message: "请输入被访人工号", trigger: "blur" }],
        proxyName: [{ required: true, message: "请设置代理审批人姓名", trigger: "blur" }],
        proxyBadge: [{ required: true, message: "请输入代理审批人工号", trigger: "blur" }]
      },
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
  },
  methods: {
    handleDel(row, index) {
      //删除
      this.$refs.crud.rowDel(row, index);
    },
    getStaffDetail(formName, type){
      if(type==1){
        if(this.validatenull(this.addForm.interVieweeBadge)){
          this.$message.error('请输入被访人工号！');
          return
        }
        try {
          this.loading1 = true
          getStaffDetail(this.addForm.interVieweeBadge).then(response => {
            let result = response.data.data;
            if (!this.validatenull(result)) {
              this.addForm.interVieweeName = result.name
              this.addForm.parkId = result.parks[0].id
            }else{
              this.addForm.interVieweeName = ''
            }
          });
        } catch (error) {
          // 忽略异常：失败时仅复位 loading 状态
        } finally {
          this.loading1 = false
        }

      }else if(type==2){
        if(this.validatenull(this.addForm.proxyBadge)){
          this.$message.error('请输入代理审批人工号！');
          return
        }
        try {
          this.loading2 = true
          getStaffDetail(this.addForm.proxyBadge).then(response => {
            let result = response.data.data;
            if (!this.validatenull(result)) {
              this.addForm.proxyName = result.name
            }else{
              this.addForm.proxyName = ''
            }
          });
        } catch (error) {
          // 忽略异常：失败时仅复位 loading 状态
        } finally {
          this.loading2 = false
        }
      }
    },
    rowDel: function(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该代理审批人信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj([row.id]);
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
    addSubmit(formName) {
      //添加岗位，确认
      var _this = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          let obj = {
            interVieweeBadge: this.addForm.interVieweeBadge,
            proxyBadge: this.addForm.proxyBadge,
            parkId: this.addForm.parkId
          }
          this.addLoading = true;
          addObj(obj)
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
.agent ::v-deep {
  .el-form-item__label {
      width: 120px;
  }
  .el-form--label-left .el-form-item__label{
    text-align: right;
  }
}
.tips {
  max-width: 500px;
  border: 1px solid red;
}
</style>
