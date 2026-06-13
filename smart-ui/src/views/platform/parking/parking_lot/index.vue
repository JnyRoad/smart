<!--区域管理，园区管理  -->
<template>
  <div class="my-basic-container parking_lot">
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
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加停车场</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @row-del="rowDel"
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
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>
          </template>
        </avue-crud>
        <el-dialog
          title="添加停车场"
          class="dialog_form"
          @close="resetEditFrom('addform')"
          width="500px"
          :visible.sync="addFormVisible"
        >
          <el-form :rules="rules" ref="addform" :model="addform" label-width="100px">
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="addform.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="停车场名称" prop="name">
              <el-input v-model="addform.name" clearable></el-input>
            </el-form-item>
            <el-form-item label="总车位" prop="totalCount">
              <el-input v-model="addform.totalCount" clearable></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addCancel('addform')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addform')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog
          title="编辑停车场"
          class="dialog_form"
          @close="resetEditFrom('editform')"
          width="500px"
          :visible.sync="editFormVisible"
        >
          <el-form :rules="rules" ref="editform" :model="editform" label-width="100px">
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="editform.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="停车场名称" prop="name">
              <el-input v-model="editform.name" clearable></el-input>
            </el-form-item>
            <el-form-item label="总车位" prop="totalCount">
              <el-input v-model="editform.totalCount" disabled></el-input>
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
import {
  fetchList,
  saveObj,
  delObj,
  putObj,
  getObj
} from "@/api/platform/parking/parking_lot";
import { tableOption } from "@/const/crud/platform/parking/parking_lot";
import { mapGetters } from "vuex";

export default {
  name: "parking_lot",
  data() {
    var validateUseCount = (rule, value, callback) => {
      var useNum = /^(0|[1-9][0-9]*)$/;
      if (!useNum.test(value)) {
        callback(new Error("请输入大于或等于0的正整数"));
      } else {
        callback();
      }
    };
    return {
      addLoading: false,
      editLoading: false,
      addFormVisible: false, //添加停车场，显示切换
      editFormVisible: false, //编辑停车场，显示切换
      addform: {
        //添加停车场，表单
        parkId: "",
        name: "",
        totalCount: 0
      },
      rules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        name: [
          { required: true, message: "请输入停车场名称", trigger: "blur" }
        ],
        totalCount: [
          { required: true, message: "请输入总车位", trigger: "blur" },
          { validator: validateUseCount, trigger: "blur" }
        ]
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      searchForm: {
        parkId: ""
      },
      editform: {
        //编辑停车场，表单
        parkId: "",
        name: "",
        totalCount: 0
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption
    };
  },
  watch: {
    addFormVisible(newVal, oldVal) {
      newVal && this.doClearValidate("addform");
    }
  },
  created() {
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
            descs: "create_time",
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
    handleEdit(row, index) {
      //点击编辑
      this.editFormVisible = true;
      this.editform = row;
    },
    editSubmit(formName) {
      //编辑内容确定
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editLoading = true;
          putObj(this.editform).then(dataResponse => {
            this.editFormVisible = false;
            this.editLoading = false;
            this.getList(this.page);
          });
        } else {
          return false;
        }
      });
    },
    addSubmit(formName) {
      //添加内容确定
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.addLoading = true;
          saveObj(this.addform).then(dataResponse => {
            this.addFormVisible = false;
            this.addLoading = false;
            this.getList(this.page);
          });
        } else {
          return false;
        }
      });
    },
    editCancel(formName) {
      this.editFormVisible = false;
      this.resetEditFrom(formName);
    },
    addCancel(formName) {
      this.addFormVisible = false;
      this.resetAddFrom(formName);
    },
    resetAddFrom(formName) {
      this.$refs[formName].resetFields();
      this.$refs[formName].clearValidate();
    },
    resetEditFrom(formName) {
      this.$refs[formName].clearValidate();
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
          elm("span", null, "确认删除该停车场信息？ ")
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
              message: "不可删除已绑定设备的停车场",
              type: "error",
              duration: 2000
            });
          }
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
</style>
