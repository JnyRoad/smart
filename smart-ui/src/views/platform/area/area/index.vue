<!--区域管理，区域管理  -->
<template>
  <div class="my-basic-container mycard2 area">
    <el-scrollbar class="my-scrollbar" :native="false">
      <div class="box-outer box-left">
        <el-scrollbar class="my-lit-scrollbar" :native="false">
          区域检索
          <div style="margin:10px 0;">
            <el-input placeholder="输入关键字进行过滤" clearable v-model="filterText" size="mini">
              <el-button type="info" slot="append" icon="el-icon-search"></el-button>
            </el-input>
          </div>
          <el-tree
            class="my-menu-tree"
            :data="treeData"
            highlight-current
            :props="defaultProps"
            :filter-node-method="filterNode"
            @node-click="handleNodeClick"
            ref="roomtree"
            default-expand-all
          ></el-tree>
          <el-button-group style="margin: 20px 0;">
            <el-button
              type="primary"
              icon="plus"
              v-if="currentLevel > 0 && currentLevel < 3 "
              @click="handlerAdd"
            >添加</el-button>
            <el-button type="primary" icon="edit" v-if="currentLevel>1" @click="handlerEdit">编辑</el-button>
            <el-button type="primary" icon="delete" v-if="currentLevel>1" @click="handleDelete">删除</el-button>
          </el-button-group>
        </el-scrollbar>
      </div>
      <div class="my-basic-inner">
        <div class="box-outer">
          <avue-crud
            ref="crud"
            :page="page"
            :data="tableData"
            :table-loading="tableLoading"
            @size-change="sizeChange"
            @current-change="currentChange"
            :option="areaOption"
          >
            <template slot-scope="scope" slot="parentAreaName">
              <span>{{scope.row.parentAreaName?scope.row.parentAreaName:"顶级" }}</span>
            </template>
          </avue-crud>
        </div>
      </div>
      <el-dialog title="区域管理" class="dialog_form" width="500px" :visible.sync="areaFormVisible">
        <el-form ref="areaform" :rules="areaRules" :model="areaform" label-width="80px">
          <el-form-item label="区域名称" prop="areaName">
            <el-input v-model="areaform.areaName" clearable></el-input>
          </el-form-item>
          <el-form-item label="上级区域" prop="parentArea">
            <el-input v-model="areaform.parentArea " disabled></el-input>
          </el-form-item>
          <el-form-item label="经度" prop="areaLongitude">
            <el-input v-model="areaform.areaLongitude" clearable></el-input>
          </el-form-item>
          <el-form-item label="纬度" prop="areaLatitude">
            <el-input v-model="areaform.areaLatitude" clearable></el-input>
          </el-form-item>
          <el-form-item label="备注">
            <el-input type="textarea" :rows="3" v-model="areaform.remark" placeholder clearable></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="areaFormVisible = false" plain>取 消</el-button>
          <template v-if="!isEdit">
            <el-button type="primary" @click="addSubmit('areaform')" :loading="addLoading">添 加</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="editSubmit('areaform')" :loading="editLoading">更 新</el-button>
          </template>
        </div>
      </el-dialog>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchList,
  putObj,
  addObj,
  delObj,
  allObj
} from "@/api/platform/area/area";
import { areaOption } from "@/const/crud/platform/area/area";
import { mapGetters } from "vuex";

export default {
  name: "area",
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
    return {
      addLoading: false,
      editLoading: false,
      areaFormVisible: false, //区域管理
      isEdit: true,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableLoading: false,
      tableData: [],
      areaOption: areaOption,

      filterText: "",
      treeData: [],
      defaultProps: {
        children: "children",
        label: "label"
      },
      currentData: {
        //当前点击节点的数据
        id: undefined,
        label: undefined,
        pid: "",
        pname: "",
        parkId: "",
        areaLongitude: undefined,
        areaLatitude: undefined,
        remark: undefined
      },
      treeForm: { parkId: "", pid: "" },
      currentLevel: undefined, //当前节点的层级，顶级的level值为1，第二级的level值为2，……
      areaform: {
        parkId: undefined,
        parentArea: undefined,
        pid: undefined,
        areaName: "",
        areaLongitude: undefined,
        areaLatitude: undefined,
        remark: undefined
      },
      areaRules: {
        areaName: [
          { required: true, message: "请输入区域名称", trigger: "blur" },
          { validator: validateParkName, trigger: "blur" }
        ],
        areaLongitude: [
          { required: true, message: "请输入区域经度", trigger: "blur" },
          { validator: validateParkLongitude, trigger: "blur" }
        ],
        areaLatitude: [
          { required: true, message: "请输入区域纬度", trigger: "blur" },
          { validator: validateParkLatitude, trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList(this.page);
  },
  mounted: function() {
    this.getTree();
  },
  computed: {
    ...mapGetters(["permissions"])
  },
  watch: {
    filterText(val) {
      this.$refs.roomtree.filter(val);
    },
    currentData(val) {
      if (val.level == 1) this.areaform.pid = 0;
      else this.areaform.pid = val.id;
      this.areaform.parentArea = val.label;
    }
  },
  methods: {
    // 添加确认
    addSubmit(formName) {
      this.areaform.pid = this.currentData.id;
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.addLoading = true;
          if (this.currentLevel == 1) {
            this.areaform.pid = 0;
          }
          addObj(this.areaform)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.getTree();
                this.getList(this.page, this.treeForm);
                this.areaFormVisible = false;
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
            .catch(() => {
              this.addLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    // 编辑确认
    editSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editLoading = true;
          if (this.currentLevel == 1) {
            this.areaform.pid = 0;
          }
          putObj(this.areaform)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.getTree();
                this.getList(this.page, this.treeForm);
                this.areaFormVisible = false;
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
            .catch(() => {
              this.editLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    //重置表单
    resetForm() {
      this.areaform = {
        parkId: this.currentData.parkId,
        pid: this.currentData.id,
        parentArea: this.currentData.label,
        areaName: "",
        areaLongitude: undefined,
        areaLatitude: undefined,
        remark: undefined
      };
    },
    //点击编辑
    handlerEdit() {
      this.areaform = this.currentData;
      this.areaform.areaName = this.currentData.label;
      this.areaform.parentArea = this.currentData.pname;
      this.isEdit = true;
      this.areaFormVisible = true;
    },
    //点击添加
    handlerAdd() {
      this.isEdit = false;
      this.resetForm();
      this.areaFormVisible = true;
    },
    //点击删除
    handleDelete() {
      let _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该区域信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          const id = _this.currentData.id;
          return delObj(id);
        })
        .then(dataResponse => {
          _this.getList(this.page, this.treeForm);
          _this.getTree();
          _this.$notify({
            title: "成功",
            message: "删除成功",
            type: "success",
            duration: 2000
          });
        })
        .catch(err => { console.error(err) });
    },
    handleNodeClick(data, node) {
      //节点点击事件
      this.currentData = data;
      this.currentLevel = node.level;
      this.treeForm.parkId = "";
      this.treeForm.pid = "";
      this.page.currentPage = 1;

      if (this.currentLevel == 1) {
        this.treeForm.pid = 0;
        this.treeForm.parkId = data.id;
        this.currentData.pid = 0;
        this.currentData.parkId = data.id;
      } else {
        this.treeForm.pid = data.id;
        if (this.currentLevel == 2) {
          this.currentData.parkId = node.parent.data.id;
        } else if (this.currentLevel == 3) {
          this.currentData.parkId = node.parent.parent.data.id;
        }
      }
      this.getList(this.page, this.treeForm);
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    getTree() {
      allObj().then(response => {
        var array = response.data.data;
        this.treeData = array;
      });
    },
    getList(page, params) {
      let _this = this;
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
        if (this.tableData.length == 0 && params.parkId == "") {
          var data = { id: params.pid };
          _this.getSelfList(page, data);
        } else {
          this.tableData.forEach(function(element) {
            if (element.pid == 0) {
              element.parentAreaName = element.parkName;
            }
          }, this);
          this.page.total = response.data.data.total;
          this.tableLoading = false;
        }
      });
      this.tableLoading = false;
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page, this.treeForm);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page, this.treeForm);
    },
    getSelfList(page, params) {
      var _this = this;
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
        this.tableData.forEach(function(element) {
          if (element.pid == 0) {
            element.parentAreaName = element.parkName;
          }
        }, this);
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
      this.tableLoading = false;
    },
    handleDetail(row, index) {
      const src = `/platform/area/area/person/${row.id}`;
      this.$router.push({
        path: src,
        query: {}
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.tree-outer {
  width: 300px;
  margin-right: 30px;
  float: left;
  border: 1px solid #f2f2f2;
  ::v-deep .avue-tree__filter {
    padding: 15px 20px;
    background: #f0f0f0;
  }
  ::v-deep .el-tree {
    padding: 0 20px 30px;
  }
}
.table-card {
  width: 100%;
  padding-left: 330px;
}
@media only screen and (min-width: 992px) {
  ::v-deep .el-dialog {
    ::v-deep .el-col-md-12 {
      width: 100%;
    }
  }
}
</style>
