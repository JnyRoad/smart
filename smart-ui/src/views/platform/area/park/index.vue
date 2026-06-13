<!--区域管理，园区管理  -->
<template>
  <div class="my-basic-container park">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">新增园区</el-button>
          </div>
        </div>
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
              @click="handleEdit(scope.row,scope.$index)"
            >编辑</el-button>
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>
            <el-button
              type="text"
              icon="el-icon-setting"
              @click="handleSet(scope.row,scope.$index)"
            >配置信息</el-button>
          </template>
        </avue-crud>
        <el-dialog
          title="添加园区"
          class="dialog_form"
          @close="resetAddFrom('addForm')"
          width="500px"
          :visible.sync="addFormVisible"
        >
          <el-form :rules="rules" ref="addForm" :model="addform" label-width="120px">
            <el-form-item label="园区名称" prop="parkName">
              <el-input v-model="addform.parkName" clearable></el-input>
            </el-form-item>
            <el-form-item label="园区地址">
              <el-input
                type="textarea"
                :rows="3"
                v-model="addform.parkAddress"
                placeholder
                clearable
              ></el-input>
            </el-form-item>
            <el-form-item label="转发服务url" prop="bridgeUrl">
              <el-input v-model="addform.bridgeUrl" clearable></el-input>
            </el-form-item>
            <el-form-item label="园区定位范围">
              <el-input v-model="addform.radius" placeholder clearable>
                <template slot="append">米</template>
              </el-input>
            </el-form-item>
            <el-form-item label="经度" prop="parkLongitude">
              <el-input v-model="addform.parkLongitude" clearable></el-input>
            </el-form-item>
            <el-form-item label="纬度" prop="parkLatitude">
              <el-input v-model="addform.parkLatitude" clearable></el-input>
            </el-form-item>
            <el-form-item label="咨询电话" prop="parkPhone">
              <el-input v-model="addform.parkPhone" clearable></el-input>
            </el-form-item>
            <el-form-item label="占地面积（亩）" prop="area">
              <el-input v-model="addform.area" clearable></el-input>
            </el-form-item>
            <el-form-item label="厂房个数" prop="workShopNum">
              <el-input v-model="addform.workShopNum" clearable></el-input>
            </el-form-item>
            <el-form-item label="食堂楼栋个数" prop="diningRoomNum">
              <el-input v-model="addform.diningRoomNum" clearable></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addCancel('addForm')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading ">确 定</el-button>
          </div>
        </el-dialog>

        <el-dialog
          title="编辑园区"
          class="dialog_form"
          width="500px"
          @close="resetEditFrom('editform')"
          :visible.sync="editFormVisible"
        >
          <el-form :rules="rules" ref="editform" :model="editform" label-width="120px">
            <el-form-item label="园区名称" prop="parkName">
              <el-input v-model="editform.parkName" clearable></el-input>
            </el-form-item>
            <el-form-item label="园区地址">
              <el-input
                type="textarea"
                :rows="3"
                v-model="editform.parkAddress"
                placeholder
                clearable
              ></el-input>
            </el-form-item>
            <el-form-item label="转发服务url" prop="bridgeUrl">
              <el-input v-model="editform.bridgeUrl" clearable></el-input>
            </el-form-item>
            <el-form-item label="园区定位范围">
              <el-input v-model="editform.radius" placeholder clearable>
                <template slot="append">米</template>
              </el-input>
            </el-form-item>
            <el-form-item label="经度" prop="parkLongitude">
              <el-input v-model="editform.parkLongitude" clearable></el-input>
            </el-form-item>
            <el-form-item label="纬度" prop="parkLatitude">
              <el-input v-model="editform.parkLatitude" clearable></el-input>
            </el-form-item>
            <el-form-item label="咨询电话" prop="parkPhone">
              <el-input v-model="editform.parkPhone" clearable></el-input>
            </el-form-item>
            <el-form-item label="占地面积（亩）" prop="area">
              <el-input v-model="editform.area" clearable></el-input>
            </el-form-item>
            <el-form-item label="厂房个数" prop="workShopNum">
              <el-input v-model="editform.workShopNum" clearable></el-input>
            </el-form-item>
            <el-form-item label="食堂楼栋个数" prop="diningRoomNum">
              <el-input v-model="editform.diningRoomNum" clearable></el-input>
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
import { fetchList, addObj, delObj, putObj } from "@/api/platform/area/park";
import { tableOption } from "@/const/crud/platform/area/park";
import { mapGetters } from "vuex";
import { isMobile } from "@/util/validate";

export default {
  name: "park",
  data() {
    var validateParkName = (rule, value, callback) => {
      var rename = /^[\u4e00-\u9fa5_a-zA-Z0-9]{1,30}$/;
      if (!rename.test(value)) {
        callback(
          new Error("园区名称只允许汉字、字母与数字的组合,最长为30个字符")
        );
      } else {
        callback();
      }
    };
    var validateParkLongitude = (rule, value, callback) => {
      var longrg = /^(\-|\+)?(((\d|[1-9]\d|1[0-7]\d|0{1,3})\.\d{0,6})|(\d|[1-9]\d|1[0-7]\d|0{1,3})|180\.0{0,6}|180)$/;
      if (!longrg.test(value)) {
        callback(new Error("经度整数部分为0-180,小数部分为0到6位"));
      } else {
        callback();
      }
    };
    var validateParkLatitude = (rule, value, callback) => {
      var latreg = /^(\-|\+)?([0-8]?\d{1}\.\d{0,6}|90\.0{0,6}|[0-8]?\d{1}|90)$/;
      if (!latreg.test(value)) {
        callback(new Error("纬度整数部分为0-90,小数部分为0到6位"));
      } else {
        callback();
      }
    };
    var validateParkPhone = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error("请输入咨询电话"));
      } else {
        var val = value.replace(/(^\s*)|(\s*$)/g, "");
        if (!isMobile(val) && !/^0\d{2,3}-\d{7,8}$/.test(val)) {
          callback(new Error("请输入正确的咨询电话"));
        } else {
          callback();
        }
      }
    };
    const isNum = (rule, value, callback) => {
      const age= /^[0-9]*$/
      if (!age.test(value)) {
        callback(new Error('只能输入数字'))
      }else{
        callback()
      }
    }
    return {
      addLoading: false,
      editLoading: false,
      addFormVisible: false, //添加园区，显示切换
      editFormVisible: false, //编辑园区，显示切换
      addform: {
        //添加园区，表单
        parkName: "",
        parkAddress: "",
        parkLongitude: "",
        parkLatitude: "",
        parkPhone: "",
        bridgeUrl: "",
        area: 0,
        workShopNum:0,
        diningRoomNum:0
      },
      rules: {
        parkName: [
          { required: true, message: "请输入园区名称", trigger: "blur" },
          { validator: validateParkName, trigger: "blur" }
        ],
        bridgeUrl: [
          { required: true, message: "请输入转发URL", trigger: "blur" }
        ],
        parkLongitude: [
          { required: true, message: "请输入园区经度", trigger: "blur" },
          { validator: validateParkLongitude, trigger: "blur" }
        ],
        parkLatitude: [
          { required: true, message: "请输入园区纬度", trigger: "blur" },
          { validator: validateParkLatitude, trigger: "blur" }
        ],
        parkPhone: [
          { required: true, message: "请输入咨询电话", trigger: "blur" },
          { validator: validateParkPhone, trigger: "blur" }
        ],
        area: [
          { required: true, message: "请输入占地面积（亩）", trigger: "blur" },
          { validator: isNum, trigger: "blur" }
        ],
        workShopNum: [
          { required: true, message: "请输入厂房个数", trigger: "blur" },
          { validator: isNum, trigger: "blur" }
        ],
        diningRoomNum: [
          { required: true, message: "请输入食堂楼栋个数", trigger: "blur" },
          { validator: isNum, trigger: "blur" }
        ]
      },
      editform: {
        //添加园区，表单
        id: "",
        parkName: "",
        parkAddress: "",
        parkLongitude: "",
        parkLatitude: "",
        parkPhone: "",
        bridgeUrl: "",
        area: 0,
        workShopNum:0,
        diningRoomNum:0
      },
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
      this.getList(this.page);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page);
    },
    handleEdit(row, index) {
      //点击编辑
      this.editFormVisible = true;

      this.editform = Object.assign({}, row);
    },
    editSubmit(formName) {
      //编辑内容确定
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editLoading = true;
          putObj(this.editform)
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
    editCancel(formName) {
      this.editFormVisible = false;
      this.resetEditFrom(formName);
    },
    addSubmit(formName) {
      //添加内容确定
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.addLoading = true;
          addObj(this.addform)
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
    addCancel(formName) {
      this.addFormVisible = false;
      this.resetAddFrom(formName);
    },
    resetAddFrom(formName) {
      this.$refs[formName].resetFields();
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
          elm("span", null, "确认删除该园区信息？ ")
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
          var msg = dataResponse.data.msg;
          var dataResult = dataResponse.data.data;
          if (dataResult == true) {
            _this.getList(this.page);
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
    handleSet(row) {
      const src = `/platform/area/park/setting`;
      this.$router.push({
        path: src,
        query: { parkId: row.id }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
