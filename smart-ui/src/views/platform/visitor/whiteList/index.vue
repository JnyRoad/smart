<!--访客预约，审批白名单 -->
<template>
  <div class="my-basic-container staff">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu clear">
          <span style="color:red">信息提示：设置在列表里的人员，将不进行二级审批</span>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="addPersonVisible = true">添加白名单</el-button>
            <el-button type="primary" icon @click="batchDel">批量删除</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="工号" prop="staffBadge">
            <el-input v-model="searchForm.staffBadge" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="staffName">
            <el-input v-model="searchForm.staffName" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="所属园区/BU/部门" prop="depIds">
            <el-cascader
              expand-trigger="hover"
              :options="compOptions"
              :show-all-levels="false"
              :change-on-select="true"
              v-model="depIds"
              clearable
            ></el-cascader>
          </el-form-item>
          <el-form-item label="岗位" prop="jobName">
            <el-input v-model="searchForm.jobName" placeholder="岗位" clearable></el-input>
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
          title="添加白名单"
          @close="resetJob('addPersonForm')"
          class="dialog_form"
          width="500px"
          :visible.sync="addPersonVisible"
        >
          <el-form
            ref="addPersonForm"
            :model="addPersonForm"
            :rules="addPersonRules"
            label-position="left"
            label-width="50px"
          >
            <el-form-item label="园区" prop="parkId">
              <parkSelect v-model="addPersonForm.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="工号" prop="staffBadge">
              <el-input v-model="addPersonForm.staffBadge" placeholder="请输入">
                <el-button
                  type="info"
                  slot="append"
                  :loading="loading1"
                  @click="getStaffDetail('addPersonForm')"
                  class="noPading"
                  icon="el-icon-search"
                ></el-button>
              </el-input>
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="personInfo.name" placeholder="" disabled></el-input>
            </el-form-item>
            <el-form-item label="BU" prop="compName">
              <el-input v-model="personInfo.compName" placeholder="" disabled></el-input>
            </el-form-item>
            <el-form-item label="部门" prop="depName">
              <el-input v-model="personInfo.depName" placeholder="" disabled></el-input>
            </el-form-item>
            <el-form-item label="岗位" prop="jobName">
              <el-input v-model="personInfo.jobName" placeholder="" disabled></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addPersonVisible = false" plain>取 消</el-button>
            <el-button type="primary" @click="addPersonSubmit('addPersonForm')" :loading="addPersonLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { fetchList, addObj, delObj, getStaffDetail } from "./_service";
import { getCompTree } from "@/api/platform/_publicService";
import { tableOption } from "@/const/crud/platform/visitor/whiteList";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
export default {
  name: "approveAuthority",
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        jobName: null
      },
      compOptions: [],
      depIds: [],
      addPersonLoading: false,
      addPersonVisible: false,
      loading1: false,
      addPersonForm:{
        parkId: '',
        staffBadge: ''
      },
      personInfo: {},
      addPersonRules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        staffBadge: [{ required: true, message: "请输入工号", trigger: "blur" }],
      },
      selectPersons: [], //当前选中员工的集合
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
    getCompTree().then(response => {
      this.compOptions = response.data.data;
    });
    this.getList(this.page, this.searchForm);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  watch: {
    selectPersons: function(val) {
      val.length > 0 ? (this.hasSelect = true) : (this.hasSelect = false);
    },
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
  methods: {
    getStaffDetail(formName){
      if(this.validatenull(this.addPersonForm.staffBadge)){
        this.$message.error('请输入工号！');
        return
      }
      try {
        this.loading1 = true
        getStaffDetail(this.addPersonForm.staffBadge).then(response => {
          let result = response.data.data;
          if (!this.validatenull(result)) {
            this.personInfo = result
          }else{
            this.personInfo = {}
          }
        });
      } catch (error) {
        // 忽略异常：失败时仅复位 loading 状态
      } finally {
        this.loading1 = false
      }
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
          elm("span", null, "确认删除该信息？ ")
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
    batchDel() {
      //批量删除
      if (this.selectPersons.length == 0) {
        this.$message({
          message: "请先选中想要删除的白名单信息！",
          type: "warning"
        });
        return;
      }
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除所有选中的白名单信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj(_this.selectPersons);
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
    selectChange(val) {
      //序号那边选择事件
      var idArr = [];
      if (val.length > 0) {
        val.forEach(function(element) {
          idArr.push(element.id);
        }, this);
      }
      this.selectPersons = idArr;
    },
    addPersonSubmit(formName) {
      //添加白名单，确认
      var _this = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          if(!(this.personInfo&&this.personInfo.name)){
            this.$message.error('请先查询员工信息');
            return
          }

          let obj = this.personInfo.parks.find(item => {
            if (item.id === this.addPersonForm.parkId) {
              return item
            }
          })

          if(!obj){
            this.$message.error('该员工无当前选择园区的权限');
            return
          }

          this.addPersonLoading = true;
          addObj(this.addPersonForm)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.addPersonVisible = false;
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
              this.addPersonLoading = false;
            })
            .catch(err => {
              this.addPersonLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    resetJob(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
        this.personInfo = {}
      }
    },
    searchSubmit(form) {
      this.page.currentPage = 1;
      this.getList(this.page, form);
    },
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
        this.depIds = [];
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
    width: 130px;
  }
}
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
