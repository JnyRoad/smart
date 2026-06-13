<!--水电模板  -->
<template>
  <div class="my-basic-container room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchform)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchform')"
              plain
            >清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">新增模板</el-button>
          </div>
        </div>
        <el-form ref="searchform" :inline="true" :model="searchform" size="mini" class="topForm">
          <el-form-item label="园区名称" prop="parkId">
            <parkSelect v-model="searchform.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="级层" prop="jchenid">
            <el-select v-model="searchform.jchenid" clearable placeholder="请选择">
              <el-option
                v-for="item in jobJchenList"
                :key="item.jchenID"
                :label="item.jchenName"
                :value="item.jchenID"
              ></el-option>
               </el-select>
          </el-form-item>
          <el-form-item label="分摊模板" prop="badge">
              <el-input v-model="searchform.templateName" placeholder="分摊模板" clearable></el-input>
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
              icon="el-icon-edit"
              @click="handleConfig(scope.row,scope.$index)">配置分摊模板</el-button>
                  <el-button
              type="text"
              icon="el-icon-edit"
              @click="handleEdit(scope.row,scope.$index)">编辑</el-button>
                <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDel(scope.row,scope.$index)">删除</el-button>
          </template>
        </avue-crud>
        <!-- 添加模板弹出框 -->
        <el-dialog
          title="添加水电分摊模板"
          class="dialog_form"
          width="550px"
          @close="resetAddForm('addForm')"
          :visible.sync="addFormVisible"
        >
          <el-form :rules="addRules" ref="addForm" :model="addForm" label-width="80px">
            <el-form-item label="园区名称" prop="parkId">
              <parkSelect v-model="addForm.parkId" @doChange="getAddDormitory"></parkSelect>
            </el-form-item>
            <el-form-item label="模板名称" prop="templateName">
              <el-input v-model="addForm.templateName" clearable></el-input>
            </el-form-item>
            <el-form-item label="员工层级" prop="jchenid">
              <el-select v-model="addForm.jchenid" clearable placeholder="请选择" @change="selectJchenName">
              <el-option
                v-for="item in jobJchenList"
                :key="item.jchenID"
                :label="item.jchenName"
                :value="item.jchenID"
              ></el-option>
             </el-select>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="resetAddForm('addForm')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading">保 存</el-button>
          </div>
        </el-dialog>
         <!-- 编辑模板弹出框 -->
        <el-dialog
          title="编辑水电分摊模板"
          class="dialog_form"
          width="550px"
          @close="resetEditForm('editForm')"
          :visible.sync="editFormVisible"
        >
          <el-form :rules="editRules" ref="editForm" :model="editForm" label-width="80px">
            <el-form-item label="所属园区" prop="parkId">
              <el-input v-model="editForm.parkName" :disabled="true"></el-input>
            </el-form-item>
             <el-form-item label="级层名称" prop="jchenname">
              <el-input v-model="editForm.jchenname" :disabled="true"></el-input>
            </el-form-item>
            <el-form-item label="分摊模板" prop="templateName">
              <el-input v-model="editForm.templateName"></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="resetEditForm('editForm')" plain>取 消</el-button>
            <el-button type="primary" @click="editSubmit('editForm')" :loading="editLoading">保 存</el-button>
          </div>
        </el-dialog>
        <!-- 配置模板弹出框 -->
        <setTemp :visible="configVisible" :row="currentObj" @configdo="configDo"></setTemp>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchList,
  queryDormitory,
  addTemplate,
  delTemplate,
  updateDormitorySDTemplate,
  queryJobJchenList
} from "@/api/platform/dormitory/sd_templates";
import { tableOption } from "@/const/crud/platform/dormitory/sd_templates";
import { validatenum } from "@/util/validate";
import { mapGetters } from "vuex";
import setTemp from "./setTemp";
export default {
  name: "dorm_mng",
  components:{
    setTemp
  },
  data() {
    var validateIsNum = (rule, value, callback) => {
      let regName = /^-?\d+$/;
      if (value === 0) {
        callback(new Error("请输入非0整数"));
      }
      if (!regName.test(value)) {
        callback(new Error("请输入整数"));
      } else {
        callback();
      }
    };
    var validateFloorNum = (rule, value, callback) => {
      let regName = /^\d+$/;
      if (value == 0) {
        callback(new Error("请输入大于0的正整数"));
      }

      if (!regName.test(value)) {
        callback(new Error("请输入正整数"));
      } else if (value > 14) {
        callback(new Error("楼层数量最大值为14"));
      } else {
        callback();
      }
    };

    var validateRoomNum = (rule, value, callback) => {
      let regName = /^\d+$/;
      // if(value==0)
      // {
      //    callback(new Error('请输入大于0的正整数'));
      // }

      if (!regName.test(value)) {
        callback(new Error("不能输入小数和负数"));
      } else if (value > 24) {
        callback(new Error("房间数量最大值为24"));
      } else {
        callback();
      }
    };
    return {
      addLoading: false, //是否正在添加
      editLoading: false, //是否正在编辑
      addFormVisible: false, //添加楼层
      editFormVisible: false, //编辑楼层
      configVisible: false,
      currentObj: {},
      addForm: {
        parkId: undefined, //所属园区
        templateName: undefined, //模板名称
        jchenid: undefined,  //级层ID
        jchenname: undefined  //级层名称
      },
      editForm: {
        parkId: undefined, //所属园区
        templateName: undefined //模板名称
      },
      addRules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        templateName: [
          { required: true, message: "请输入模板名称", trigger: "blur" }
        ],
        jchenid: [{ required: true, message: "请选择级层", trigger: "change" }]
      },
      editRules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        dormitoryId: [
          { required: true, message: "请选择楼栋", trigger: "change" }
        ],
        floorName: [
          { required: true, message: "请输入楼层编号", trigger: "blur" }
        ],
        roomNum: [
          { required: true, message: "请输入房间数量", trigger: "blur" },
          { validator: validateRoomNum, trigger: "blur" }
        ]
      },
      searchform: {
        parkId: undefined,
        templateName: undefined,
        jchenid: undefined
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      dormitoryData: [],
      addDormitoryData: [],
      jobJchenList: [],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    };
  },
  created() {
    this.getList(this.page);
    this.getJobJchenList();
  },
  mounted: function() {},
  computed: {},
  methods: {
    getList(page, params) {
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            asc: "id",
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

    getJobJchenList() {
      queryJobJchenList().then(response => {
        if(response.data.data) {
            this.jobJchenList = response.data.data
        }
      })
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page, this.searchform);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page, this.searchform);
    },
    getAddDormitory(parkId) {
      if (parkId === null || parkId === "") return;
      this.$refs["addForm"].resetFields();
      this.addForm.parkId = parkId;
      this.hasStartNum = false;
      queryDormitory({ parkId: parkId }).then(response => {
        this.addDormitoryData = response.data.data;
      });
    },

    selectJchenName(val) {
      let obj = {};
      obj = this.jobJchenList.find((item)=>{
        return item.jchenID === val;
      });
      let jchenName = obj.jchenName;
      this.addForm.jchenname = jchenName;
    },

    addSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.addLoading = true;
          addTemplate(this.addForm)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.addFormVisible = false;
                this.addLoading = false;
                this.getList(this.page, this.searchform);
                this.$notify({
                  title: "成功",
                  message: msg,
                  type: "success",
                  duration: 2000
                });
              } else if (dataResult === false) {
                this.addLoading = false;
                this.$notify({
                  title: "失败",
                  message: msg,
                  type: "error",
                  duration: 2000
                });
              }
            })
            .catch(() => {
              this.addLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    resetAddForm(formName) {
      this.addFormVisible = false;
      this.addLoading = false;
      this.hasStartNum = false; //将起始编号状态恢复默认设置
      this.$refs[formName].resetFields();
    },
    resetEditForm(formName) {
      this.editFormVisible = false;
      this.editLoading = false;
      this.$refs[formName].clearValidate();
    },
    handleDel(row, index) {
      this.$refs.crud.rowDel(row, index);
    },
    rowDel(row, index) {
      //删除
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该模板信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delTemplate(row.id);
        })
        .then(data => {
          if (data.data.data == false) {
            _this.$notify({
              title: "失败",
              message: data.data.msg,
              type: "error"
            });
          } else {
            this.getList(this.page, this.searchform);
            _this.$notify({
              title: "成功",
              message: "删除成功",
              type: "success"
            });
          }
        })
        .catch(error => { console.error(error) });
    },
    handleEdit(row, index, done, loading) {
      this.editFormVisible = true;
      this.editForm = Object.assign({}, row);
    },
    handleConfig(row, index, done, loading) {
      this.configVisible =  true
      this.currentObj = row
    },
    configDo(val){
      this.configVisible = val
    },
    editSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editLoading = true;
          updateDormitorySDTemplate(this.editForm)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true || dataResult === 1) {
                this.editFormVisible = false;
                this.editLoading = false;
                this.getList(this.page, this.searchform);
                this.$notify({
                  title: "成功",
                  message: msg,
                  type: "success",
                  duration: 2000
                });
              } else if (dataResult === false) {
                this.editLoading = false;
                this.$notify({
                  title: "失败",
                  message: msg,
                  type: "error",
                  duration: 2000
                });
              }
            })
            .catch(() => {
              this.editLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    searchSubmit(form) {
      //搜索
      this.page.currentPage = 1;
      this.getList(this.page, form);
    },
    resetFrom(formName) {
      //清空
      this.$refs[formName].resetFields();
      this.dormitoryData = [];
      this.page.currentPage = 1;
      this.getList(this.page);
    }
  }
};
</script>
