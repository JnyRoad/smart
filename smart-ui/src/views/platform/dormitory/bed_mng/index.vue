<!--宿舍管理，床位管理  -->
<template>
  <div class="my-basic-container mycard2 room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <div class="box-outer box-left">
        <el-scrollbar class="my-lit-scrollbar" :native="false">
          <div class="cont-left">
            <span class="tiplbl">房间检索</span>
            <div style="margin: 10px 0">
              <el-input placeholder="输入关键字进行过滤" clearable v-model="filterText" size="mini">
                <el-button type="info" slot="append" icon="el-icon-search"></el-button>
              </el-input>
            </div>
            <!-- :default-expanded-keys="[2, 3]" -->
            <el-tree
              class="filter-tree"
              :data="treedata"
              highlight-current
              node-key="id"
              :default-expanded-keys="defaultExpandedKey"
              :props="defaultProps"
              :filter-node-method="filterNode"
              @node-click="handleNodeClick"
              ref="roomtree"
            ></el-tree>
          </div>
        </el-scrollbar>
      </div>
      <div class="my-basic-inner">
        <div class="box-outer">
          <div class="top-menu clear">
            床位列表
            <div class="top-right">
              <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
              <el-button
                type="primary"
                icon="el-icon-delete"
                @click="resetFrom('searchForm')"
                plain
              >清空</el-button>
              <el-button type="primary" icon="el-icon-upload2" @click="dormInfoImport">入住信息导入</el-button>
              <el-button
                type="primary"
                :loading="exportLoading"
                @click="export2Excel"
                icon="icon-yutong-download"
              >导出表格</el-button>
              <el-button
                type="primary"
                :loading="exportFamilyLoading"
                @click="exportFamily"
                icon="icon-yutong-download"
              >导出家属</el-button>
            </div>
          </div>
          <tce-Search-bar>
            <el-form
              ref="searchForm"
              :inline="true"
              :model="searchForm"
              class="topForm"
              size="mini"
            >
              <el-form-item label="工号" prop="staffBadge">
                <el-input v-model="searchForm.staffBadge" placeholder="工号" clearable></el-input>
              </el-form-item>
              <el-form-item label="员工姓名" prop="name">
                <el-input v-model="searchForm.name" placeholder="员工姓名" clearable></el-input>
              </el-form-item>
              <el-form-item label="BU" prop="compName">
                <el-input v-model="searchForm.compName" placeholder="BU" clearable></el-input>
              </el-form-item>
              <el-form-item label="派遣公司" prop="pqcompany">
                <el-input v-model="searchForm.pqcompany" placeholder="派遣公司" clearable></el-input>
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="searchForm.status" clearable placeholder="状态">
                  <el-option
                    v-for="item in staffStatusData"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="是否空床位" prop="bedEmpty">
                <el-select v-model="searchForm.bedEmpty" clearable placeholder="是否空床位">
                  <el-option label="是" value="1"></el-option>
                  <el-option label="否" value="0"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="入住时间" prop="rangTimeIn">
                <el-date-picker
                  v-model="searchForm.rangTimeIn"
                  type="datetimerange"
                  range-separator="-"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  :default-time="['00:00:00', '23:59:59']"
                  start-placeholder="起始时间"
                  end-placeholder="截止时间"
                  clearable
                ></el-date-picker>
              </el-form-item>
            </el-form>
          </tce-Search-bar>
          <div>
            <el-button type="primary" @click="showLeave()" plain>已离职人员 + {{leaveStaffSize}} ></el-button>
            <el-button type="primary" @click="searchRegister()">先入住未报道</el-button>
            <el-button type="primary" @click="delBatch()" v-if="searchForm.status===-1">批量移除</el-button>
          </div>
          <avue-crud
            ref="crud"
            :page="page"
            :data="tableData"
            @size-change="sizeChange"
            @current-change="currentChange"
            :option="tableOption"
            :row-class-name="tableRowClassName"
            @selection-change="selectChange"
          >
            <template slot-scope="scope" slot="jobName">
              <el-tooltip placement="bottom">
                <div slot="content" style="line-height: 25px">
                  BU：{{scope.row.compName||scope.row.dorCompName}}<br/>
                  中心：{{scope.row.depAbbr||'-'}}<br/>
                  组织单位：{{scope.row.depName||scope.row.dorDepName}}<br/>
                  派遣公司：{{scope.row.pqcompany||'-'}}<br/>
                </div>
                <div style="cursor: pointer">
                  <span class="ft-blue">{{ scope.row.jobName||scope.row.dorJobName }}</span>
                </div>
              </el-tooltip>
            </template>
            <template slot-scope="scope" slot="bedName">
              {{scope.row.bedName}}
              <!-- <span class="ft-blue"  @click="updateBedName(scope.row)" >{{scope.row.bedName}}</span> -->
            </template>
            <template slot-scope="scope" slot="delFlag">
              <!-- 1 红色:已锁定, 0 绿色:未锁定 -->
              <el-switch
                v-model="scope.row.delFlag"
                active-color="#10CC8F"
                inactive-color="#e7292e"
                :active-value="0"
                :inactive-value="1"
                @change="delChange(scope.row)"
                :disabled="!!scope.row.staffBadge">
              </el-switch>
            </template>
            <template slot-scope="scope" slot="staffBadge">
              <!-- 在住 显示工号 -->
              <template v-if="scope.row.staffBadge">
                <span v-if="scope.row.isStaff==1">{{ scope.row.staffBadge }}</span>
                <!-- 非员工入住的，点击工号，可以修改入住信息 -->
                <span
                  v-else
                  class="ft-blue"
                  @click="editCheckInInfo(scope.row, scope.$index)"
                >{{ scope.row.staffBadge }}</span>
              </template>
              <!-- 空床 显示 入住 -->
              <template v-else>
                <el-button
                  :disabled="scope.row.delFlag===1"
                  type="text"
                  icon="el-icon-plus"
                  @click="checkIn(scope.row,scope.$index)"
                >入住</el-button>
              </template>
            </template>
            <template slot-scope="scope" slot="name">
              <el-tooltip placement="bottom">
                <div slot="content" style="line-height: 25px">
                  户籍：{{scope.row.residentaddress || '-'}}<br/>
                  民族：{{scope.row.nation || '-'}}<br/>
                  手机号：{{scope.row.phone || '-'}}
                </div>
                <div style="cursor: pointer">
                  <span class="ft-blue">{{ scope.row.name }}</span>
                </div>
              </el-tooltip>
            </template>
            <template slot-scope="scope" slot="createTime">
              <span class="ft-blue" @click="editTime(scope.row)" >{{dateFormat(scope.row.createTime)}}</span>
            </template>
            <template slot-scope="scope" slot="joinDate">
              <span>{{dateFormat(scope.row.joinDate)}}</span>
            </template>
            <template slot-scope="scope" slot="leaDate">
              <span class="ft-danger">{{dateFormat(scope.row.leaDate)}}</span>
            </template>
            <template slot-scope="scope" slot="remark">
              <span class="ft-blue" v-if="scope.row.remark" @click="remarkList(scope.row,scope.$index)" >{{scope.row.remark}}</span>
              <el-button
                v-else
                type="text"
                icon="el-icon-plus"
                @click="remarkList(scope.row,scope.$index)"
                :disabled="!scope.row.staffBadge"
              >标记</el-button>
            </template>
            <template slot-scope="scope" slot="simpleRemark">
              <span class="ft-blue" v-if="scope.row.simpleRemark" @click="updateRemark(scope.row,scope.$index)" >{{scope.row.simpleRemark}}</span>
            </template>
            <template slot-scope="scope" slot="status">
              <span class="danger" v-if="scope.row.status===0 && scope.row.leaType">{{scope.row.leaType}}</span>
              <span :class="{'danger': scope.row.status===0}" v-else>{{scope.row.status | staffStatusInit}}</span>
            </template>
            <template slot-scope="scope" slot="family">
              <!-- 夫妻/家属 房 可以添加家属 v-if="scope.row.roomSex==2"-->
              <template v-if="scope.row.family && scope.row.family.length>0">
                <span class="ft-blue" @click="checkFamily(scope.row,scope.$index)">{{scope.row.family.toString()}}</span>
              </template>
              <template v-else>
                <el-button
                  :disabled="scope.row.delFlag===1"
                  type="text"
                  icon="el-icon-plus"
                  v-if="scope.row.staffBadge && scope.row.roomSex==2"
                  @click="checkFamily(scope.row,scope.$index)"
                >添加</el-button>
              </template>
            </template>
            <template slot-scope="scope" slot="menu">
              <template v-if="scope.row.staffBadge">
                <el-button
                  type="text"
                  @click="changeDorm(scope.row,scope.$index)"
                >换宿</el-button>
                <el-button
                  type="text"
                  icon="el-icon-delete"
                  @click="checkOut(scope.row,scope.$index)"
                >退宿</el-button>
                <el-button
                  type="text"
                  @click="handlePrintPre(scope.row,scope.$index)"
                >补打凭条</el-button>
              </template>
            </template>
          </avue-crud>
        </div>
      </div>
      <!-- 修改床位编号 -->
      <el-dialog title="修改床位编号" class="dialog_form" width="400px" :visible.sync="bedNameVisible" :close-on-click-modal="false">
        <el-form ref="editBedNameForm" :model="editBedNameForm" :rules="editBedNameRules">
          <el-form-item label="床位编号" prop="bedName">
            <el-input v-model="editBedNameForm.bedName" placeholder="请输入" clearable></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="bedNameVisible = false" plain>取 消</el-button>
          <el-button type="primary" @click="handleEditBedName('editBedNameForm')" :loading="bedNameLoading">确 定</el-button>
        </div>
      </el-dialog>
      <!-- 修改入住时间 -->
      <dlgEditTime :visible="timeVisible" :row="timeForm" @dlgdo="val => timeVisible = val" @refresh="refreshList" />
      <!-- 修改备注 -->
      <dlgEditRemark :visible="simpleRemarkVisible" :row="simpleRemarkForm" @dlgdo="val => simpleRemarkVisible = val" @refresh="refreshList" />
      <!-- 非员工入住,编辑 -->
      <dlgEditCheckIn :visible="editCheckInVisible" :row="editCheckInForm" @dlgdo="val => editCheckInVisible = val" @refresh="refreshList" />
      <!-- 查看家属 -->
      <dlgFamily :row="familyStaff" ref="dlgfamily" @refresh="refreshList"/>
      <dlgDormChange :visible="dlg5Visible" :row="curDlg5Obj" @dlgdo="dlg5do" @dlgdoSuccess="dlg5doSuccess"/>
      <!--入住信息导入 -->
      <el-dialog title="入住信息导入" class="dialog_form" width="600px" :visible.sync="importDialog.visible">
        <div class="import-dialog-inner">
          <componentImport v-if="importDialog.visible" @complete="importComplete" :dormitoryId="params.dormitoryId"></componentImport>
        </div>
      </el-dialog>
      <!-- 入住 -->
      <checkIn ref="checkIn" :dataItem="curCheckIn" @refresh="refreshList"/>
      <!-- 退宿 -->
      <checkOut ref="checkout" :dataItem="curCheckIn" @refresh="refreshList"/>
      <!-- 添加备注 -->
      <remarkList ref="remarklist" :dataItem="curCheckIn" @refresh="refreshList"/>
      <!-- 打印预览 -->
      <dlgNotePre :visible="dlg2Visible" :row="curDlg2Obj" @dlgdo="dlg2do" />
    </el-scrollbar>
  </div>
</template>
<!--引用的room.scss-->
<style lang="scss">
@use "@/styles/platform/dormitory/room" as *;
</style>

<script>
import {
  fetchList,
  allList,
  getLeaveCount,
  updateBedName,
  isLock,
  delBatch,
  exportFamily
} from "@/api/platform/dormitory/bed_mng";
import { tableOption } from "@/const/crud/platform/dormitory/bed_mng";
import { enumStaffStatus } from "@/const/crud/platform/enum";
import { staffStatusInit } from '@/filters/index'
import { registerRowClassName, formatCheckInDate, toExportRows } from './bed-rules'
import dlgFamily from "../new_room/compnents/dlg_family";
import dlgDormChange from "../ks_checkIn/compnents/dlg_dorm_change";
import componentImport from './import/index'
import checkIn from './components/_check_in'
import checkOut from './components/_check_out'
import remarkList from './components/_remark_list'
import dlgNotePre from "./components/dlg_note_pre"
import dlgEditTime from "./components/dlg_edit_time"
import dlgEditRemark from "./components/dlg_edit_remark"
import dlgEditCheckIn from "./components/dlg_edit_check_in"

export default {
  name: "bed_mng",
  components:{
    dlgFamily,
    dlgDormChange,
    componentImport,
    checkIn,
    checkOut,
    remarkList,
    dlgNotePre,
    dlgEditTime,
    dlgEditRemark,
    dlgEditCheckIn
  },
  data() {
    return {
      curCheckIn: {}, //当前要入住的床位对象
      importDialog: {
        visible: false
      },
      dlg5Visible: false, //更换宿舍
      curDlg5Obj: {},
      familyStaff: undefined,
      editCheckInVisible: false,
      checkInLoading: false,
      exportLoading: false,
      exportFamilyLoading: false,
      bedNameVisible: false,
      bedNameLoading: false,
      staffStatusData: enumStaffStatus,
      editCheckInForm: {
        id: "",
        bedId: "",
        staffBadge: "",
        staffName: "",
        jobName: "",
        staffSex: undefined
      },
      editBedNameForm: {
        bedId: "",
        bedName: ""
      },
      editBedNameRules: {
        bedName: [{ required: true, message: "请输入床位编号", trigger: "blur" }]
      },
      searchForm: {
        //搜索菜单表单
        compName: undefined,
        staffBadge: undefined,
        pqcompany: undefined,
        name: undefined,
        status: undefined,
        bedEmpty: undefined
      },
      simpleRemarkVisible: false,
      simpleRemarkForm: {
        id: "",
        simpleRemark: ""
      },
      timeVisible: false,
      timeForm: {
        id: "",
        createTime: ""
      },
      obj: {},
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      rangIDTemp: [],
      tableOption: tableOption,
      tableData: [],
      filterText: "",
      treedata: [],
      leaveStaffSize: 0,
      params: null,
      defaultProps: {
        children: "children",
        label: "label",
        value: "value"
      },
      defaultExpandedKey: [],
      selectInfos: [], //选中的集合
      dlg2Visible: false, //凭条预览
      curDlg2Obj: {},
    };
  },
  created() {
    this.$nextTick(() => {
      if (this.$route.query.queryForm != undefined) {
        let params = this.$route.query.params;
        let queryPage = this.$route.query.queryPage;
        let queryForm = this.$route.query.queryForm;
        if (params && params.constructor === Object) {
          this.params = Object.assign(params, {});
        }
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {});
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {});
        }
        this.defaultExpandedKey = this.$route.query.queryDefaultExpandedKey
          ? this.$route.query.queryDefaultExpandedKey
          : [];
        this.getTreeQuery();
      } else {
        this.getTree();
      }
    });
  },
  mounted: function() {},
  computed: {},
  watch: {
    filterText(val) {
      this.$refs.roomtree.filter(val);
    }
  },
  methods: {
    //凭条预览
    handlePrintPre(row){
      this.dlg2Visible = true
      this.curDlg2Obj = row
    },
    dlg2do(val){
      this.dlg2Visible = val
    },
    updateRemark(row){
      // 先填行数据再开弹窗，保证子组件 watch(visible) 读到的是最新 row
      this.simpleRemarkForm = { id: row.id, simpleRemark: row.simpleRemark }
      this.simpleRemarkVisible = true
    },
    //添加备注
    remarkList(row){
      this.curCheckIn = row
      this.$refs.remarklist && this.$refs.remarklist.open()
    },
    //批量移除，先入住未报道
    async delBatch(){
      if(this.selectInfos && this.selectInfos.length>0){
        const res = await delBatch({ids: this.selectInfos})
        if(res.data.code===0){
          this.$message({
            message: '移除成功',
            type: 'success'
          });
          this.refreshList()
        }
      }else{
        this.$message.error('请选中要移除的信息');
      }
    },
    selectChange(val) {
      //序号那边选择事件
      this.selectInfos = []
      var idArr = [];
      if (val.length > 0) {
        val.forEach(function(element) {
          idArr.push(element.id);
        }, this);
        this.selectInfos = idArr
      }
    },
    tableRowClassName({row, index}){
      return registerRowClassName(row)
    },
    searchRegister(){
      this.searchForm.status = -1
      this.searchSubmit(this.searchForm)
    },
    dormInfoImport(){
      if(this.params && this.params.dormitoryId){
        this.importDialog.visible = true
      }else{
        this.$message.error(`请先在左侧选中要导入的楼栋`)
        return
      }
    },
    refreshList(){
      this.getList(this.page, this.params);
    },
    importComplete(){
      this.importDialog.visible = false
      this.getList(this.page, this.params)
    },
    async delChange(row){
      let obj = {
        bedId: row.bedId,
        bedName: row.bedName,
        delFlag: row.delFlag
      }
      const res = await isLock(obj)
      if(res.data.code===0){
        this.$notify({
          title: '成功',
          message: '修改成功',
          type: 'success'
        });
        this.getList(this.page, this.params);
      }else{
        this.$notify.error({
          title: '失败',
          message: res.data.msg
        });
      }
    },
    dlg5do(val){
      this.dlg5Visible = val
    },
    dlg5doSuccess(val){
      // this.dlg5Visible = false
      this.getList(this.page, this.params);
      // this.curDlg5Obj = null
    },
    /**
     * 更换宿舍
     */
    changeDorm(val){
      this.dlg5Visible = true
      this.curDlg5Obj = val
    },
    checkFamily(row){
      this.familyStaff = row
      this.$refs.dlgfamily && this.$refs.dlgfamily.open()
    },
    dateFormat(val) {
      return formatCheckInDate(val)
    },
    editTime(row) {
      this.timeVisible = true;
      this.timeForm = { id: row.id, createTime: row.createTime };
    },
    updateBedName(row){
      this.bedNameVisible = true
      this.editBedNameForm = { bedId: row.bedId, bedName: row.bedName };
    },
    handleEditBedName(formName){
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.bedNameLoading = true;
          updateBedName(this.editBedNameForm)
            .then(response => {
              this.bedNameVisible = false;
              this.bedNameLoading = false;
              this.$notify({
                title: '成功',
                message: '修改床位编号成功',
                type: 'success'
              });
              this.getList(this.page, this.params);
            })
            .catch(err => {
              this.bedNameLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1;
      this.getList(this.page, this.params);
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
        this.page.currentPage = 1;
        this.getList(this.page, this.params);
      }
    },
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "序号",
          "工号",
          "姓名",
          "员工状态",
          "BU",
          "部门",
          "职务",
          "原因",
          "入住日期",
          "入职日期",
          "离职日期",
          "楼栋",
          "房间",
          "手机号",
          "身份证号",
          "民族",
          "身份证地址",
          "派遣公司",
          "备注",
        ];
        const filterVal = [
          "bedNumber",
          "staffBadge",
          "name",
          "status",
          "compName",
          "depName",
          "jobName",
          "reason",
          "createTime",
          "joinDate",
          "leaDate",
          "dormitoryName",
          "roomName",
          "phone",
          "certno",
          "nation",
          "residentaddress",
          "pqcompany",
          "simpleRemark"
        ];
        let startTime = undefined
        let endTime = undefined
        if (this.searchForm.rangTimeIn && this.searchForm.rangTimeIn.length>0) {
          startTime = this.searchForm.rangTimeIn[0];
          endTime = this.searchForm.rangTimeIn[1];
        }
        fetchList(Object.assign({
          current: 1,
          size: this.page.total,
          staffBadge: this.searchForm.staffBadge,
          name: this.searchForm.name,
          status: this.searchForm.status,
          bedEmpty: this.searchForm.bedEmpty,
          compName: this.searchForm.compName,
          startTime: startTime,
          endTime: endTime
        }, this.params))
          .then(response => {
            const list = response.data.data.records;
            list.forEach(function(item) {
              if (item.depName == null || item.depName == "") {
                item.depName = item.dorDepName;
              }
              if (item.jobName == null || item.jobName == "") {
                item.jobName = item.dorJobName;
              }
              item.reason = ''
              item.status = staffStatusInit(item.status)
            });
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, `床位管理&(${this.searchForm.rangTimeIn})`);
            this.exportLoading = false;
          })
          .catch(err => {
            this.exportLoading = false;
          });
      });
    },
    //导出家属
    exportFamily() {
      require.ensure([], () => {
        this.exportFamilyLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "楼栋",
          "房间",
          "工号",
          "姓名",
          "家属姓名",
          "家属工号",
          "家属关系",
          "家属手机号",
          "家属身份证号"
        ];
        const filterVal = [
          "dormitoryName",
          "roomName",
          "staffBadge",
          "name",
          "familyName",
          "familyBadge",
          "familyRelationDesc",
          "familyPhone",
          "familyCertno",
        ];
        let startTime = undefined
        let endTime = undefined
        if (this.searchForm.rangTimeIn && this.searchForm.rangTimeIn.length>0) {
          startTime = this.searchForm.rangTimeIn[0];
          endTime = this.searchForm.rangTimeIn[1];
        }
        exportFamily(Object.assign({
          staffBadge: this.searchForm.staffBadge,
          name: this.searchForm.name,
          status: this.searchForm.status,
          bedEmpty: this.searchForm.bedEmpty,
          compName: this.searchForm.compName,
          startTime: startTime,
          endTime: endTime
        }, this.params))
          .then(response => {
            const list = response.data.data
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, "家属列表");
            this.exportFamilyLoading = false;
          })
          .catch(err => {
            this.exportFamilyLoading = false;
          });
      });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return toExportRows(filterVal, jsonData)
    },
    getTree: function() {
      var page = this.page;
      var _this = this;
      allList().then(response => {
        this.treedata = response.data.data;
        //查询第一节点的内宿员工
        this.params = { parkId: _this.treedata[0].id };
        this.getList(page, _this.params);
      });
    },
    getTreeQuery: function() {
      var page = this.page;
      var _this = this;
      allList().then(response => {
        this.treedata = response.data.data;
        this.$nextTick(() => {
          if (this.defaultExpandedKey.length > 0) {
            this.$refs.roomtree.setCurrentKey(this.defaultExpandedKey[0]);
          }
        });
        //查询第一节点的内宿员工
        this.getList(page, _this.params);
      });
    },
    handleNodeClick(data, node) {
      this.defaultExpandedKey = [data.id];
      var level = node.level;
      var params = null;
      this.page.currentPage = 1;
      if (level == 1) {
        params = { parkId: data.id };
      } else if (level == 2) {
        params = { dormitoryId: data.id };
      } else if (level == 3) {
        params = { floorId: data.id };
      } else if (level == 4) {
        params = { roomId: data.id };
      }
      this.params = params;
      this.getList(this.page, this.params);
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    getList(page, params) {
      this.tableLoading = true;
      var _this = this;
      let startTime = undefined
      let endTime = undefined
      if (this.searchForm.rangTimeIn && this.searchForm.rangTimeIn.length>0) {
        startTime = this.searchForm.rangTimeIn[0];
        endTime = this.searchForm.rangTimeIn[1];
      }
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize,
            staffBadge: this.searchForm.staffBadge,
            name: this.searchForm.name,
            status: this.searchForm.status,
            bedEmpty: this.searchForm.bedEmpty,
            compName: this.searchForm.compName,
            roomType: this.searchForm.roomType,
            pqcompany: this.searchForm.pqcompany,
            startTime: startTime,
            endTime: endTime
          },
          params
        )
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;

        getLeaveCount(params).then(responseData => {
          _this.leaveStaffSize = responseData.data.data;
        });
      });
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page, this.params);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page, this.params);
    },
    //退宿
    checkOut(row, index) {
      this.curCheckIn = row
      this.$refs.checkout && this.$refs.checkout.open()
    },
    //入住
    checkIn(row) {
      this.curCheckIn = row
      this.$refs.checkIn && this.$refs.checkIn.open()
    },
    editCheckInInfo(row, index) {
      // 先填映射好的行数据再开弹窗，保证子组件 watch(visible) 读到的是最新 row
      this.editCheckInForm = {
        id: row.id,
        bedId: row.bedId,
        staffBadge: row.staffBadge,
        staffName: row.name,
        jobName: row.dorJobName,
        staffSex: row.sex
      };
      this.editCheckInVisible = true;
    },
    //点击离职人数
    showLeave() {
      this.searchForm = {
        staffBadge: undefined,
        name: undefined,
        status: "0"
      };
      this.searchSubmit(this.searchForm)
    }
  }
};
</script>

<style lang="scss" scoped>
.mycard2 ::v-deep {
  .isRegister{
    background: yellow;
  }
  .el-table--striped .el-table__body tr.el-table__row--striped.isRegister td{
    background: yellow;
  }
  .el-table--striped .el-table__body tr.el-table__row--striped.isRegister.hover-row td{
    background: #ecf5ff;
  }
}
.danger{
  font-weight: bold;
  color: red;
}
.mycard2{
  .box-left{
    width: 225px;
    padding: 20px 10px;
  }
  .my-scrollbar{
    padding: 0 0 0 230px;
  }
}
.btns{
  text-align: center;
  padding: 15px 0;
}
.topForm ::v-deep {
  .el-form-item__label {
    width: 110px;
  }
}
.bed_mng_staff {
  ::v-deep .el-radio.is-bordered {
    padding: 0;
    width: 90px;
    height: 40px;
    line-height: 38px;
    margin: 10px 13px 0 0;
    text-align: center;
    .el-radio__input {
      display: none;
    }
    .el-radio__label {
      padding: 0;
    }
  }
}
</style>
