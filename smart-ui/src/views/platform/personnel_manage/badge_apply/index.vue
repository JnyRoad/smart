<!--人资行政管理，厂牌补领记录  -->
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
            <el-button
              type="primary"
              icon="ali-icon-download"
              @click="export2Excel()"
            >导出excel</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <template>
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
          </template>
          <el-form-item label="工号" prop="badge">
            <el-input v-model="searchForm.badge" placeholder="请输入工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="办理状态" prop="state">
            <el-select v-model="searchForm.state" placeholder="请选择办理状态">
              <template v-for="(item, index) in status">
                <el-option :label="item.desc" :value="item.code" :key="index"></el-option>
              </template>
            </el-select>
          </el-form-item>
          <el-form-item label="挂失时间">
            <el-date-picker
              v-model="times"
              format="yyyy-MM-dd HH:mm:ss"
              value-format="yyyy-MM-dd HH:mm:ss"
              :default-time="['00:00:00', '23:59:59']"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          @size-change="sizeChange"
          @current-change="currentChange"
          :option="tableOption"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              v-if="scope.row.state == 1"
              type="text"
              icon="el-icon-edit"
              @click="handleAgree(scope.row)"
            >同意</el-button>
            <el-button
              v-if="scope.row.state == 1"
              type="text"
              icon="el-icon-edit"
              @click="handleRefuse(scope.row)"
            >拒绝</el-button>
            <el-button
              v-if="scope.row.state == 2"
              type="text"
              icon="el-icon-edit"
              @click="handleConfirm(scope.row, 'agreeForm')"
            >确认领取</el-button>
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">查看</el-button>
          </template>
        </avue-crud>

        <el-dialog title class="dialog_form" width="700px" :visible.sync="editFormVisible">
          <div class="box-outer">
            <p class="box-orange">申请信息</p>
            <table class="lit-table">
              <tr>
                <td>员工工号</td>
                <td>{{detailForm.badge}}</td>
              </tr>
              <tr>
                <td>员工姓名</td>
                <td>{{detailForm.name}}</td>
              </tr>
              <tr>
                <td>所属园区</td>
                <td>{{detailForm.parkName}}</td>
              </tr>
              <tr>
                <td>BU</td>
                <td>{{detailForm.compName}}</td>
              </tr>
              <tr>
                <td>部门</td>
                <td>{{detailForm.depName}}</td>
              </tr>
              <tr>
                <td>申请原因</td>
                <td>{{detailForm.reason}}</td>
              </tr>
              <tr>
                <td>描述</td>
                <td>{{detailForm.remark}}</td>
              </tr>
              <tr>
                <td>厂牌费用</td>
                <td>{{detailForm.price}}</td>
              </tr>
              <tr>
                <td>申请时间</td>
                <td>{{detailForm.createTime}}</td>
              </tr>
            </table>
            <p class="box-orange">办理进度</p>
            <div class="record">
              <template v-for="(item, index) in detailForm.operaList">
                <div class="record-item" :key="index">
                  <div class="record-item__left">
                    <div class="num">{{index+1}}</div>
                    <i class="line1"></i>
                  </div>
                  <div class="record-item__right">
                    <div>
                      <span style="padding-right: 15px;">{{item.operateTitleDesc}}</span>
                      <span>{{item.createrName}}</span>
                    </div>
                    <div class="line2">
                      <span style="padding-right: 15px;">{{item.operateTypeDesc}}</span>
                      <span>{{item.createTime}}</span>
                    </div>
                    <div class="line2">{{item.remark}}</div>
                  </div>
                </div>
              </template>
            </div>
          </div>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="editCancel('editform')">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog
          title="是否同意为该员工办理厂牌卡？"
          class="dialog_form"
          width="600px"
          :visible.sync="agreeFormVisible"
        >
          <el-form :rules="rulesAgree" ref="agreeForm" :model="agreeForm" label-width="100px">
            <el-form-item label="领取地址" prop="address">
              <el-input v-model="agreeForm.address"></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="agreeCancel('agreeForm')" plain>取 消</el-button>
            <el-button type="primary" @click="agreeSubmit('agreeForm')" :loading="agreeLoading">确 定</el-button>
          </div>
        </el-dialog>

        <el-dialog
          title="是否拒绝为该员工办理厂牌卡？"
          class="dialog_form"
          width="600px"
          :visible.sync="refuseFormVisible"
        >
          <el-form :rules="rulesRefuse" ref="agreeForm" :model="agreeForm" label-width="100px">
            <el-form-item label="拒绝原因" prop="refuseReason">
              <el-input v-model="agreeForm.refuseReason"></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="agreeCancel('agreeForm')" plain>取 消</el-button>
            <el-button type="primary" @click="refuseSubmit('agreeForm')" :loading="agreeLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchList,
  editObj,
  getObj,
  getOperaStatus
} from "./badge_service";
import { getCompTree } from "@/api/platform/_publicService";
import { tableOption } from "@/const/crud/platform/personnel_manage/badge_apply";
import { isArrayFn } from "@/util/util";
import { mapGetters } from "vuex";

export default {
  name: "badgeApply",
  data() {
    return {
      agreeLoading: false,
      editFormVisible: false,
      agreeFormVisible: false,
      refuseFormVisible: false,
      compOptions: [],
      depIds: [],
      agreeForm: {
        id: "",
        address: "",
        refuseReason: "",
        state: ""
      },
      times: [],
      status: [],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 10 // 每页显示多少条
      },
      searchForm: {
        parkId: undefined,
        depId: undefined,
        compId: undefined,
        badge: undefined,
        startTime: undefined,
        endTime: undefined,
        states: undefined
      },
      detailForm: {
        badge: undefined,
        name: undefined,
        compName: undefined,
        depName: undefined,
        parkName: undefined,
        startTime: undefined
      },
      tableData: [],
      tableOption: tableOption,
      rulesAgree: {
        address: [
          { required: true, message: "请输入领取地址", trigger: "blur" }
        ]
      },
      rulesRefuse: {
        refuseReason: [
          { required: true, message: "请输入拒绝原因", trigger: "blur" }
        ]
      },
      ruleConfirm: {
        state: [
          { required: true, message: "请选择是否确认领取", trigger: "blur" }
        ]
      }
    };
  },
  watch: {
    times(val, oldVal) {
      if (!val || val.length === 0) {
        this.searchForm.startTime = undefined;
        this.searchForm.endTime = undefined;
      } else {
        this.searchForm.startTime = val[0];
        this.searchForm.endTime = val[1];
      }
    },
    depIds(newVal, oldVal) {
      if (isArrayFn(newVal) && newVal.length > 0) {
        const depLength = newVal.length;
        if (depLength == 3) {
          this.searchForm.depId = this.depIds[2];
          this.searchForm.compId = undefined;
          this.searchForm.parkId = undefined;
        } else if (depLength >= 2) {
          this.searchForm.depId = undefined;
          this.searchForm.compId = this.depIds[1];
          this.searchForm.parkId = undefined;
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
  created() {
    this.getList(this.page,this.searchForm);
    this.getEnum();
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    getList(page, params) {
      this.tableLoading = true;
      fetchList(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
      this.tableLoading = false;
    },
    agreeSubmit(formName) {
      this.agreeForm.state = 2;
      this.editSubmit(formName);
    },
    refuseSubmit(formName) {
      this.agreeForm.state = 4;
      this.editSubmit(formName);
    },
    editSubmit(formName) {
      //编辑内容确定
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.agreeLoading = true;
          editObj(this.agreeForm)
            .then(dataResponse => {
              this.agreeFormVisible = false;
              this.refuseFormVisible = false;
              this.agreeLoading = false;
              this.getList(this.page,this.searchForm);
            })
            .catch(() => {
              this.agreeLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    handleEdit(row) {
      //点击编辑
      this.editFormVisible = true;
      getObj({ id: row.id }).then(response => {
        this.detailForm = response.data.data;
      });
    },
    handleAgree(row) {
      this.agreeFormVisible = true;
      this.agreeForm.id = row.id;
    },
    handleRefuse(row) {
      this.refuseFormVisible = true;
      this.agreeForm.id = row.id;
    },
    handleConfirm(row, formName) {
      this.agreeForm.id = row.id;
      this.agreeForm.state = 3;

      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo checkIn" } }, ""),
          elm("span", null, "确定员工已领取了厂牌？")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return editObj(_this.agreeForm);
        })
        .then(dataResponse => {
          var msg = dataResponse.data.msg;
          var dataResult = dataResponse.data.data;
          if (dataResult == true) {
            _this.getList(_this.page, _this.searchForm);
            _this.$notify({
              title: "确认领取成功",
              message: "确认领取成功",
              type: "success",
              duration: 2000
            });
          } else if (dataResult === false) {
            _this.$notify({
              title: "确认领取失败",
              message: msg,
              type: "error",
              duration: 2000
            });
          }
        })
        .catch(err => { console.error(err) });
    },
    getEnum() {
      getCompTree().then(response => {
        this.compOptions = response.data.data;
      });
      getOperaStatus().then(response => {
        this.status = response.data.data;
      });
    },
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "员工工号",
          "员工姓名",
          "所属园区",
          "BU",
          "部门",
          "申请原因",
          "申请时间",
          "办理状态",
          "厂牌价格"
        ];
        const filterVal = [
          "badge",
          "name",
          "parkName",
          "compName",
          "depName",
          "reason",
          "createTime",
          "stateDesc",
          "price"
        ];
        let params = Object.assign({}, this.searchForm);
        fetchList(
          {
            current: 1,
            size: this.page.total,
          },
          params
        ).then(response => {
          const list = response.data.data.records;
          const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, `厂牌补领记录&(${this.times})`);
          this.exportLoading = false;
        })
          .catch(err => {
            this.exportLoading = false;
          });
      });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
    agreeCancel(formName) {
      this.refuseFormVisible = false;
      this.agreeFormVisible = false;
      this.resetFrom(formName);
    },
    editCancel() {
      this.editFormVisible = false;
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1;
      this.getList(this.page, form);
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
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      this.$refs[formName]&&this.$refs[formName].resetFields();
      this.$refs[formName]&&this.$refs[formName].clearValidate();
      this.times = [];
      this.depIds = [];
      this.searchForm.depId = undefined;
      this.searchForm.compId = undefined;
      this.searchForm.parkId = undefined;
      this.searchForm.startTime = undefined;
      this.searchForm.endTime = undefined;
      this.page.currentPage = 1;
      this.getList(this.page,this.searchForm);
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
.record {
  padding-left: 30px;
  margin-top: 20px;
  &-item {
    display: flex;
    &__left {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding-right: 8px;
      .num {
        width: 20px;
        height: 20px;
        border-radius: 50%;
        border: 1px solid #e0e0e0;
        text-align: center;
        line-height: 18px;
      }
      .line1 {
        flex: 1;
        width: 1px;
        border-left: 1px solid #e0e0e0;
      }
    }
    &__right {
      .line2 {
        margin: 5px 0;
      }
      padding-bottom: 25px;
    }
  }
  &-item:last-child {
    .record-item__left {
      .line1 {
        display: none;
      }
    }
  }
}
</style>
