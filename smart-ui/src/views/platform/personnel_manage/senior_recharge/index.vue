<!--人资行政管理，员工充值名单-在职  -->
<template>
  <div class="my-basic-container parking_lot">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-radio-group v-model="searchForm.syncStatus" @change="radioChange">
            <el-radio-button :label="1">未同步</el-radio-button>
            <el-radio-button :label="2">已同步</el-radio-button>
          </el-radio-group>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空
            </el-button>
            <el-button type="primary" @click="specialRecharge" plain>特殊名单充值</el-button>
            <template v-if="searchForm.syncStatus===1">
              <el-button type="primary" icon="ali-icon-download" @click="syncRecharge()">生成充值名单</el-button>
              <el-button type="primary" icon="ali-icon-download" @click="syncToC6(searchForm)">同步充值名单到C6
              </el-button>
            </template>
            <el-button
              type="primary"
              :loading="exportLoading"
              @click="exportExcel"
              icon="icon-yutong-download"
            >导出excel
            </el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <!-- <el-form-item label="所属园区/BU/部门" prop="depIds">
            <parkSelect v-model="editform.parkId" @doChange="selectDevice"></parkSelect>
            <el-cascader
              expand-trigger="hover"
              :options="compOptions"
              :show-all-levels="false"
              :change-on-select="true"
              v-model="depIds"
              clearable
            ></el-cascader>
          </el-form-item> -->
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="BU" prop="compIds">
            <selectBU v-model="searchForm.compIds" :parkId="searchForm.parkId"></selectBU>
          </el-form-item>
          <el-form-item label="工号">
            <el-input v-model="searchForm.badge" placeholder="多个工号以空格分隔" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="生成状态">
            <el-select v-model="searchForm.isAccount" placeholder="请选择">
              <el-option label="全部" value=""></el-option>
              <el-option label="未生成" :value="2"></el-option>
              <el-option label="已生成" :value="1"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="考勤月份" prop="checkMonth">
            <el-date-picker
              v-model="searchForm.checkMonth"
              type="month"
              value-format="yyyy-MM"
              placeholder="考勤月份"
              clearable
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="入职日期" prop="snapTime" clearable>
            <el-date-picker
              v-model="snapTime"
              type="daterange"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </el-form-item>
        </el-form>
        <div>
          <el-button type="primary" @click="delOnce" plain>一键清理</el-button>
          <el-button type="primary" @click="delBatch" plain>批量删除</el-button>
          <el-switch
            v-model="isOpen"
            @change="openSelect"
            active-color="#10CC8F"
            inactive-color="#e7292e"
            style="margin: 0 5px 0 15px">
          </el-switch>开启勾选
        </div>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          @size-change="sizeChange"
          @current-change="currentChange"
          :option="tableOption"
          @selection-change="selectChange"
        >
          <template slot-scope="scope" slot="account">
            <span v-if="searchForm.syncStatus===2">{{scope.row.account}}</span>
            <span class="ft-blue" v-else @click="editAccount(scope.row)" >{{scope.row.account}}</span>
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-edit"
              @click="handleEdit(scope.row,scope.$index)"
            >查看
            </el-button>
          </template>
        </avue-crud>

        <el-dialog
          title
          class="dialog_form"
          @close="resetEditFrom('editform')"
          width="600px"
          :visible.sync="editFormVisible"
        >
          <div class="box-outer">
            <p class="box-orange">充值详情</p>
            <table class="lit-table">
              <tr>
                <td>工号</td>
                <td>{{detailForm.badge}}</td>
              </tr>
              <tr>
                <td>姓名</td>
                <td>{{detailForm.name}}</td>
              </tr>
              <tr>
                <td>所属园区</td>
                <td>{{detailForm.parkNames}}</td>
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
                <td>福利层次</td>
                <td>{{detailForm.welfareLevel}}</td>
              </tr>
              <tr>
                <td>入职时间</td>
                <td>{{detailForm.createTime}}</td>
              </tr>
              <tr>
                <td>餐补标准</td>
                <td>{{detailForm.standard}}</td>
              </tr>
              <tr>
                <td>考核月份</td>
                <td>{{detailForm.checkMonth}}</td>
              </tr>
              <tr>
                <td>应出勤</td>
                <td>{{detailForm.shouldOn}}</td>
              </tr>
              <tr>
                <td>实出勤</td>
                <td>{{detailForm.actualOn}}</td>
              </tr>
              <tr>
                <td>餐补结算</td>
                <td>{{detailForm.account}}</td>
              </tr>
              <tr>
                <td>同步状态</td>
                <td>{{detailForm.syncStatus}}</td>
              </tr>
            </table>
          </div>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="editCancel('editform')">确 定</el-button>
          </div>
        </el-dialog>
        <dlgSpecialRecharge ref="dlgSpecialRecharge" @refresh="refreshSpecialRecharge"/>
        <dlgAccount ref="dlgAccount" :row="curRow" @refresh="refreshAccount"/>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
  import {
    fetchList,
    exportTitle,
    getSeniorInfo,
    toC6,
    deleteRecharge
  } from "./recharge_service.js";
  import {getCompTree} from "@/api/platform/_publicService";
  import {tableOption} from "@/const/crud/platform/personnel_manage/staff_recharge";
  import {getDatePreMonth} from "@/util/util";
  import {mapGetters} from "vuex";
  import dlgSpecialRecharge from "./components/dlg_specialRecharge";
  import dlgAccount from "./components/dlg_account";
  import selectBU from "./components/select_bu";

  export default {
    name: "badgeLoss",
    components: {
      dlgSpecialRecharge,
      dlgAccount,
      selectBU
    },
    data() {
      return {
        isOpen: true,
        ids: [],
        curRow: {},
        snapTime: [],
        editFormVisible: false,
        compOptions: [],
        depIds: [],
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 10 // 每页显示多少条
        },
        searchForm: {
          checkMonth: undefined,
          parkId: undefined,
          depId: undefined,
          compIds: [],
          badge: undefined,
          name: undefined,
          startEntryTime: undefined,
          endEntryTime: undefined,
          rechargeType: 2,
          syncStatus: 1 //默认显示未同步的
        },
        detailForm: {
          badge: undefined,
          name: undefined,
          parkNames: undefined,
          compName: undefined,
          depName: undefined,
          welfareLevel: undefined,
          createTime: undefined,
          standard: undefined,
          checkMonth: undefined,
          shouldOn: undefined,
          actualOn: undefined,
          account: undefined,
          syncStatus: undefined
        },
        eTitle: "",
        exportLoading: false,
        tableData: [],
        tableOption: tableOption,
        listTotal: 5, //同步总条数
      };
    },
    created() {
      this.searchForm.checkMonth = getDatePreMonth();
      this.getList(this.page,this.searchForm);
      getCompTree().then(response => {
        this.compOptions = response.data.data;
      });
    },
    mounted: function () {
    },
    computed: {
      ...mapGetters(["permissions"]),
      begainTime: function() {
        if (this.snapTime) {
          return this.snapTime[0];
        } else {
          return undefined;
        }
      },
      endTime: function() {
        if (this.snapTime) {
          return this.snapTime[1];
        } else {
          return undefined;
        }
      }
    },
    methods: {
      openSelect(){
        this.ids = []
        this.$refs.crud.selectClear()
        this.$refs.crud.$options.propsData.option.selection = this.isOpen
      },
      selectChange(val) {
        var idArr = [];
        if (val.length > 0) {
          val.forEach(function(element) {
            idArr.push(element.id);
          }, this);
        }
        this.ids = idArr;
      },
      //特殊名单充值
      specialRecharge(){
        this.$refs.dlgSpecialRecharge && this.$refs.dlgSpecialRecharge.open()
      },
      //编辑应发餐补
      editAccount(row){
        this.curRow = row
        this.$refs.dlgAccount && this.$refs.dlgAccount.open()
      },
      refreshSpecialRecharge(badges){
        let page = {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 10 // 每页显示多少条
        }
        this.getList( page, {
          badge: badges
        });
      },
      refreshAccount(){
        this.getList(this.page,this.searchForm);
      },
      //批量删除
      async delBatch(){
        let params = {}
        if(this.isOpen){
          if(this.ids.length>0){
            params = { ids: this.ids }
          }else{
            this.$message.error('当前开启了勾选，请选中要操作的数据');
            return
          }
        }else{
          params = this.searchForm
        }
        const elm = this.$createElement;
        await this.$msgbox({
          message: elm("p", { attrs: { class: "smallp" } }, [
            elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
            elm("span", null, "确认批量删除信息？ ")
          ]),
          showCancelButton: true,
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          customClass: "small_dialog",
          center: true
        })
        const res = await deleteRecharge(params)
        if(res.data.data){
          this.getList(this.page, this.searchForm);
          this.ids = []
          this.$notify({
            title: "成功",
            message: "批量删除成功",
            type: "success",
            duration: 2000
          });
        }else{
          this.$notify.error({
            title: '失败',
            message: '批量删除失败',
            duration: 2000
          });
        }
      },
      //一键清理
      async delOnce(){
        const elm = this.$createElement;
        await this.$msgbox({
          message: elm("p", { attrs: { class: "smallp" } }, [
            elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
            elm("span", null, "确定一键清理所有信息？ ")
          ]),
          showCancelButton: true,
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          customClass: "small_dialog",
          center: true
        })
        let obj = {
          checkMonth: this.searchForm.checkMonth,
          rechargeType: this.searchForm.rechargeType,
          syncStatus: this.searchForm.syncStatus
        }
        const res = await deleteRecharge(obj)
        if(res.data.data){
          this.getList(this.page, this.searchForm);
          this.$notify({
            title: "成功",
            message: "一键清理成功",
            type: "success",
            duration: 2000
          });
        }else{
          this.$notify.error({
            title: '失败',
            message: '一键清理失败',
            duration: 2000
          });
        }
      },
      radioChange() {
        this.getList(this.page,this.searchForm);
      },
      async syncToC6(form) {
        let params = {}
        if(this.isOpen){
          if(this.ids.length>0){
            params = { ids: this.ids }
            this.listTotal = this.ids.length
          }else{
            this.$message.error('当前开启了勾选，请选中要操作的数据');
            return
          }
        }else{
          params = form
          await this.getListTotal(this.page,this.searchForm)
        }
        var _this = this;
        const elm = this.$createElement;
        this.$msgbox({
          message: elm("p", { attrs: { class: "smallp" } }, [
            elm("i", { attrs: { class: "smallInfo checkIn" } }, ""),
            elm("span", null, `确认同步${this.listTotal}条员工信息至C6？`)
          ]),
          showCancelButton: true,
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          customClass: "small_dialog",
          center: true
        })
          .then(async function() {
            return await toC6(params);
          })
          .then(response => {
            var msg = response.data.msg;
            var dataResult = response.data.code;
            if (dataResult === 0) {
              _this.getList(this.page, this.searchForm);
              this.ids = []
              _this.$notify({
                title: "成功",
                message: "同步成功",
                type: "success",
                duration: 2000
              });
              if(response.data.data){
                this.$alert(response.data.data, '提示', {
                  confirmButtonText: '确定',
                });
              }
            } else {
              _this.$notify({
                title: "失败",
                message: msg,
                type: "error",
                duration: 2000
              });
            }
          })
          .catch(error => { console.error(error) });
      },
      getList(page, params) {
        this.tableLoading = true;
        params.startEntryTime = this.begainTime;
        params.endEntryTime = this.endTime;
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
      //获取总条数
      async getListTotal(page, params) {
        params.startEntryTime = this.begainTime;
        params.endEntryTime = this.endTime;
        const res = await fetchList(
            {
              current: page.currentPage,
              size: page.pageSize
            },
            params
        )
        this.listTotal = res.data.data.total
      },
      handleEdit(row, index) {
        //点击编辑
        this.editFormVisible = true;
        this.detailForm = row;
        if(this.detailForm.syncStatus == 1) {
          this.detailForm.syncStatus = "未同步"
        }
        if(this.detailForm.syncStatus == 2) {
          this.detailForm.syncStatus = "已同步"
        }
      },
      editCancel(formName) {
        this.editFormVisible = false;
        this.resetEditFrom(formName);
      },
      resetEditFrom(formName) {
        this.$refs[formName].clearValidate();
      },
      /**
       * 搜索回调
       */
      searchSubmit(form) {
        this.page.currentPage = 1;
        this.getList(this.page, form);
      },
      async syncRecharge() {
        var _this = this;
        const elm = this.$createElement;
        this.$msgbox({
          message: elm("p", { attrs: { class: "smallp" } }, [
            elm("i", { attrs: { class: "smallInfo checkIn" } }, ""),
            elm("span", null, "等待时间较长，是否确认生成名单？")
          ]),
          showCancelButton: true,
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          customClass: "small_dialog",
          center: true
        })
          .then(async function() {
            return await getSeniorInfo();
          })
          .then(response => {

            var msg = response.data.msg;
            var dataResult = response.data.data;
            if (dataResult === true) {
              this.$notify({
                title: "成功",
                message: "生成充值名单成功",
                type: "success",
                duration: 2000
              });
              this.getList(this.page, this.searchForm);
            } else {
              this.$notify.error({
                title: '失败',
                message: msg,
                duration: 2000
              });
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
      /**
       * 清空搜索
       */
      resetFrom(formName) {
          this.$refs[formName].resetFields();
          this.depIds = [];
          this.searchForm.checkMonth =  null;
          this.searchForm.badge = null;
          this.searchForm.name = null;
          this.snapTime = [];
          this.page.currentPage = 1;
          this.getList(this.page,this.searchForm);

      },
      exportExcel() {
        var _this = this;
        const elm = this.$createElement;
        this.$msgbox({
          message: elm("p", { attrs: { class: "smallp" } }, [
            elm("i", { attrs: { class: "smallInfo checkIn" } }, ""),
            elm("span", null, "等待时间较长，是否确认导出excel？")
          ]),
          showCancelButton: true,
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          customClass: "small_dialog",
          center: true
        }).then(async function() {
          return _this.export2Excel();
        })
      },
      //导出
      async export2Excel() {
        require.ensure([], async () => {
          this.exportLoading = true;
          const { export_json_to_excel } = require("@/vendor/Export2Excel");
          const tHeader = [
            "工号",
            "姓名",
            "所属园区",
            "BU",
            "部门",
            "福利层次",
            "入职时间",
            "餐补标准",
            "考核月份",
            "应出勤",
            "实出勤",
            "餐补结算",
            "备注",
            "同步状态"
          ];
          const filterVal = [
            "badge",
            "name",
            "parkNames",
            "compName",
            "depName",
            "welfareLevel",
            "createTime",
            "standard",
            "checkMonth",
            "shouldOn",
            "actualOn",
            "account",
            "blank",
            "syncStatus"
          ];
          await exportTitle().then(response => {
            this.eTitle = response.data.data.records;
          }).catch(err => {
            this.exportLoading = false;
          });
          let params = Object.assign({}, this.searchForm);
          await fetchList(
            {
              current: 1,
              size: this.page.total,
            },
            params

          ).then(response => {
            const list = response.data.data.records;
            list.forEach(function(item) {
              item.syncStatus == 1 ? (item.syncStatus = "未同步") : (item.syncStatus = "已同步");
            });
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, `${this.eTitle}&(${this.snapTime})`);
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
