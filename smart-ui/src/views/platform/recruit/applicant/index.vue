<!--招聘管理，应聘管理  -->
<template>
  <div class="my-basic-container applicant">
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
            <el-button type="primary" @click="exportExcel">导出</el-button>
            <el-button
              type="primary"
              v-if="!isMultiple && applicant_status!=5"
              @click="isMultiple = true"
            >批量选择</el-button>
            <template v-if="isMultiple">
              <el-button
                type="primary"
                v-if="applicant_status==0 ||applicant_status==1  || applicant_status==6"
                @click="interviewClick()"
              >面试邀请</el-button>
              <el-button type="primary" v-if="applicant_status==2  " @click="addtotalents()">加入人才库</el-button>
              <el-button type="primary" v-if="applicant_status==2 " @click="reexamineClick()">复试</el-button>
              <el-button
                type="primary"
                v-if="applicant_status==2 ||applicant_status==4  "
                @click="inviteEntryClick()"
              >录取</el-button>
              <el-button type="primary" v-if="applicant_status==3 " @click="handleEntry">入职</el-button>
              <!-- 只有以下四种状态有拒绝按钮：已投递，已邀请，待复试，待入职 -->
              <el-button
                v-if="applicant_status==0 || applicant_status==2 || applicant_status==3|| applicant_status==4"
                type="primary"
                @click="openRefuse"
              >拒绝</el-button>
              <el-button type="primary" @click="isMultiple = false" plain>取消</el-button>
            </template>
          </div>
          <!-- 0-已投递1-已拒绝 2-已邀请3-待入职/4待复试，暂定/5-已入职/6-已入库 -->
          <el-radio-group v-model="applicant_status" @change="getApplicantStatus">
            <el-radio-button :label="0">已投递</el-radio-button>
            <el-radio-button :label="2">已邀请</el-radio-button>
            <el-radio-button :label="4">待复试</el-radio-button>
            <el-radio-button :label="3">待入职</el-radio-button>
            <el-radio-button :label="5">已入职</el-radio-button>
            <el-radio-button :label="1">已拒绝</el-radio-button>
            <el-radio-button :label="6">已入库</el-radio-button>
          </el-radio-group>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
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
          <el-form-item label="姓名" prop="name">
            <el-input v-model="searchForm.name" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="年龄" prop="ageRang">
            <el-select v-model="searchForm.ageRang" placeholder="年龄" clearable>
              <el-option label="18岁-25岁" value="18-25">18岁-25岁</el-option>
              <el-option label="26岁-40岁" value="26-40">26岁-40岁</el-option>
              <el-option label="40岁以上" value="40-">40岁以上</el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="searchForm.phone" placeholder="手机号" clearable></el-input>
          </el-form-item>
          <el-form-item label="岗位" prop="jobId">
            <el-input v-model="searchForm.jobName" placeholder="岗位" clearable></el-input>
          </el-form-item>
          <el-form-item label="投递时间" prop="rangTime">
            <el-date-picker
              v-model="searchForm.rangTime"
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
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
          @selection-change="selectionChange"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleDetail(scope.row,scope.$index)"
            >应聘详情</el-button>
            <el-button type="text" icon="icon-yutong-record" @click="handleProcess(scope.row)">应聘记录</el-button>
            <el-button
              type="text"
              v-if="applicant_status==5"
              icon="icon-yutong-record"
              @click="addStaff(scope.row)"
            >重新同步</el-button>
            <!-- <el-button type="text"
                      icon="icon-yutong-preview"
                      @click="box_resume = true">预览简历
            </el-button>-->
          </template>
        </avue-crud>
        <!-- 应聘记录 -->
        <el-dialog
          title="应聘记录"
          class="dialog_form record_form"
          width="500px"
          :visible.sync="recordVisble"
        >
          <div class="step-outer">
            <el-steps direction="vertical" :active="appicactionStatus">
              <el-step
                v-for="process in processArray"
                :title="process.status|applyStatusFormat"
                :key="process.id"
                :description="process.createTime"
              ></el-step>
            </el-steps>
          </div>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="recordVisble = false">知道了</el-button>
          </div>
        </el-dialog>

        <!-- <el-dialog title=""
                fullscreen
                :visible.sync="box_resume"
                width="100%"
                custom-class="dialog_resume"
                append-to-body>
          <resume></resume>
        </el-dialog>-->

        <!-- 面试邀请 -->
        <el-dialog
          title="邀请面试"
          @close="resetInterviewForm('interviewForm')"
          class="dialog_form"
          width="400px"
          :visible.sync="interviewFormVisible"
        >
          <el-form ref="interviewForm" :model="interviewForm" :rules="interviewRules">
            <el-form-item label="请选择面试时间" prop="interviewTime">
              <el-date-picker
                v-model="interviewForm.interviewTime"
                type="datetime"
                format="yyyy-MM-dd HH:mm"
                value-format="yyyy-MM-dd HH:mm:ss"
                :default-value="defaultValue"
                :picker-options="pickerOptions"
                clearable
              ></el-date-picker>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="resetInterviewForm('interviewForm')" plain>取 消</el-button>
            <el-button
              type="primary"
              @click="handleInterview('interviewForm')"
              :loading="interviewLoading"
            >确 定</el-button>
          </div>
        </el-dialog>

        <!-- 邀请复试 -->
        <el-dialog
          title="邀请复试"
          @close="resetReexamineForm('reexamineForm')"
          class="dialog_form"
          width="400px"
          :visible.sync="reexamineFormVisible"
        >
          <el-form ref="reexamineForm" :model="reexamineForm" :rules="reexamineRules">
            <el-form-item label="请选择复试时间" prop="reexamineTime">
              <el-date-picker
                v-model="reexamineForm.reexamineTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                format="yyyy-MM-dd HH:mm"
                :default-value="defaultValue"
                :picker-options="pickerOptions"
                clearable
              ></el-date-picker>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="resetReexamineForm('reexamineForm')" plain>取 消</el-button>
            <el-button
              type="primary"
              @click="handleReexamine('reexamineForm')"
              :loading="reexamineLoading"
            >确 定</el-button>
          </div>
        </el-dialog>

        <!-- 邀请入职 -->
        <el-dialog
          title="邀请入职"
          @close="resetInviteEntryForm('inviteEntryForm')"
          class="dialog_form"
          width="400px"
          :visible.sync="inviteEntryFormVisible"
        >
          <el-form ref="inviteEntryForm" :model="inviteEntryForm" :rules="inviteEntryRules">
            <el-form-item label="请选择入职时间" prop="inviteEntryTime">
              <el-date-picker
                v-model="inviteEntryForm.inviteEntryTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                format="yyyy-MM-dd HH:mm"
                :default-value="defaultValue"
                :picker-options="pickerOptions"
                clearable
              ></el-date-picker>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="resetInviteEntryForm('inviteEntryForm')" plain>取 消</el-button>
            <el-button
              type="primary"
              @click="handleInviteEntry('inviteEntryForm')"
              :loading="inviteEntryLoading"
            >确 定</el-button>
          </div>
        </el-dialog>

        <!-- 拒绝原因 -->
        <el-dialog
          title="拒绝原因"
          class="refuseDialog"
          width="750px"
          :visible.sync="refuseFormVisible"
        >
          <el-form :model="form_refuse">
            <p class="box-orange">应聘者原因</p>
            <el-checkbox-group v-model="form_refuse.applicantGroup" size="small">
              <el-checkbox
                v-for="reason in computedReason"
                :label="reason"
                :key="reason"
                border
              >{{reason}}</el-checkbox>
            </el-checkbox-group>
            <p class="box-orange">公司原因</p>
            <el-checkbox-group v-model="form_refuse.firmGrop" size="small">
              <el-checkbox
                v-for="reason in firmReason"
                :label="reason"
                :key="reason"
                border
              >{{reason}}</el-checkbox>
            </el-checkbox-group>
            <p class="box-orange">其他原因</p>
            <el-input class="textarea" type="textarea" rows="5" v-model="form_refuse.other"></el-input>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="refuseFormVisible = false" plain>取 消</el-button>
            <el-button type="primary" @click="handleRefuse" :loading="refuseLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/recruit/index" as *;
</style>

<script>
import {
  fetchList,
  getProcess,
  putObj,
  putObjToStaff
} from "@/api/platform/recruit/applicant";
import { tableOption } from "@/const/crud/platform/recruit/applicant";
import { getCompTree } from "@/api/platform/_publicService";
import { mapGetters } from "vuex";
import { excel } from "@/util/excel";
import { isArrayFn } from "@/util/util";
import { dateFormat } from "@/util/date";

import resume from "@/views/platform/recruit/applicant/preview_resume";

/**拒绝原因，根据阶段不同会发生变化
 * yyqOptions ：已邀请阶段拒绝
 * dfsOptions : 待复试阶段拒绝
 * drzOptions : 待入职阶段拒绝
 * firmOptions : 公司原因
 */
const yyqOptions = [
  "笔试成绩不合格",
  "视力不合格",
  "存在色盲色弱",
  "手脚灵活性不合格",
  "不符合岗位要求",
  "不愿前来"
];
const dfsOptions = ["能力不匹配", "与岗位不符合", "面试未通过"];
const drzOptions = ["已经找到工作", "不想来了", "个人原因", "未按时报道"];
const firmOptions = ["岗位已满", "暂停不招人"];

export default {
  name: "recruit",
  components: { resume },
  data() {
    var validatDateTime = (rule, value, callback) => {
      var tm = new Date();
      var selectTime = new Date(value.replace(/-/g, "/"));
      var _time = selectTime - tm;
      if (_time < 0) {
        callback(new Error("选择的时间不能早于当前时间"));
      } else {
        callback();
      }
    };
    return {
      searchForm: {
        //搜索菜单表单
        parkId: undefined,
        compId: undefined,
        depId: undefined,
        name: undefined,
        ageRang: undefined,
        phone: undefined,
        jobId: undefined,
        rangTime: undefined
      },
      depIds: [],
      compOptions: [],
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7; //8.64e7=1000*60*60*24一天
        }
      },
      interviewLoading: false, //邀请面试loading
      reexamineLoading: false, //邀请复试loading
      inviteEntryLoading: false, //邀请入职loading
      refuseLoading: false, //拒绝原因loading
      interviewFormVisible: false, //邀请面试
      reexamineFormVisible: false, //邀请复试
      inviteEntryFormVisible: false, //邀请入职
      entryFormVisible: false, //入职
      refuseFormVisible: false, //拒绝原因
      firmReason: firmOptions,
      interviewForm: {
        interviewTime: "" //面试时间
      },
      reexamineForm: {
        reexamineTime: "" //复试时间
      },
      inviteEntryForm: {
        inviteEntryTime: "" //邀请入职时间
      },
      interviewRules: {
        interviewTime: [
          { required: true, message: "请选择面试时间", trigger: "blur" },
          { validator: validatDateTime, trigger: "blur" }
        ]
      },
      reexamineRules: {
        reexamineTime: [
          { required: true, message: "请选择复试时间", trigger: "blur" },
          { validator: validatDateTime, trigger: "blur" }
        ]
      },
      inviteEntryRules: {
        inviteEntryTime: [
          { required: true, message: "请选择入职时间", trigger: "blur" },
          { validator: validatDateTime, trigger: "blur" }
        ]
      },
      form_refuse: {
        applicantGroup: [],
        firmGrop: [],
        other: ""
      },
      recordVisble: false,
      box_resume: false,
      applicant_status: 0,
      isMultiple: false,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      processArray: [],
      appicactionStatus: 5,
      editForm: {
        ids: [],
        status: 0,
        createUserNme: "",
        refuseReason: ""
      }
    };
  },
  watch: {
    depIds(newVal, oldVal) {
      if (isArrayFn(newVal) && newVal.length > 0) {
        const depLength = newVal.length;
        if (depLength == 3) {
          this.searchForm.depId = this.depIds[2];
          this.searchForm.compId = this.depIds[1];
          this.searchForm.parkId = this.depIds[0];
        } else if (depLength >= 2) {
          this.searchForm.depId = undefined;
          this.searchForm.compId = this.depIds[1];
          this.searchForm.parkId = this.depIds[0];
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
  created: function() {
    getCompTree().then(response => {
      this.compOptions = response.data.data;
    });
    this.$nextTick(() => {
      if (this.$route.query.queryForm != undefined) {
        let queryPage = this.$route.query.queryPage;
        let queryForm = this.$route.query.queryForm;
        let queryApplicantStatus = this.$route.query.queryApplicantStatus;
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {});
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {});
        }
        this.applicant_status = queryApplicantStatus;
        this.getList(this.page, this.searchForm);
      } else {
        this.getList(this.page);
      }
    });
    // this.getList(this.page);
    this.editForm.createUserNme = this.userInfo.username;
  },
  mounted: function() {
    this.interviewForm.interviewTime = dateFormat(this.defaultValue);
    this.reexamineForm.reexamineTime = dateFormat(this.defaultValue);
    this.inviteEntryForm.inviteEntryTime = dateFormat(this.defaultValue);
  },
  computed: {
    ...mapGetters(["userInfo", "permissions"]),
    computedReason: function() {
      let status = this.applicant_status;
      if (status == 2) {
        return yyqOptions;
      } else if (status == 4) {
        return dfsOptions;
      } else if (status == 3) {
        return drzOptions;
      }
      return [];
    },
    defaultValue: function() {
      //邀请时间默认显示为当天的上午十点整（10:00）
      let tm = new Date();
      tm.setHours(10);
      tm.setMinutes(0);
      tm.setSeconds(0);
      return tm;
    }
  },
  methods: {
    getList(page, params) {
      this.tableLoading = true;
      params = Object.assign(
        {
          current: page.currentPage,
          size: page.pageSize,
          status: this.applicant_status
        },
        params
      );

      if (isArrayFn(params.rangTime)) {
        params.rangTime = params.rangTime.join();
      }
      if (params.ageRang) {
        params.ageRang = params.ageRang.split("-").join();
      }

      fetchList(params).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
      this.tableLoading = false;
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
      const src = `/platform/recruit/applicant/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm,
          queryApplicantStatus: this.applicant_status
        }
      });
    },
    handleProcess(row) {
      this.recordVisble = true;
      var id = row.id;
      //获取当前的状态
      this.processArray = [];
      getProcess(id).then(response => {
        var arr = response.data.data;
        this.appicactionStatus = arr.length - 1;
        arr.forEach(function(element) {
          if (element.createUserName != null && element.createUserName != "")
            element.createTime =
              element.createTime + " 操作人：" + element.createUserName;
        }, this);
        this.processArray = arr;
      });
    },
    updateProcess() {
      var _this = this;
      putObj(_this.editForm).then(response => {
        var data = response.data.data;
        if (data == true) {
          _this.getList(_this.page, _this.searchForm);
          _this.$notify({
            title: "成功",
            message: "加入人才库成功",
            type: "success"
          });
        } else {
          _this.$notify({
            title: "失败",
            message: "加入人才库失败",
            type: "error"
          });
        }
      });
    },
    selectionChange(list) {
      var idArr = [];
      if (list.length > 0) {
        list.forEach(function(element) {
          idArr.push(element.id);
        }, this);
      }
      this.editForm.ids = idArr;
    },
    handleResume(row, index) {
      const src = `/platform/appointment/index`;

      window.open(src, "_blank");
    },
    //已投递、已入库等状态按钮点击事件
    getApplicantStatus(val) {
      //this.resetFrom('searchForm');
      this.page.currentPage = 1;
      this.applicant_status = val;
      this.getList(this.page, this.searchForm);
    },
    //邀请面试弹框
    interviewClick() {
      if (this.editForm.ids.length == 0) {
        this.$notify({
          title: "失败",
          message: "请选选择员工",
          type: "error"
        });
        return;
      }
      this.interviewFormVisible = true;
    },
    //邀请面试，确定
    handleInterview(formName) {
      var _this = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editForm.status = 2;
          this.editForm.interviewTime = this.interviewForm.interviewTime;
          this.interviewLoading = true;
          putObj(this.editForm)
            .then(response => {
              var data = response.data.data;
              if (data == true) {
                _this.getList(_this.page, _this.searchForm);
                _this.interviewFormVisible = false;
                _this.$notify({
                  title: "成功",
                  message: "面试邀请成功",
                  type: "success"
                });
              } else {
                _this.$notify({
                  title: "失败",
                  message: "面试邀请失败",
                  type: "error"
                });
              }
              _this.interviewLoading = false;
            })
            .catch(err => {
              _this.interviewLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    //复试弹框
    reexamineClick() {
      if (this.editForm.ids.length == 0) {
        this.$notify({
          title: "失败",
          message: "请选选择员工",
          type: "error"
        });
        return;
      }
      this.reexamineFormVisible = true;
    },
    //邀请复试，确定
    handleReexamine(formName) {
      var _this = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editForm.status = 4;
          this.editForm.interviewTime = this.reexamineForm.reexamineTime;
          this.reexamineLoading = true;
          putObj(this.editForm)
            .then(response => {
              var data = response.data.data;
              if (data == true) {
                _this.getList(_this.page, _this.searchForm);
                _this.reexamineFormVisible = false;
                _this.$notify({
                  title: "成功",
                  message: "复试邀请成功",
                  type: "success"
                });
              } else {
                _this.$notify({
                  title: "失败",
                  message: "复试邀请失败",
                  type: "error"
                });
              }
              _this.reexamineLoading = false;
            })
            .catch(err => {
              _this.reexamineLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    //邀请入职点击
    inviteEntryClick() {
      if (this.editForm.ids.length == 0) {
        this.$notify({
          title: "失败",
          message: "请选选择员工",
          type: "error"
        });
        return;
      }
      this.inviteEntryFormVisible = true;
    },
    addStaff(row) {
      this.editForm.ids = [];
      var _this = this;
      this.editForm.ids.push(row.id);
      this.editForm.createUserNme = this.userInfo.username;
      this.editForm.status = 5;
      putObjToStaff(_this.editForm).then(response => {
        var data = response.data.data;
        if (data == true) {
          _this.getList(_this.page, _this.searchForm);
          _this.inviteEntryFormVisible = false;
          _this.$notify({
            title: "成功",
            message: "重新同步成功",
            type: "success"
          });
        } else {
          _this.$notify({
            title: "失败",
            message: "重新同步失败",
            type: "error"
          });
        }
      });
    },
    //邀请入职，确定
    handleInviteEntry(formName) {
      var _this = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editForm.status = 3;
          this.editForm.interviewTime = this.inviteEntryForm.inviteEntryTime;
          this.inviteEntryLoading = true;
          putObj(this.editForm)
            .then(response => {
              var data = response.data.data;
              if (data == true) {
                _this.getList(_this.page, _this.searchForm);
                _this.inviteEntryFormVisible = false;
                _this.$notify({
                  title: "成功",
                  message: "邀请入职成功",
                  type: "success"
                });
              } else {
                _this.$notify({
                  title: "失败",
                  message: "邀请入职失败",
                  type: "error"
                });
              }
              _this.inviteEntryLoading = false;
            })
            .catch(err => {
              _this.inviteEntryLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    //入职，确定
    handleEntry() {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        title: "",
        message: elm("div", { attrs: { class: "imgDialogInner" } }, [
          elm("span", { attrs: { class: "imgInfo" } }, ""),
          elm("span", null, "是否确定入职")
        ]),
        confirmButtonText: "确定",
        customClass: "img_dialog",
        center: true
      })
        .then(function() {
          _this.editForm.status = 5;
          putObj(_this.editForm).then(response => {
            var data = response.data.data;
            if (data == true) {
              _this.getList(_this.page, _this.searchForm);
              _this.inviteEntryFormVisible = false;
              _this.$notify({
                title: "成功",
                message: "入职成功",
                type: "success"
              });
            } else {
              _this.$notify({
                title: "失败",
                message: "入职失败",
                type: "error"
              });
            }
          });
        })
        .then(data => {})
        .catch(err => { console.error(err) });
    },

    //拒绝原因
    openRefuse() {
      var _this = this;
      if (this.editForm.ids.length == 0) {
        if (this.editForm.ids.length == 0) {
          this.$notify({
            title: "失败",
            message: "请选选择员工",
            type: "error"
          });
          return;
        }
        return;
      }
      if (this.applicant_status == 0) {
        this.editForm.status = 1;
        putObj(this.editForm).then(response => {
          var data = response.data.data;
          if (data == true) {
            _this.getList(_this.page, _this.searchForm);
            _this.refuseFormVisible = false;
            _this.$notify({
              title: "成功",
              message: "拒绝成功",
              type: "success"
            });
          } else {
            _this.$notify({
              title: "失败",
              message: "面拒绝失败",
              type: "error"
            });
          }
        });
      } else {
        this.refuseFormVisible = true;
      }
    },
    //拒绝原因，确定
    handleRefuse() {
      this.editForm.status = 1;
      if (
        this.form_refuse.applicantGroup.length == 0 &&
        this.form_refuse.firmGrop.length == 0 &&
        this.form_refuse.other.length == 0
      ) {
        this.$notify({
          title: "失败",
          message: "请选择拒绝原因",
          type: "error"
        });
        return;
      }
      var reson = "";
      var group = this.form_refuse.applicantGroup.join();
      reson = group;
      var firmGrop = this.form_refuse.firmGrop.join();
      if (reson.length > 0) {
        if (firmGrop.length > 0) {
          reson += ",";
        }
      }
      reson += firmGrop;
      if (reson.length > 0) {
        if (this.form_refuse.other.length > 0) reson += ",";
      }
      reson += this.form_refuse.other;

      this.editForm.refuseReason = reson;
      this.editForm.refuseReason = reson;

      var _this = this;
      _this.refuseLoading = true;
      putObj(this.editForm).then(response => {
        var data = response.data.data;
        if (data == true) {
          _this.getList(_this.page, _this.searchForm);
          _this.refuseFormVisible = false;
          _this.$notify({
            title: "成功",
            message: "拒绝成功",
            type: "success"
          });
        } else {
          _this.$notify({
            title: "失败",
            message: "面拒绝失败",
            type: "error"
          });
        }
        _this.refuseLoading = false;
      });
    },
    //加入人才库
    addtotalents() {
      var _this = this;
      const elm = this.$createElement;
      if (this.editForm.ids.length == 0) {
        _this.$notify({
          title: "失败",
          message: "请选选择员工",
          type: "error"
        });
        return;
      }
      this.$msgbox({
        title: "",
        message: elm("div", { attrs: { class: "imgDialogInner" } }, [
          elm("span", { attrs: { class: "imgInfo" } }, ""),
          elm("span", null, "确定将此简历加入人才库")
        ]),
        confirmButtonText: "确定",
        customClass: "img_dialog",
        center: true
      })
        .then(function() {
          _this.editForm.status = 6;
          _this.updateProcess();
        })
        .then(data => {})
        .catch(err => {
          _this.$notify({
            title: "失败",
            message: "加入人才库失败",
            type: "error"
          });
        });
    },
    //导出excel
    exportExcel() {
      var excelName = "";
      var status = this.applicant_status;
      switch (status) {
        case 0:
          excelName = "已投递-";
          break;
        case 1:
          excelName = "已拒绝-";
          break;
        case 2:
          excelName = "已邀请-";
          break;
        case 3:
          excelName = "待入职-";
          break;
        case 4:
          excelName = "待复试-";
          break;
        case 5:
          excelName = "已入职-";
          break;
        case 6:
          excelName = "已入库-";
          break;
      }

      excelName += `应聘信息&(${this.searchForm.rangTime})`;

      excel(document.querySelector(".el-table"), excelName);
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
        this.searchForm.ageRang = null;
        this.searchForm.rangTime = null;
        this.depIds = [];
        this.page.currentPage = 1;
        this.getList(this.page);
      }
    },
    resetInterviewForm(formName) {
      //重置，面试邀请
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
      }
      this.interviewFormVisible = false;
    },
    resetReexamineForm(formName) {
      //重置，复试邀请
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
      }
      this.reexamineFormVisible = false;
    },
    resetInviteEntryForm(formName) {
      //重置，入职邀请
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
      }
      this.inviteEntryFormVisible = false;
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
.el-dialog {
  background: #f1f2f6;
}
::v-deep .el-checkbox.is-bordered.el-checkbox--small {
  width: 145px;
  padding-top: 7px;
  margin: 5px 20px 15px 0;
}
</style>
