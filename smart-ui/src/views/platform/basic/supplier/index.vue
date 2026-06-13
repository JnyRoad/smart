<!--基础信息：供应商管理 -->
<template>
  <div class="my-basic-container staff">
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
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加服务单位</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="所在园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="公司名称" prop="name">
            <el-input v-model="searchForm.name" placeholder="公司名称" clearable></el-input>
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
        >
          <template slot-scope="scope" slot="status">
            <span>{{scope.row.status}}</span>
          </template>

          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleEdit(scope.row,scope.$index)"
            >编辑</el-button>
            <el-button
              type="text"
              icon="el-icon-view"
              @click="deleteClick(scope.row,scope.$index)"
            >删除</el-button>
          </template>
        </avue-crud>
        <el-dialog
          title="添加园区服务单位"
          class="dialog_form"
          @close="resetAddFrom('addForm')"
          width="500px"
          :visible.sync="addFormVisible"
        >
          <el-form :rules="rules" ref="addForm" :model="addform" label-width="120px">
            <el-form-item label="公司名称" prop="name">
              <el-input v-model="addform.name" clearable></el-input>
            </el-form-item>

            <el-form-item label="所在园区" prop="parkId">
              <parkSelect v-model="addform.parkId" @getItem="getAddPark"></parkSelect>
            </el-form-item>
            <el-form-item label="法人" prop="legalPerson">
              <el-input
                type="textarea"
                :rows="3"
                v-model="addform.legalPerson"
                placeholder
                clearable
              ></el-input>
            </el-form-item>
            <el-form-item label="税务识别号">
              <el-input v-model="addform.numbers" placeholder clearable></el-input>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="addform.remark" clearable></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addCancel('addForm')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading ">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog
          title="编辑园区服务单位"
          class="dialog_form"
          width="500px"
          @close="resetEditFrom('editform')"
          :visible.sync="editFormVisible"
        >
          <el-form :rules="rules" ref="editform" :model="editform" label-width="120px">
            <el-form-item label="公司名称" prop="name">
              <el-input v-model="editform.name" clearable></el-input>
            </el-form-item>
            <el-form-item label="所在园区" prop="parkId">
              <parkSelect v-model="editform.parkId" @getItem="getEditPark"></parkSelect>
            </el-form-item>
            <el-form-item label="法人" prop="legalPerson">
              <el-input
                type="textarea"
                :rows="3"
                v-model="editform.legalPerson"
                placeholder
                clearable
              ></el-input>
            </el-form-item>
            <el-form-item label="税务识别号">
              <el-input v-model="editform.numbers" placeholder clearable></el-input>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="editform.remark" clearable></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="editCancel('editform')" plain>取 消</el-button>
            <el-button type="primary" @click="editSubmit('editform')" :loading="editLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, remove, edit, add } from "@/api/platform/basic/supplier";
import { tableOption } from "@/const/crud/platform/basic/supplier";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
export default {
  name: "attendance",
  data() {
    var validateName = (rule, value, callback) => {
      var rename = /^[\u4e00-\u9fa5_a-zA-Z0-9]{1,30}$/;
      if (!rename.test(value)) {
        callback(
          new Error("公司名称只允许汉字、字母与数字的组合,最长为30个字符")
        );
      } else {
        callback();
      }
    };
    var validateLegalPerson = (rule, value, callback) => {
      var latreg = /^[\u4e00-\u9fa5_a-zA-Z0-9]{1,30}$/;
      if (!latreg.test(value)) {
        callback(new Error("法人只允许汉字、字母与数字的组合,最长为30个字符"));
      } else {
        callback();
      }
    };

    return {
      addLoading: false,
      editLoading: false,
      addFormVisible: false, //添加园区供应商，显示切换
      editFormVisible: false, //编辑园区供应商，显示切换
      searchForm: {
        //搜索菜单表单
        staffName: undefined,
        staffBadge: undefined,
        timeRange: undefined
      },

      addform: {
        //添加园区供应商，表单
        name: "",
        legalPerson: "",
        remark: "",
        parkId: "",
        numbers: "",
        parkName: ""
      },

      rules: {
        name: [
          { required: true, message: "请输入公司名称", trigger: "blur" },
          { validator: validateName, trigger: "blur" }
        ],

        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        legalPerson: [
          { required: true, message: "请输入法人", trigger: "blur" },
          { validator: validateLegalPerson, trigger: "blur" }
        ]
      },
      editform: {
        //修改园区供应商，表单
        name: "",
        legalPerson: "",
        remark: "",
        parkId: "",
        numbers: "",
        parkName: ""
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
  watch: {},
  methods: {
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
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });

      this.tableLoading = false;
    },
    resetAddFrom(formName) {
      this.$refs[formName].resetFields();
    },
    resetEditFrom(formName) {
      this.$refs[formName].clearValidate();
    },
    addCancel(formName) {
      this.addFormVisible = false;
      this.resetAddFrom(formName);
    },
    editCancel(formName) {
      this.editFormVisible = false;
      this.resetEditFrom(formName);
    },
    getAddPark(item) {
      this.addform.parkName = item.label;
    },
    getEditPark(item) {
      this.editform.parkName = item.label;
    },
    deleteClick(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该供应商信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return remove(row.id);
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
        .catch(error => {
          this.$notify({
            title: "删除供应商失败",
            message: error.message,
            type: "error",
            duration: 2000
          });
        });
    },
    handleEdit(row, index) {
      //点击编辑
      this.editFormVisible = true;
      this.editform = Object.assign({}, row);
    },

    addSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.addLoading = true;
          add(this.addform)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.getList(this.page);
                this.addFormVisible = false;
                this.$notify({
                  title: "成功",
                  message: msg,
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

    editSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editLoading = true;
          edit(this.editform)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.getList(this.page);
                this.editFormVisible = false;
                this.$notify({
                  title: "成功",
                  message: msg,
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
              this.editLoading = false;
            })
            .catch(err => {
              this.editLoading = false;
            });
        } else {
          return false;
        }
      });
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
      const src = `/platform/work/attendance/detail/${row.recordId}`;
      this.$router.push({
        path: src
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
