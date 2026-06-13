<!--水电抄表模板  -->
<template>
  <div class="my-basic-container room">
    <div class="tip1">信息提示：生成水电明细后，数据将不能再修改</div>
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <avue-tabs :option="tabOption" @change="tabChange" class="tabs">
          </avue-tabs>
          <div class="top-right">
            <el-button type="primary" @click="sureGenerateDetail">生成水电明细</el-button>
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchform)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchform')"
              plain
            >重置</el-button>
            <!-- <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">生成记录</el-button> -->
          </div>
        </div>
        <el-form ref="searchform" :inline="true" :model="searchform" size="mini" class="topForm">
          <el-form-item label="园区" prop="parkId">
            <parkSelect
              @doChange="doParkChange"
              v-model="searchform.parkId"
            ></parkSelect>
          </el-form-item>
          <el-form-item label="楼栋" prop="dormitoryId">
            <dormSelect
              @doChange="doDormChange"
              :parkId="searchform.parkId"
              v-model="searchform.dormitoryId"
            ></dormSelect>
          </el-form-item>
          <el-form-item label="楼层" prop="floorId">
            <floorSelect
              :parkId="searchform.parkId"
              :dormitoryId="searchform.dormitoryId"
              v-model="searchform.floorId"
            ></floorSelect>
          </el-form-item>
          <el-form-item label="房间号" prop="roomName">
            <el-input v-model="searchform.roomName" placeholder="模糊查询" clearable></el-input>
          </el-form-item>
          <el-form-item label="抄表月份" prop="meterMonth">
            <el-date-picker v-model="searchform.meterMonth" type="month" value-format="yyyy-MM" placeholder="选择月份"></el-date-picker>
          </el-form-item>
          <el-form-item label="抄表状态" prop="status">
            <el-select v-model="searchform.status" placeholder="请选择">
              <el-option
                v-for="item in isMeterStatusArr"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="结算状态" prop="statementStatus">
            <el-select v-model="searchform.statementStatus" placeholder="请选择">
              <el-option
                v-for="item in isStatementStatusArr"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="mainOption"
          @size-change="sizeChange"
          @current-change="currentChange"
        >
          <template slot-scope="scope" slot="menu">
            <!-- status: 0 未抄表， 1部分抄表 2已抄完-->
            <!-- 没抄完的时候  都可以抄表 -->
            <!-- <el-button
              type="text"
              icon="el-icon-edit"
              v-if="scope.row.status !==2"
              @click="handleAddMeterReadData(scope.row, false)"
            >抄表</el-button> -->
            <el-button
              type="text"
              icon="el-icon-view"
              v-if="scope.row.status === 1 || scope.row.status === 2"
              @click="handleAddMeterReadData(scope.row, true)"
            >查看抄表数据</el-button>
            <!-- 只有普通水电 有修改入住人天 和 查看修改记录 -->
            <template v-if="activeTab=='tab1'">
              <!-- 未结算 且 已抄完 时可修改 -->
              <el-button
                type="text"
                icon="el-icon-view"
                v-if="scope.row.statementStatus!=1 && scope.row.status===2"
                @click="editAvgHandler(scope.row)"
              >修改入住人天</el-button>
              <el-button
                type="text"
                icon="el-icon-view"
                v-if="scope.row.status===2"
                @click="getDayRecord(scope.row)"
              >查看人天修改记录</el-button>
            </template>
            <!-- isRevise 是否有重置 -->
            <el-button
              type="text"
              icon="el-icon-view"
              :disabled="scope.row.isRevise!=1"
              @click="recordHandler(scope.row.reviseInfo)"
            >查看上月止度修改记录</el-button>
          </template>
          <template slot-scope="scope" slot="status">
            <span>{{scope.row.status === 1 ?'部分抄表': scope.row.status === 2?'已抄完':'未抄表'}}</span>
          </template>
          <template slot-scope="scope" slot="statementstatus">
            <span>{{scope.row.statementStatus === 1 ?'已结算': '未结算'}}</span>
          </template>
        </avue-crud>
        <!-- 生成房间水电记录弹框 -->
        <el-dialog
          title="添加房间水电记录"
          class="dialog_form"
          width="550px"
          @close="resetAddForm('addForm')"
          :visible.sync="addFormVisible"
        >
          <el-form :rules="addRules" ref="addForm" :model="addForm" label-width="80px">
            <el-form-item label="园区" prop="parkId">
              <parkSelect v-model="addForm.parkId" @doChange="doParkChange2"></parkSelect>
            </el-form-item>
            <el-form-item label="楼栋" prop="dormitoryId">
              <dormSelect @doChange="doDormChange2" :parkId="addForm.parkId" v-model="addForm.dormitoryId"></dormSelect>
            </el-form-item>
            <el-form-item label="楼层" prop="floorId">
              <floorSelect @doChange="doFloorChange2" :parkId="addForm.parkId" :dormitoryId="addForm.dormitoryId" v-model="addForm.floorId"></floorSelect>
            </el-form-item>
            <el-form-item label="房间" prop="roomId">
              <roomSelect :parkId="addForm.parkId" :dormitoryId="addForm.dormitoryId" :floorId="addForm.floorId" v-model="addForm.roomId"></roomSelect>
            </el-form-item>
            <el-form-item label="月份" prop="meterMonth">
              <el-date-picker
                v-model="addForm.meterMonth"
                type="month"
                placeholder="选择月份"
                value-format="yyyy-MM"
              ></el-date-picker>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="resetAddForm('addForm')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading">保 存</el-button>
          </div>
        </el-dialog>

        <!-- 抄表弹出模板 -->
        <el-dialog
          title="查看抄表数据"
          class="dialog_form config_form"
          width="750px"
          @close="resetMeterReadData('configform')"
          :visible.sync="addMeterReadFormVisible"
        >
          <el-scrollbar class="my-scrollbar" :native="false">
            <section class="my-basic-inner config-inner">
              <el-form
                ref="configform"
                :inline="true"
                :model="configform"
                size="mini"
                :disabled="isStatement"
              >
                <div>
                  月份：{{configform.meterMonth}}
                  <span class="tips" v-if="!isRead">信息提示：请确认抄表数据</span>
                </div>
                <table class="ruleTbl">
                  <template v-for="(categoryItem, index1) in configform.meterReadDetailList">
                    <tr class="tr2" :key="index1">
                      <td>{{getType(categoryItem.categoryId)}}</td>
                      <td class="tdL">上月底数</td>
                      <td>
                        <el-form-item
                          label
                          :prop="'meterReadDetailList.'+index1+'.preMonthNum'"
                          :rules="[
                            { required: true, message: '请输入', trigger: 'blur' },
                            { type: 'number', message: '请输入数字', trigger: 'blur' }
                          ]"
                        >
                          <el-input v-model.number="categoryItem.preMonthNum" placeholder="请输入" clearable></el-input>
                        </el-form-item>
                      </td>
                      <td class="tdL">本月底数</td>
                      <td>
                        <el-form-item
                          label
                          :prop="'meterReadDetailList.'+index1+'.curMonthNum'"
                          :rules="[
                            { required: true, message: '请输入', trigger: 'blur' },
                            { type: 'number', message: '请输入数字', trigger: 'blur' }
                          ]"
                        >
                          <el-input
                            v-model.number="categoryItem.curMonthNum"
                            placeholder="请输入"
                            clearable
                          ></el-input>
                        </el-form-item>
                      </td>
                    </tr>
                  </template>
                </table>
                <div class="status-bg" v-if="isRead">
                  <span class="status-text">已抄表</span>
                </div>
              </el-form>
            </section>
          </el-scrollbar>
          <div slot="footer" class="dialog-footer">
            <!-- <template v-if="isStatement"> -->
              <el-button type="primary" @click="resetMeterReadData('configform')">关 闭</el-button>
            <!-- </template> -->
            <!-- <template v-else>
              <el-button type="primary" @click="resetMeterReadData('configform')" plain>取 消</el-button>
              <el-button type="primary" @click="addMeterReadDataSubmit('configform')" :loading="setLoading">保 存</el-button>
            </template> -->
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
    <dlgRecord :visible="dlg1Visible" :row="dlg1Obj" @dlgdo="dlg1do"/>
    <dlgEditAvg ref="dlgeditavg" :row="dlg2Obj" @refresh="doApi"/>
    <dlgRecordDayModify :visible="dlg3Visible" :row="dlg3Obj" @dlgdo="dlg3do"/>
  </div>
</template>

<script>
import {
  fetchList,
  addRecord,
  addRoomSdData,
  getRoomMeterReadDetail,
  getPerMonthDetail,
  generateSDStatementDetail
} from "@/api/platform/dormitory/sd_meterread";
import { categroyList } from "./_service";
import { tab1OptionConfig, tab2OptionConfig, tab3OptionConfig, tab4OptionConfig } from "@/const/crud/platform/dormitory/sd_meterread";
import dlgRecord from "./components/dlg_record";
import dlgEditAvg from "./components/dlg_edit_avg";
import dlgRecordDayModify from "./components/dlg_record_day_modify";


//抄表状态 0：未抄表（这里不显示未抄表状态）	1：部分抄表 2：已抄完
const isMeterStatus = [
  { label: "部分抄表", value: 1 },
  { label: "已抄完", value: 2 }
];

const isStatementStatus = [
  { label: "未结算", value: 0 },
  { label: "已结算", value: 1 }
];

let meterReadDetailList = [];
for (let i = 1; i < 4; i++) {
  meterReadDetailList.push({
    preMonthNum: "",
    curMonthNum: "",
    categoryId: i
  });
}
export default {
  name: "dorm_mng",
  components: {
    dlgRecord,
    dlgEditAvg,
    dlgRecordDayModify
  },
  data() {
    return {
      dlg1Visible: false,
      dlg1Obj: {},
      dlg3Visible: false,
      dlg3Obj: {},
      dlg2Obj: {},
      activeTab: 'tab1',
      categoryId: undefined,//查询公摊时用到 1热水，2冷水，3电
      tabOption: {
        column: [
          {
            label: "普通水电表",
            prop: "tab1"
          },
          {
            label: "公摊热水表",
            prop: "tab2"
          },
          {
            label: "公摊冷水表",
            prop: "tab3"
          },
          {
            label: "公摊电表",
            prop: "tab4"
          }
        ]
      },
      pageLoading: null,
      mainOption: tab1OptionConfig,
      tab1Option: tab1OptionConfig,
      tab2Option: tab2OptionConfig,
      tab3Option: tab3OptionConfig,
      tab4Option: tab4OptionConfig,
      configform: {
        mrId:"",
        roomId: "",
        meterMonth: "",
        meterReadDetailList: meterReadDetailList
      },
      setLoading: false,
      isRead: true,
      isStatement: false, //是否已生成明细
      addLoading: false, //是否正在添加
      editLoading: false, //是否正在编辑
      addFormVisible: false, //添加楼层
      addMeterReadFormVisible: false, //抄表弹窗
      isMeterStatusArr: isMeterStatus,
      isStatementStatusArr: isStatementStatus,
      addForm: {
        parkId: undefined, //所属园区
        dormitoryId: undefined, //楼栋ID
        floorId: undefined, //楼层ID
        roomId: undefined, //房间号
        meterMonth: undefined //月份
      },
      addRules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        dormitoryId: [
          { required: true, message: "请选择楼栋", trigger: "change" }
        ],
        floorId: [{ required: true, message: "请选择楼层", trigger: "change" }],
        meterMonth: [{ required: true, message: "请选月份", trigger: "change" }]
      },
      searchform: {
        parkId: undefined, //园区ID
        dormitoryId: undefined, //楼栋ID
        floorId: undefined, //楼层ID
        roomName: undefined, //房间号
        meterMonth: undefined, //月份
        status: undefined, //抄表状态
        statementStatus: undefined   //结算状态
      },
      tableLoading: false,
      tableData: [],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    };
  },
  created() {
  },
  mounted: function() {},
  computed: {},
  methods: {
    dlg1do(val){
      this.dlg1Visible = val
    },
    dlg3do(val){
      this.dlg3Visible = val
    },
    // 查看人员修改记录
    getDayRecord(item){
      this.dlg3Obj = item
      this.dlg3Visible = true
    },
    editAvgHandler(item){
      this.dlg2Obj = item
      this.$refs.dlgeditavg && this.$refs.dlgeditavg.open()
    },
    recordHandler(item){
      if(!this.validatenull(item)){
        this.dlg1Obj = item
        this.dlg1Visible = true
      }else{
        this.$message({
          message: '当前没有重置记录！',
          type: 'warning'
        });
      }
    },
    tabChange(item) {
      this.activeTab = item.prop
      this.tableData = []
      if (item.prop == "tab1") { //普通水电表
        this.initTab1();
      } else if(item.prop == "tab2") { //公摊热水表
        this.initTab2();
      } else if(item.prop == "tab3") { //公摊冷水表
        this.initTab3();
      } else if(item.prop == "tab4") { //公摊电表
        this.initTab4();
      }
      this.resetFrom("searchForm");
      // this.doApi()
    },
    initSame(){
      this.page = {
        total: 0,
        currentPage: 1,
        pageSize: 20
      }
    },
    initTab1(){
      this.initSame()
      this.categoryId = undefined
      this.mainOption = this.tab1Option;
    },
    initTab2(){
      this.initSame()
      this.categoryId = 1
      this.mainOption = this.tab2Option;
    },
    initTab3(){
      this.initSame()
      this.categoryId = 2
      this.mainOption = this.tab3Option;
    },
    initTab4(){
      this.initSame()
      this.categoryId = 3
      this.mainOption = this.tab4Option;
    },
    doParkChange(e){
      this.searchform.dormitoryId = ''
      this.searchform.floorId = ''
    },
    doDormChange(e){
      this.searchform.floorId = ''
    },
    doParkChange2(e){
      this.addForm.dormitoryId = ''
      this.addForm.floorId = ''
      this.addForm.roomId = ''
    },
    doDormChange2(e){
      this.addForm.floorId = ''
      this.addForm.roomId = ''
    },
    doFloorChange2(e){
      this.addForm.roomId = ''
    },
    getType(type) {
      const obj = ["", "热水", "冷水", "电"];
      return obj[type];
    },
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
    getCategoryList(page, params) {
      this.tableLoading = true;
      categroyList(Object.assign(
        {
          asc: "id",
          current: page.currentPage,
          size: page.pageSize,
          categoryId: this.categoryId
        },params)).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });

      this.tableLoading = false;
    },
    doApi(){
      if(this.activeTab=='tab1'){
        this.getList(this.page, this.searchform);
      }else{
        this.getCategoryList(this.page, this.searchform);
      }
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.doApi();
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.doApi();
    },
    addSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.addLoading = true;
          addRecord(this.addForm)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.addFormVisible = false;
                this.addLoading = false;
                this.doApi()
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
    handleAddMeterReadData(row, isRead) {
      this.addMeterReadFormVisible = true;
      //是否已生成明细
      if(row.statementStatus !== 0){
          this.isStatement = true;
      }
      if (isRead) {
        //查看
        this.isRead = true;
        getRoomMeterReadDetail(row.id).then(response => {
          this.configform = response.data.data;
          this.configform.meterReadDetailList.forEach(el=>{
            if(el.isRevise==1){
              el.preMonthNum = el.revPreMonthNum
            }
          })
        });
      } else {
        //抄表
        this.isRead = false;
        this.configform = {
          mrId: row.id,
          roomId: row.roomId,
          meterMonth: row.meterMonth,
          meterReadDetailList: meterReadDetailList
        }
        getPerMonthDetail({ roomId: row.roomId, meterMonth: row.meterMonth}).then(response => {
          var resData = response.data.data;
          this.configform.meterReadDetailList = resData.meterReadDetailList
          for (let i = 0; i < 3; i++) {
            this.configform.meterReadDetailList[i].preMonthNum = this.configform.meterReadDetailList[i].curMonthNum
            this.configform.meterReadDetailList[i].curMonthNum = ""
          }
        });
      }
    },
    resetMeterReadData(formName) {
      this.addMeterReadFormVisible = false;
      this.setLoading = false;
      this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    addMeterReadDataSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editLoading = true;
          addRoomSdData(this.configform)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true || dataResult === 1) {
                this.addMeterReadFormVisible = false;
                this.editLoading = false;
                this.doApi()
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
      this.doApi()
    },
    resetFrom(formName) {
      //清空
      this.$refs[formName]&&this.$refs[formName].resetFields();
      this.page.currentPage = 1;
      this.doApi()
    },
    sureGenerateDetail(){
      let _this = this
      const elm = this.$createElement;
       this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("div", null, [
            elm("p", null, "是否生成水电结算明细，数据生成后，将不可修改"),
            elm("p", null, "注意：核算水电明细较为耗时，请耐心等待结果")
          ])
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      }).then(function() {
          _this.pageLoading = _this.$loading({
            lock: true,
            text: "正在执行，请稍后…",
            spinner: "el-icon-loading",
            background: "rgba(0, 0, 0, 0.7)",
            customClass: "bigdata-loading"
          });
          return generateSDStatementDetail();
        })
        .then(data => {
          _this.pageLoading.close();
          this.doApi()
          if (data.data.code == 0) {
            this.$alert(data.data.data, '生成水电明细成功', {
            confirmButtonText: '确定',
            callback: action => {}
          });
          }
        })
        .catch(() => {_this.pageLoading.close();});
    }
  }
};
</script>
<style lang="scss" scoped>
  .top-menu{
    border: none;
  }
  .tabs ::v-deep {
    flex: 1;
    .el-tabs__item{
      height: 50px;
    }
  }
  .tip1{
    color: red;
    text-align: center;
    margin-bottom: 10px;
  }

.config_form ::v-deep {
  .el-form--inline .el-form-item {
    margin: 0;
    width: 100%;
    height: 100%;
  }
  .el-form--inline .el-form-item__content {
    width: 100%;
  }
  .el-input--mini .el-input__inner {
    text-align: center;
    border: none;
  }
  .el-input.is-disabled .el-input__inner {
    background-color: transparent;
    border-color: transparent;
    color: #333;
    cursor: not-allowed;
  }
  .config-inner {
    position: relative;
  }
  .tips{
    color: red;
    display: inline-block;
    margin-left: 50px;
    font-size: 12px;
  }
  .status-bg {
    position: absolute;
    right: 0;
    top: 20px;
    margin-right: 0;
  }
  .ruleTbl {
    width: 100%;
    font-size: 12px;
    margin-bottom: 20px;
    margin-top: 15px;
    td {
      border: 1px solid #e0e0e0;
      padding: 5px 0 12px 0;
      text-align: center;
    }
    td:first-child {
      width: 100px;
    }
    .tdL {
      padding: 0 10px;
    }
    .el-button {
      font-size: 12px;
    }
  }
}
</style>
