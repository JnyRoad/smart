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
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加车辆</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="handleDelBatch()">批量删除</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @row-del="rowDel"
          @size-change="sizeChange"
          @current-change="currentChange"
          @selection-change="selectChange"
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
          title="添加车辆"
          class="dialog_form"
          @close="resetEditFrom('addform')"
          width="500px"
          :visible.sync="addFormVisible"
        >
          <el-form :rules="addRules" ref="addform" :model="addform" label-width="100px">
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="addform.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="车牌号" prop="vehiclePlate">
              <el-input v-model="addform.vehiclePlate " clearable></el-input>
            </el-form-item>
            <el-form-item label="原因" prop="remark">
              <el-input v-model="addform.remark " clearable></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addCancel('addform')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addform')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog
          title="编辑车辆"
          class="dialog_form"
          @close="resetEditFrom('editform')"
          width="500px"
          :visible.sync="editFormVisible"
        >
          <el-form :rules="editRules" ref="editform" :model="editform" label-width="100px">
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="editform.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="车牌号" prop="vehiclePlate">
              <el-input v-model="editform.vehiclePlate" clearable></el-input>
            </el-form-item>
            <el-form-item label="原因" prop="remark">
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
import {
  fetchList,
  saveObj,
  delObj,
  putObj,
  getObj,
  plate,
  batch
} from "@/api/platform/vehicle/vehicle_black";
import { tableOption } from "@/const/crud/platform/vehicle/vehicle_black";
import { mapGetters } from "vuex";

export default {
  name: "parking_lot",
  data() {
    var addVehiclePlateValidator = (rule, value, callback) => {
      var temp = value.replace(/\s*/g, "");
      this.addform.vehiclePlate = temp.toLocaleUpperCase();
      var parten = /^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[A-Z])|([A-Z]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$/;
      if (!parten.test(this.addform.vehiclePlate)) {
        callback(new Error("车牌号输入错误"));
      } else {
        plate(this.addform).then(response => {
          if (response.data.data) {
            callback(new Error("车牌号已绑定，请勿重复绑定"));
          } else {
            callback();
          }
        });
      }
    };

    var editVehiclePlateValidator = (rule, value, callback) => {
      var temp = value.replace(/\s*/g, "");
      this.editform.vehiclePlate = temp.toLocaleUpperCase();
      var parten = /^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[A-Z])|([A-Z]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$/;
      if (!parten.test(this.editform.vehiclePlate)) {
        callback(new Error("车牌号输入错误"));
      } else {
        plate(this.editform).then(response => {
          if (response.data.data) {
            callback(new Error("车牌号已绑定，请勿重复绑定"));
          } else {
            callback();
          }
        });
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
        vehiclePlate: "",
        remark: ""
      },
      addRules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        vehiclePlate: [
          { required: true, message: "请输入车牌号", trigger: "blur" },
          { validator: addVehiclePlateValidator, trigger: "blur" }
        ]
      },
      editRules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        vehiclePlate: [
          { required: true, message: "请输入车牌号", trigger: "blur" },
          { validator: editVehiclePlateValidator, trigger: "blur" }
        ]
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      searchForm: {
        parkId: undefined
      },
      editform: {
        //编辑停车场，表单
        id: "",
        parkId: "",
        vehiclePlate: "",
        remark: ""
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      deleteForm: {
        ids: []
      }
    };
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
      getObj(row.id).then(res => {
        this.editform = res.data.data
        this.editform.parkId = Number(res.data.data.parkId)
      });

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

    selectChange(val) {
      //序号那边选择事件
      this.deleteForm.ids = [];
      if (val.length > 0) {
        val.forEach(function(element) {
          this.deleteForm.ids.push(element.id);
        }, this);
      }
    },

    handleDelBatch() {
      var _this = this;
      const elm = this.$createElement;
      if (this.deleteForm.ids.length == 0) {
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
      })
        .then(() => {
          return batch(this.deleteForm);
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
          elm("span", null, "确认删除该车辆信息？ ")
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
              message: "删除失败",
              type: "error",
              duration: 2000
            });
          }
        })
        .catch(err => { console.error(err) });
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList(this.page, this.searchForm)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.searchForm)
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
