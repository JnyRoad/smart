<!--招聘管理：应聘管理的应聘详情-->
<template>
  <div class="my-basic-container center-card recruit_detail">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="center-conent">
          <div class="top-menu clear">
            <el-button
              type="primary"
              icon="el-icon-back"
              plain
              @click="goBack"
              style="margin-right:20px;"
            >返回</el-button>
            <div class="remark" v-if="data_application.status==1" style="display:inline-block;">
              <span class="circle-white">拒绝原因：</span>
              <span>{{data_application.refuseReason}}</span>
            </div>
            <!-- 0-已投递1-已拒绝 2-已邀请3-待入职/4待复试，暂定/5-已入职/6-已入库 -->
            <div class="btns">
              <el-button
                type="primary"
                v-if="data_application.status==0||data_application.status==1||data_application.status==6"
                @click="interviewFormVisible = true"
                round
              >邀请面试</el-button>
              <el-button
                type="primary"
                v-if="data_application.status==2"
                @click="reexamineFormVisible = true"
                round
              >复试</el-button>
              <el-button
                type="primary"
                v-if="data_application.status==2 ||data_application.status==4"
                @click="inviteEntryFormVisible = true"
                round
              >录取</el-button>
              <el-button
                type="primary"
                v-if="data_application.status==3"
                @click="handleEntry"
                round
              >入职</el-button>
              <!-- 只有以下四种状态有拒绝按钮：已投递，已邀请，待复试，待入职 -->
              <el-button
                type="primary"
                v-if="data_application.status==0 || data_application.status==2 || data_application.status==3|| data_application.status==4"
                @click="openRefuse"
                plain
                round
              >拒绝</el-button>
              <el-button
                type="primary"
                v-if="data_application.status==2"
                @click="addtotalents()"
                plain
                round
              >加入人才库</el-button>
            </div>
          </div>
          <el-row class="border-btm info1">
            <el-col :span="20">
              <div class="job-info clear">
                <p class="circle-white">应聘岗位：{{data_applicant.jobName}}</p>
                <p class="circle-white">园区：{{application.parkName}}</p>
                <p class="circle-white">BU：{{data_applicant.compName}}</p>
                <p class="circle-white">部门：{{data_applicant.depName}}</p>
                <p class="circle-white">职层：{{data_applicant.jcheName}}</p>
                <p class="circle-white">投递时间：{{data_application.applyDate}}</p>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="status-bg">
                <span class="status-text">{{data_application.status|applyStatusFormat}}</span>
              </div>
            </el-col>
          </el-row>
          <el-row class="border-btm info2">
            <el-col :span="4">
              <dl class="img-lft">
                <dt>
                  <div>
                    <viewer>
                      <img
                        :src="application.facePic || errorImgPeaple()"
                        :onerror="errorImgPeaple()"
                      />
                    </viewer>
                  </div>
                </dt>
                <dd>人脸照片</dd>
              </dl>
            </el-col>
            <el-col :span="14">
              <p class="person-name">{{data_application.name}}</p>
              <div class="person-info clear">
                <p>
                  <i class="psi psi-sex"></i>
                  {{data_application.sex | genderInit}}
                </p>
                <p>
                  <i class="psi psi-date"></i>
                  {{data_application.birth}}
                </p>
                <p>
                  <i class="psi psi-age"></i>
                  {{data_application.age}}
                </p>
                <p :title="data_application.nation">
                  <i class="psi psi-nation"></i>
                  {{data_application.nation}}
                </p>
                <p>
                  <i class="psi psi-edu"></i>
                  {{application.applicantEducation}}
                </p>
                <p>
                  <i class="psi psi-tel"></i>
                  {{data_application.phone}}
                </p>
                <p :title="data_application.police">
                  <i class="psi psi-police"></i>
                  {{data_application.police}}
                </p>
                <p>
                  <i class="psi psi-marriage"></i>
                  {{data_application.maritalStatus | marriageFilter}}
                </p>
                <p>
                  <i class="psi psi-email"></i>
                  {{application.applicantEmail}}
                </p>
              </div>
              <div class="person-info clear">
                <p>
                  <i class="psi psi-certno"></i>
                  {{data_application.certno}}
                </p>
                <p>
                  <i class="psi psi-validate"></i>
                  {{new Date(data_application.validDateFm)|parserDate}}
                  <template
                    v-if="data_application.validDateFm"
                  >-</template>
                  <template
                    v-if="data_application.validDateChar==='长期'"
                  >{{ data_application.validDateChar }}</template>
                  <template v-else>{{new Date(data_application.validDate)|parserDate}}</template>
                </p>
              </div>
              <div class="person-info clear">
                <p class="pi2-p2" :title="data_application.homeAddress">
                  <i class="psi psi-site"></i>
                  {{data_application.homeAddress}}
                </p>
              </div>
            </el-col>
            <el-col :span="6">
              <dl class="img-rt">
                <dt>
                  <div>
                    <viewer>
                      <img
                        :src="application.certnoPic || errorImgIdentity() "
                        :onerror="errorImgIdentity()"
                      />
                    </viewer>
                  </div>
                </dt>
                <dd>身份证照片</dd>
              </dl>
            </el-col>
          </el-row>
          <div class="info3" v-if="data_application.status==3||data_application.status==5">
            <p class="box-orange">紧急联系人</p>
            <template v-if="hasEmergency">
              <div class="info-inner clear" v-for="emergency in data_emergency" :key="emergency.id">
                <p class="circle-white">紧急联系人姓名：{{emergency.emergencyName}}</p>
                <p class="circle-white">紧急联系人关系：{{emergency.relationTypeDesc}}</p>
                <p class="circle-white">紧急联系人电话：{{emergency.emergencyPhone}}</p>
              </div>
            </template>
            <div class="noData" v-else>暂无紧急联系人信息</div>
          </div>
          <div class="info3">
            <p class="box-orange">教育经历</p>
            <template v-if="hasEducation">
              <div
                class="info-inner clear"
                v-for="data_education in data_education"
                :key="data_education.educationHisId"
              >
                <p class="circle-white">起始时间：{{data_education.startTime}}</p>
                <p class="circle-white">截止时间：{{data_education.endTime}}</p>
                <p class="circle-white">学校名称：{{data_education.schoolName}}</p>
                <p class="circle-white">专业：{{data_education.major}}</p>
                <p class="circle-white">学历：{{data_education.educationDesc}}</p>
                <p class="circle-white">是否为最高学历：{{data_education.isHighEduType==1 ? '是':'否'}}</p>
                <p class="circle-white">学位：{{data_education.degreeDesc}}</p>
                <p class="circle-white">是否为最高学位：{{data_education.isHighDegreeType==1 ? '是':'否'}}</p>
              </div>
            </template>
            <div class="noData" v-else>暂无教育经历信息</div>
          </div>
          <div class="info3">
            <p class="box-orange">工作经验</p>
            <template v-if="hasWork">
              <div class="info-inner clear" v-for="work in data_work" :key="work.id">
                <p class="circle-white">起始时间：{{work.startTime}}</p>
                <p class="circle-white">截止时间：{{work.endTime}}</p>
                <p class="circle-white">服务单位：{{work.company}}</p>
                <p class="circle-white">岗位：{{work.jobName}}</p>
                <p class="circle-white">负责人：{{work.personLiable}}</p>
                <p class="circle-white">联系方式：{{work.phone}}</p>
              </div>
            </template>
            <div class="noData" v-else>暂无工作经验信息</div>
          </div>
          <div class="info3" v-if="data_application.status==3||data_application.status==5">
            <p class="box-orange">家庭成员</p>
            <template v-if="hasFamily">
              <div
                class="info-inner clear"
                v-for="relation in data_family"
                :key="relation.data_family"
              >
                <p class="circle-white">亲属名称：{{relation.familyName}}</p>
                <p class="circle-white">亲属关系：{{relation.relationTypeDesc}}</p>
                <p class="circle-white">性别：{{relation.familyGender | genderInit}}</p>
                <p class="circle-white">出生日期：{{relation.familyBirthday}}</p>
                <p class="circle-white">所在单位：{{relation.familyCompany}}</p>
                <p class="circle-white">担任职务：{{relation.familyJob}}</p>
                <p class="circle-white">手机号：{{relation.emergencyPhone}}</p>
              </div>
            </template>
            <div class="noData" v-else>暂无家庭成员信息</div>
          </div>
          <div class="info3" v-if="data_application.status==3||data_application.status==5">
            <p class="box-orange">任职关系</p>
            <template v-if="hasRelation">
              <div
                class="info-inner clear"
                v-for="relation in data_relation"
                :key="relation.orgrelationId"
              >
                <p class="circle-white">姓名：{{relation.orgPersonName}}</p>
                <p class="circle-white">任职关系：{{relation.relationTypeDesc}}</p>
                <p class="circle-white">详细关系：{{relation.relationDetail?relation.relationDetail:'-'}}</p>
                <p class="circle-white">亲属BU：{{relation.orgPersonBu}}</p>
                <p class="circle-white">亲属部门：{{relation.orgPersonDept}}</p>
                <p class="circle-white">亲属岗位：{{relation.jobName?relation.jobName:'-'}}</p>
              </div>
            </template>
            <div class="noData" v-else>暂无任职关系信息</div>
          </div>
        </div>

        <!-- 邀请面试 -->
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
                value-format="yyyy-MM-dd HH:mm:ss"
                format="yyyy-MM-dd HH:mm"
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
            <el-input
              class="textarea"
              type="textarea"
              rows="5"
              v-model="form_refuse.other"
              placeholder
            ></el-input>
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
@use "@/styles/platform/recruit/detail" as *;
</style>

<script>
import { getById, putObj } from "@/api/platform/recruit/applicant_detail";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
import { dateFormat } from "@/util/date";

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
      interviewLoading: false, //邀请面试loading
      reexamineLoading: false, //邀请复试loading
      inviteEntryLoading: false, //邀请入职loading
      refuseLoading: false, //拒绝原因loading
      interviewFormVisible: false, //邀请面试
      reexamineFormVisible: false, //邀请复试
      inviteEntryFormVisible: false, //邀请入职
      refuseFormVisible: false, //拒绝原因
      applicantReason: yyqOptions,
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
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7; //8.64e7=1000*60*60*24一天
        }
      },
      form_refuse: {
        applicantGroup: [],
        firmGrop: [],
        other: ""
      },
      application: {},
      data_application: {},
      data_applicant: {},
      editForm: {
        ids: [],
        status: "",
        createUserNme: ""
      },
      data_education: {},
      data_work: {},
      data_relation: {},
      data_emergency: {},
      data_family: {}
    };
  },
  created() {
    var id = this.$route.params.id;
    this.editForm.ids.push(id);
    this.editForm.createUserNme = this.userInfo.username;
    this.getInfo();
  },
  mounted: function() {
    this.interviewForm.interviewTime = dateFormat(this.defaultValue);
    this.reexamineForm.reexamineTime = dateFormat(this.defaultValue);
    this.inviteEntryForm.inviteEntryTime = dateFormat(this.defaultValue);
  },
  filters: {
    marriageFilter(type) {
      const typeMap = {
        1: "未婚",
        2: "已婚",
        3: "丧偶",
        4: "离异",
        5: "其他"
      };
      return typeMap[type] ? typeMap[type] : "-";
    }
  },
  computed: {
    ...mapGetters(["userInfo", "permissions"]),
    computedReason: function() {
      let status = this.data_application.status;
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
    },
    hasEmergency: function() {
      let arr = this.data_emergency;
      if (isArrayFn(arr)) {
        if (arr.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    },
    hasEducation: function() {
      let arr = this.data_education;
      if (isArrayFn(arr)) {
        if (arr.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    },
    hasWork: function() {
      let arr = this.data_work;
      if (isArrayFn(arr)) {
        if (arr.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    },
    hasFamily: function() {
      let arr = this.data_family;
      if (isArrayFn(arr)) {
        if (arr.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    },
    hasRelation: function() {
      let arr = this.data_relation;
      if (isArrayFn(arr)) {
        if (arr.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    }
  },
  methods: {
    goBack() {
      this.$router.push({
        path: `/platform/recruit/applicant`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm,
          queryApplicantStatus: this.$route.query.queryApplicantStatus
        }
      });
    },
    getInfo() {
      var id = this.editForm.ids[0];
      getById(id).then(response => {
        this.application = response.data.data;
        this.application.certnoPic =
          "data:image/jpeg;base64," + this.application.certnoPic;
        this.application.facePic =
          "data:image/jpeg;base64," + this.application.facePic;
        this.data_applicant = response.data.data.recruitment;
        this.data_application = response.data.data.application;
        this.data_work = response.data.data.applicationWork;
        this.data_education = response.data.data.applicationEducation;
        this.data_emergency = response.data.data.applicationEmergency;
        this.data_relation = response.data.data.applicationRelation;
        this.data_family = response.data.data.applicationFamilyMember;
      });
    },

    updateProcess() {
      var _this = this;
      putObj(this.editForm).then(response => {
        var data = response.data.data;
        if (data == true) {
          _this.getInfo();
          _this.$notify({
            title: "成功",
            message: "加入人才库成功",
            type: "success"
          });
        } else {
          _this.$notify({
            title: "失败",
            message: "加入人才库失败",
            type: "success"
          });
        }
      });
    },

    //邀请面试，确定
    handleInterview(formName) {
      var _this = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editForm.status = 2;
          this.editForm.interviewTime = this.interviewForm.interviewTime;
          _this.interviewLoading = true;
          putObj(this.editForm)
            .then(response => {
              var data = response.data.data;
              if (data == true) {
                _this.getInfo();
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
                  type: "success"
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
    //邀请复试，确定
    handleReexamine(formName) {
      var _this = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editForm.status = 4;
          this.editForm.interviewTime = this.reexamineForm.reexamineTime;
          _this.reexamineLoading = true;
          putObj(this.editForm)
            .then(response => {
              var data = response.data.data;
              if (data == true) {
                _this.getInfo();
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
    //邀请入职，确定
    handleInviteEntry(formName) {
      var _this = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editForm.status = 3;
          this.editForm.interviewTime = this.inviteEntryForm.inviteEntryTime;
          _this.inviteEntryLoading = true;
          putObj(this.editForm)
            .then(response => {
              var data = response.data.data;
              if (data == true) {
                _this.getInfo();
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
    //入职
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
              _this.getInfo();
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
      if (this.data_application.status == 0) {
        this.editForm.status = 1;
        putObj(this.editForm).then(response => {
          var data = response.data.data;
          if (data == true) {
            _this.getInfo();
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
      var _this = this;
      _this.refuseLoading = true;
      putObj(this.editForm).then(response => {
        var data = response.data.data;
        if (data == true) {
          _this.getInfo();
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
            type: "success"
          });
        }
        _this.refuseLoading = false;
      });
    },
    //加入人才库
    addtotalents() {
      var _this = this;
      const elm = this.$createElement;
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
            type: "success"
          });
        });
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
::v-deep .el-checkbox.is-bordered.el-checkbox--small {
  width: 145px;
  padding-top: 7px;
  margin: 5px 20px 15px 0;
}
</style>
