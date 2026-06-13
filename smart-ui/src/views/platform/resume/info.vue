<!--简历登记，第三步  -->
<template>
  <div class="my-basic-container resume">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top">
          <div class="top-logo">
            <img src="img/logo.png" />
            <span class="top-titile">
              裕同集团员工入职简历登记
              <span class="steps">【步骤3/3】</span>
            </span>
          </div>
          <!-- <el-row class="flex"  justify="center">
            <el-col :lg="12" :xs="24" :sm="24">
              <div class="top-logo">
                <img src="img/logo.png"/>
              </div>
            </el-col>
            <el-col :lg="12" :xs="24" :sm="24">
              <div class="top-titile">
                裕同集团员工入职简历登记
                <span class="steps">【步骤3/3】</span>
              </div>
            </el-col>
          </el-row>-->
        </div>
        <div class="cont">
          <el-row class="flex" :gutter="40" justify="center">
            <el-col :lg="2" class="hidden-xs-only hidden-sm-only">&nbsp;</el-col>
            <el-col :lg="4" :xs="24" :sm="24">
              <el-row>
                <el-col :span="14">
                  <p class="info-title">紧急联系人</p>
                </el-col>
                <el-col :span="10">
                  <el-button
                    @click="addEmergencyOpen('emergencyForm')"
                    :disabled="hasEmergency"
                    type="text"
                    icon="el-icon-plus"
                  >添加联系人</el-button>
                </el-col>
              </el-row>
              <el-row class="infosq t1">
                <el-col :span="24">
                  <div v-if="!hasEmergency" class="noData">
                    <i></i>暂无数据
                  </div>
                  <template v-else>
                    <div class="item">
                      <p>
                        <span class="i-name">{{emergencyInfo.emergencyName}}</span>
                      </p>
                      <p>
                        <span>{{relationLabel(emergencyInfo.relation)}}</span>
                        <span>{{emergencyInfo.phone}}</span>
                      </p>
                      <div class="item-btns">
                        <el-button
                          @click="editEmergency(emergencyInfo)"
                          type="text"
                          icon="el-icon-edit"
                        ></el-button>
                        <el-button
                          @click="delEmergency(emergencyInfo.id)"
                          type="text"
                          icon="el-icon-delete"
                        ></el-button>
                      </div>
                    </div>
                  </template>
                </el-col>
              </el-row>
            </el-col>
            <el-col :lg="4" :xs="24" :sm="24">
              <el-row>
                <el-col :span="14">
                  <p class="info-title">家庭成员</p>
                </el-col>
                <el-col :span="10">
                  <el-button
                    @click="addFamilyOpen('familyForm')"
                    type="text"
                    icon="el-icon-plus"
                  >添加家庭成员</el-button>
                </el-col>
              </el-row>
              <el-row class="infosq t2">
                <el-col :span="24">
                  <div v-if="!hasFamily" class="noData">
                    <i></i>暂无数据
                  </div>
                  <template v-else>
                    <template v-for="(item, index) in familyInfo">
                      <div class="item" :key="index">
                        <p>
                          <span class="i-name">{{item.name}}</span>
                        </p>
                        <p>
                          <span class="float-right">{{item.phone}}</span>
                          <span>{{relationLabel(item.relation)}}</span>
                          <span>|</span>
                          <span>{{item.sex | genderInit}}</span>
                        </p>
                        <p class="info2">
                          <span>{{item.company}}</span>
                          <span>|</span>
                          <span>{{item.job}}</span>
                        </p>
                        <div class="item-btns">
                          <el-button @click="editFamily(item)" type="text" icon="el-icon-edit"></el-button>
                          <el-button @click="delFamily(item.id)" type="text" icon="el-icon-delete"></el-button>
                        </div>
                      </div>
                    </template>
                  </template>
                </el-col>
              </el-row>
            </el-col>
            <el-col :lg="4" :xs="24" :sm="24">
              <el-row>
                <el-col :span="14">
                  <p class="info-title">教育经历</p>
                </el-col>
                <el-col :span="10">
                  <el-button
                    @click="addEducationOpen('educationForm')"
                    type="text"
                    icon="el-icon-plus"
                  >添加教育经历</el-button>
                </el-col>
              </el-row>
              <el-row class="infosq t3">
                <el-col :span="24">
                  <div v-if="!hasEducation" class="noData">
                    <i></i>暂无数据
                  </div>
                  <template v-else>
                    <template v-for="(item, index) in educationInfo">
                      <div class="item" :key="index">
                        <p>
                          <span class="i-name">{{item.schoolName}}</span>
                        </p>
                        <p>
                          <span>{{item.startTime | parseFormat}} - {{item.endTime | parseFormat}}</span>
                        </p>
                        <p>
                          <span>{{educationLabel(item.education)}}</span>
                          <span>|</span>
                          <span>{{item.major}}</span>
                          <span>|</span>
                          <span>{{degreeLabel(item.degree)}}</span>
                        </p>
                        <div class="item-btns">
                          <el-button @click="editEducation(item)" type="text" icon="el-icon-edit"></el-button>
                          <el-button
                            @click="delEducation(item.id)"
                            type="text"
                            icon="el-icon-delete"
                          ></el-button>
                        </div>
                      </div>
                    </template>
                  </template>
                </el-col>
              </el-row>
            </el-col>
            <el-col :lg="4" :xs="24" :sm="24">
              <el-row>
                <el-col :span="14">
                  <p class="info-title">工作经验</p>
                </el-col>
                <el-col :span="10">
                  <el-button @click="addWorkOpen('workForm')" type="text" icon="el-icon-plus">添加工作经验</el-button>
                </el-col>
              </el-row>
              <el-row class="infosq t4">
                <el-col :span="24">
                  <div v-if="!hasWork" class="noData">
                    <i></i>暂无数据
                  </div>
                  <template v-else>
                    <template v-for="(item, index) in workInfo">
                      <div class="item" :key="index">
                        <p>
                          <span class="i-name">{{item.company}}</span>
                        </p>
                        <p>
                          <span>{{item.startTime | parseFormat}} - {{item.endTime | parseFormat}}</span>
                        </p>
                        <p>
                          <span>{{item.jobName}}</span>
                        </p>
                        <p class="info2" style="margin-top:15px;">
                          <span class="float-right">{{item.phone}}</span>
                          <span>证明人：{{item.personLiable}}</span>
                        </p>
                        <div class="item-btns">
                          <el-button @click="editWork(item)" type="text" icon="el-icon-edit"></el-button>
                          <el-button @click="delWork(item.id)" type="text" icon="el-icon-delete"></el-button>
                        </div>
                      </div>
                    </template>
                  </template>
                </el-col>
              </el-row>
            </el-col>
            <el-col :lg="4" :xs="24" :sm="24">
              <el-row>
                <el-col :span="14">
                  <p class="info-title">任职关系</p>
                </el-col>
                <el-col :span="10">
                  <el-button
                    @click="addRelationOpen('relationForm')"
                    type="text"
                    icon="el-icon-plus"
                  >添加任职关系</el-button>
                </el-col>
              </el-row>
              <el-row class="infosq t5">
                <el-col :span="24">
                  <div v-if="!hasRelation" class="noData">
                    <i></i>暂无数据
                  </div>
                  <template v-else>
                    <template v-for="(item, index) in relationInfo">
                      <div class="item" :key="index">
                        <p>
                          <span class="i-name">{{item.orgPersonName}}</span>
                        </p>
                        <p>
                          <span>{{item.orgPersonBu}}</span>
                        </p>
                        <p>
                          <span>{{item.orgPersonDept}}</span>
                        </p>
                        <p class="info2" style="margin-top:15px;">
                          <span class="float-right">{{item.orgPersonGender | genderInit}}</span>
                          <span>关系：{{item.relationTypeDesc}}</span>
                        </p>
                        <div class="item-btns">
                          <el-button @click="editRelation(item)" type="text" icon="el-icon-edit"></el-button>
                          <el-button
                            @click="delRelation(item.orgrelationId)"
                            type="text"
                            icon="el-icon-delete"
                          ></el-button>
                        </div>
                      </div>
                    </template>
                  </template>
                </el-col>
              </el-row>
            </el-col>
            <el-col :lg="2" class="hidden-xs-only hidden-sm-only">&nbsp;</el-col>
          </el-row>
          <el-row>
            <div class="btndv">
              <el-button type="primary" @click="nextStep" :loading="submitLoading">提交</el-button>
            </div>
          </el-row>
        </div>
      </section>

      <!-- 添加紧急联系人 -->
      <el-dialog
        title="紧急联系人"
        class="dialog_form"
        width="450px"
        @close="rsEmergencyForm('emergencyForm')"
        :visible.sync="emergencyVisible"
      >
        <el-form
          :rules="emergencyRules"
          ref="emergencyForm"
          :model="emergencyForm"
          label-width="60px"
        >
          <el-form-item label="姓名" prop="emergencyName">
            <el-input v-model="emergencyForm.emergencyName" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="关系" prop="relation">
            <el-select v-model="emergencyForm.relation" placeholder="请选择" clearable>
              <el-option
                v-for="item in relations"
                :key="item.typeCode"
                :label="item.typeName"
                :value="item.typeCode"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="emergencyForm.phone" placeholder="请输入" clearable></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="rsEmergencyForm('emergencyForm')" plain>取 消</el-button>
          <el-button
            type="primary"
            @click="emergencyAdd('emergencyForm')"
            :loading="emergencyLoading"
          >确 定</el-button>
        </div>
      </el-dialog>

      <!-- 添加家庭成员 -->
      <el-dialog
        title="家庭成员"
        class="dialog_form"
        width="450px"
        @close="rsFamilyForm('familyForm')"
        :visible.sync="familyVisible"
      >
        <el-form :rules="familyRules" ref="familyForm" :model="familyForm" label-width="80px">
          <el-form-item label="家属姓名" prop="name">
            <el-input v-model="familyForm.name" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="亲属关系" prop="relation">
            <el-select v-model="familyForm.relation" placeholder="请选择" clearable>
              <el-option
                v-for="item in relations"
                :key="item.typeCode"
                :label="item.typeName"
                :value="item.typeCode"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="亲属性别" prop="sex">
            <el-select v-model="familyForm.sex" placeholder="请选择" clearable>
              <el-option
                v-for="item in genders"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="出生年月" prop="birth">
            <el-date-picker
              v-model="familyForm.birth"
              type="date"
              value-format="yyyy.MM.dd"
              placeholder="请选择"
              :picker-options="birthDayOption"
              clearable
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="所在单位" prop="company">
            <el-input v-model="familyForm.company" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="担任职务" prop="job">
            <el-input v-model="familyForm.job" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="familyForm.phone" placeholder="请输入" clearable></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="rsFamilyForm('familyForm')" plain>取 消</el-button>
          <el-button type="primary" @click="familyAdd('familyForm')" :loading="familyLoading">确 定</el-button>
        </div>
      </el-dialog>

      <!-- 添加任职关系 -->
      <el-dialog
        title="任职关系"
        class="dialog_form"
        width="500px"
        @close="rsRelationForm('relationForm')"
        :visible.sync="relationDialog.visible"
      >
        <el-form
          :rules="relationDialog.ruleAdd"
          ref="relationForm"
          :model="relationDialog.form"
          label-width="80px"
        >
          <template v-if="!existRelationStaff&&!relationDialog.editing">
            <el-form-item label="工号" prop="staffId">
              <el-input v-model="relationDialog.form.staffId" placeholder="请输入" clearable>
                <el-button type="info" slot="append" icon="el-icon-search" @click="getStaffInf('relationForm')"></el-button>
              </el-input>
            </el-form-item>
          </template>
          <template v-else>
            <el-form-item label="工号" prop="badge">
              <el-input v-model="relationDialog.form.badge" placeholder clearable readonly></el-input>
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="relationDialog.form.name" placeholder clearable readonly></el-input>
            </el-form-item>
            <el-form-item label="性别" prop="sex">
              <el-input
                :value="relationDialog.form.sex | genderInit"
                placeholder
                clearable
                readonly
              ></el-input>
            </el-form-item>
            <el-form-item label="亲属BU" prop="compName">
              <el-input v-model="relationDialog.form.compName" placeholder clearable readonly></el-input>
            </el-form-item>
            <el-form-item label="亲属部门" prop="deptName">
              <el-input v-model="relationDialog.form.deptName" placeholder clearable readonly></el-input>
            </el-form-item>
            <el-form-item label="亲属岗位" prop="jobName">
              <el-input v-model="relationDialog.form.jobName" placeholder clearable readonly></el-input>
            </el-form-item>
            <el-form-item label="任职关系" prop="relation">
              <el-select v-model="relationDialog.form.relation" placeholder="请选择" clearable>
                <el-option
                  v-for="item in relations"
                  :key="item.typeCode"
                  :label="item.typeName"
                  :value="item.typeCode"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="详细关系" prop="relationDetail">
              <el-input v-model="relationDialog.form.relationDetail" placeholder="请输入" clearable></el-input>
            </el-form-item>
          </template>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="rsRelationForm('relationForm')" plain>取 消</el-button>
          <el-button
            type="primary"
            @click="relationAdd('relationForm')"
            :loading="relationDialog.loading"
            :disabled="!existRelationStaff"
          >确 定</el-button>
        </div>
      </el-dialog>

      <!-- 添加教育经历 -->
      <el-dialog
        title="教育经历"
        class="dialog_form"
        width="500px"
        @close="rsEducationForm('educationForm')"
        :visible.sync="educationVisible"
      >
        <el-form
          :rules="educationRules"
          ref="educationForm"
          :model="educationForm"
          label-width="120px"
        >
          <el-form-item label="开始日期" prop="startTime">
            <el-date-picker
              v-model="educationForm.startTime"
              type="date"
              value-format="yyyy.MM.dd"
              placeholder="请选择"
              clearable
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="结束日期" prop="endTime">
            <el-date-picker
              v-model="educationForm.endTime"
              type="date"
              value-format="yyyy.MM.dd"
              placeholder="请选择"
              clearable
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="学校名称" prop="schoolName">
            <el-input v-model="educationForm.schoolName" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-input v-model="educationForm.major" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="学历" prop="education">
            <el-select v-model="educationForm.education" placeholder="请选择" clearable>
              <el-option
                v-for="item in educations"
                :key="item.typeCode"
                :label="item.typeName"
                :value="item.typeCode"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="是否为最高学历" prop="isHighEduType">
            <el-select v-model="educationForm.isHighEduType" placeholder="请选择" clearable>
              <el-option label="是" :value="1"></el-option>
              <el-option label="否" :value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="学位" prop="degree">
            <el-select
              v-model="educationForm.degree"
              placeholder="请选择"
              clearable
              @change="degreeChange(educationForm.degree)"
            >
              <el-option
                v-for="item in degrees"
                :key="item.typeCode"
                :label="item.typeName"
                :value="item.typeCode"
              ></el-option>
            </el-select>
          </el-form-item>
          <template
            v-if="educationForm.degree!=4&&educationForm.degree!=undefined&&educationForm.degree!=''"
          >
            <el-form-item label="是否为最高学位" prop="isHighDegreeType">
              <el-select v-model="educationForm.isHighDegreeType" placeholder="请选择" clearable>
                <el-option label="是" :value="1"></el-option>
                <el-option label="否" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </template>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="rsEducationForm('educationForm')" plain>取 消</el-button>
          <el-button
            type="primary"
            @click="educationAdd('educationForm')"
            :loading="educationLoading"
          >确 定</el-button>
        </div>
      </el-dialog>

      <!-- 添加工作经验 -->
      <el-dialog
        title="工作经验"
        class="dialog_form"
        width="450px"
        @close="rsWorkForm('workForm')"
        :visible.sync="workVisible"
      >
        <el-form :rules="workRules" ref="workForm" :model="workForm" label-width="100px">
          <el-form-item label="开始日期" prop="startTime">
            <el-date-picker
              v-model="workForm.startTime"
              type="date"
              value-format="yyyy.MM.dd"
              placeholder="请选择"
              clearable
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="结束日期" prop="endTime">
            <el-date-picker
              v-model="workForm.endTime"
              type="date"
              value-format="yyyy.MM.dd"
              placeholder="请选择"
              clearable
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="公司" prop="company">
            <el-input v-model="workForm.company" placeholder="请选择" clearable></el-input>
          </el-form-item>
          <el-form-item label="职务" prop="jobName">
            <el-input v-model="workForm.jobName" placeholder="请选择" clearable></el-input>
          </el-form-item>
          <el-form-item label="负责人" prop="personLiable">
            <el-input v-model="workForm.personLiable" placeholder="请选择" clearable></el-input>
          </el-form-item>
          <el-form-item label="负责人电话" prop="phone">
            <el-input v-model="workForm.phone" placeholder="请选择" clearable></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="rsWorkForm('workForm')" plain>取 消</el-button>
          <el-button type="primary" @click="workAdd('workForm')" :loading="workLoading">确 定</el-button>
        </div>
      </el-dialog>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/resume/index" as *;
</style>

<script>
import {
  getRelations,
  getDegrees,
  getEducations,
  addEmergency,
  editEmergency,
  delEmergency,
  getEmergency,
  addFamily,
  editFamily,
  delFamily,
  getFamily,
  addEducation,
  editEducation,
  delEducation,
  getEducation,
  addWork,
  editWork,
  delWork,
  getWork,
  selectStaffInfo,
  addRelation,
  editRelation,
  delRelation,
  getRelation,
  deliver
} from "@/api/platform/resume/index";
import { mapGetters } from "vuex";
import { isMobile } from "@/util/validate";

const genderOption = [
  { label: "男", value: 0 },
  { label: "女", value: 1 }
];

export default {
  name: "resume",
  data() {
    var validatePhone = (rule, value, callback) => {
      if (!this.validatenull(value)) {
        if (!isMobile(value.replace(/(^\s*)|(\s*$)/g, ""))) {
          callback(new Error("请输入正确的电话"));
        } else {
          callback();
        }
      } else {
        callback();
      }
    };

    var validateWorkPhone = (rule, value, callback) => {
      if (
        this.jcheName != "员工层" &&
        this.jcheName != "技工层" &&
        this.jcheName != "班组长层"
      ) {
        if (this.validatenull(value)) {
          callback(new Error("请输入电话"));
        } else {
          if (!isMobile(value.replace(/(^\s*)|(\s*$)/g, ""))) {
            callback(new Error("请输入正确的电话"));
          } else {
            callback();
          }
        }
      } else {
        if (!this.validatenull(value)) {
          if (!isMobile(value.replace(/(^\s*)|(\s*$)/g, ""))) {
            callback(new Error("请输入正确的电话"));
          } else {
            callback();
          }
        } else {
          callback();
        }
      }
    };

    var validateProver = (rule, value, callback) => {
      if (
        this.jcheName != "员工层" &&
        this.jcheName != "技工层" &&
        this.jcheName != "班组长层"
      ) {
        if (this.validatenull(value)) {
          callback(new Error("请输入负责人"));
        } else {
          callback();
        }
      } else {
        callback();
      }
    };

    var validateTime1 = (rule, value, callback) => {
      if (value < this.educationForm.startTime) {
        callback(new Error("结束日期必须大于开始日期"));
      } else {
        callback();
      }
    };
    var validateTime2 = (rule, value, callback) => {
      if (value < this.workForm.startTime) {
        callback(new Error("结束日期必须大于开始日期"));
      } else {
        callback();
      }
    };

    return {
      maritalStatus: 1, //婚姻状况，默认未婚
      genders: genderOption, //性别
      relations: [], //关系集合{typeName:'',typeCode:''}
      educations: [], //学历集合{typeName:'',typeCode:''}
      degrees: [], //学位集合{typeName:'',typeCode:''}
      applicationId: "",
      jcheName: "",

      emergencyVisible: false,
      familyVisible: false,
      educationVisible: false,
      workVisible: false,

      emergencyEditing: false,
      familyEditing: false,
      educationEditing: false,
      workEditing: false,

      emergencyLoading: false,
      familyLoading: false,
      educationLoading: false,
      workLoading: false,
      submitLoading: false,
      birthDayOption: {
        //出生年月只能选择今天和今天之前的
        disabledDate(time) {
          return time.getTime() > Date.now(); //
        }
      },
      relationInfo: [], //任职关系信息
      existRelationStaff: false, //是否有查到指定的员工信息
      relationDialog: {
        visible: false,
        editing: false,
        loading: false,
        form: {
          staffId: undefined, //查询使用的id字段
          badge: undefined, //添加使用的id字段
          name: undefined,
          relation: undefined,
          compName: undefined,
          deptName: undefined,
          sex: undefined,
          applicationId: undefined,
          relationDetail: undefined,
          jobName: undefined
        },
        ruleSelect: {
          staffId: [
            { required: true, message: "请输入员工工号", trigger: "blur" }
          ]
        },
        ruleAdd: {
          relation: [
            { required: true, message: "请选择亲属关系", trigger: "change" }
          ]
        }
      },
      emergencyInfo: {}, //紧急联系人信息
      emergencyForm: {
        //紧急联系人添加表单
        applicationId: "",
        emergencyName: "", //姓名
        relation: "", //关系, 来自关系字典
        phone: "" //电话
      },
      emergencyRules: {
        emergencyName: [
          { required: true, message: "请输入姓名", trigger: "blur" }
        ],
        relation: [
          { required: true, message: "请选择关系", trigger: "change" }
        ],
        phone: [
          { required: true, message: "请输入电话", trigger: "blur" },
          { validator: validatePhone, trigger: "blur" }
        ]
      },

      familyInfo: [], //家庭成员信息
      familyForm: {
        //家庭成员添加表单
        applicationId: "",
        name: "", //姓名
        relation: "", //关系, 来自关系字典
        sex: "", //性别 0-男  1-女
        birth: "", //生日
        company: "", //所在单位
        job: "", //担任职务
        phone: "" //电话
      },
      familyRules: {
        name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
        relation: [
          { required: true, message: "请选择关系", trigger: "change" }
        ],
        sex: [{ required: true, message: "请选择性别", trigger: "change" }],
        birth: [{ required: true, message: "请选择出生年月", trigger: "blur" }],
        company: [
          { required: true, message: "请输入所在单位的名称", trigger: "blur" }
        ],
        job: [
          { required: true, message: "请输入所担任职务的名称", trigger: "blur" }
        ],
        phone: [
          { required: true, message: "请输入电话", trigger: "blur" },
          { validator: validatePhone, trigger: "blur" }
        ]
      },

      educationInfo: [], //教育经历信息
      educationForm: {
        //教育经历添加表单
        applicationId: "",
        startTime: "", //开始日期
        endTime: "", //结束日期
        schoolName: "", //学校名称
        major: "", //专业
        education: "", //学历，来自学历字典表code
        degree: "", //学位  来自学位字典表code
        isHighEduType: 0, //默认否
        isHighDegreeType: undefined //默认无，是：1，否：0
      },
      educationRules: {
        startTime: [
          { required: true, message: "请选择开始日期", trigger: "blur" }
        ],
        endTime: [
          { required: true, message: "请选择结束日期", trigger: "blur" },
          { validator: validateTime1, trigger: "blur" }
        ],
        schoolName: [
          { required: true, message: "请输入学校名称", trigger: "blur" }
        ],
        major: [{ required: true, message: "请输入专业", trigger: "blur" }],
        education: [
          { required: true, message: "请选择学历", trigger: "change" }
        ],
        degree: [{ required: true, message: "请选择学位", trigger: "change" }]
      },

      workInfo: [], //工作经验信息
      workForm: {
        //工作经验添加表单
        applicationId: "",
        startTime: "", //开始日期
        endTime: "", //结束日期
        company: "", //公司名称
        jobName: "", //岗位
        personLiable: "", //负责人
        phone: "" //负责人电话
      },
      workRules: {
        startTime: [
          { required: true, message: "请选择开始日期", trigger: "blur" }
        ],
        endTime: [
          { required: true, message: "请选择结束日期", trigger: "blur" },
          { validator: validateTime2, trigger: "blur" }
        ],
        company: [
          { required: true, message: "请输入公司名称", trigger: "blur" }
        ],
        jobName: [{ required: true, message: "请输入职务", trigger: "blur" }],
        personLiable: [{ validator: validateProver, trigger: "blur" }],
        phone: [{ validator: validateWorkPhone, trigger: "blur" }]
      }
    };
  },
  created: function() {
    this.applicationId = localStorage.getItem("applicationId");
    this.jcheName = localStorage.getItem("jcheName");
    if (
      this.validatenull(this.applicationId) ||
      this.validatenull(this.jcheName)
    ) {
      return;
    }

    this.getRelations(); //亲属关系集合
    this.getDegrees();
    this.getEducations();

    this.getEmergency(this.applicationId);
    this.getEducation(this.applicationId);
    this.getFamily(this.applicationId);
    this.getWork(this.applicationId);
    this.getRelation(this.applicationId); //任职关系
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"]),
    hasEmergency: function() {
      if (!this.validatenull(this.emergencyInfo)) {
        return true;
      } else {
        return false;
      }
    },
    hasFamily: function() {
      if (!this.validatenull(this.familyInfo)) {
        if (this.familyInfo.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    },
    hasRelation: function() {
      if (!this.validatenull(this.relationInfo)) {
        if (this.relationInfo.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    },
    hasEducation: function() {
      if (!this.validatenull(this.educationInfo)) {
        if (this.educationInfo.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    },
    hasWork: function() {
      if (!this.validatenull(this.workInfo)) {
        if (this.workInfo.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    },
    /**
     * 指定项是否必填
     */
    allIsRequired() {
      if (
        this.jcheName != "员工层" &&
        this.jcheName != "技工层" &&
        this.jcheName != "班组长层"
      ) {
        return true;
      } else {
        return false;
      }
    }
  },
  filters: {
    parseFormat: function(val) {
      return val.replace(/-/g, ".");
    }
  },
  methods: {
    /**
     * 添加紧急联系人，打开
     */
    addEmergencyOpen(formName) {
      this.emergencyEditing = false;
      this.emergencyVisible = true;
      this.emergencyForm = {};
    },
    /**
     * 添加家庭成员，打开
     */
    addFamilyOpen(formName) {
      this.familyEditing = false;
      this.familyVisible = true;
      this.familyForm = {};
      // this.$refs[formName].resetFields();
      // this.$refs[formName].clearValidate();
    },
    /**
     * 添加任职关系，打开
     */
    addRelationOpen(formName) {
      this.relationDialog.editing = false;
      this.relationDialog.visible = true;
      this.existRelationStaff = false;
      this.relationDialog.form = {};
      // this.$refs[formName].resetFields();
      // this.$refs[formName].clearValidate();
    },
    /**
     * 添加教育经历，打开
     */
    addEducationOpen(formName) {
      this.educationEditing = false;
      this.educationVisible = true;
      this.educationForm = {};
    },
    /**
     * 添加工作经验，打开
     */
    addWorkOpen(formName) {
      this.workEditing = false;
      this.workVisible = true;
      this.workForm = {};
    },
    /**
     * 添加紧急联系人，确定
     */
    emergencyAdd(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.emergencyLoading = true;
          this.emergencyForm.applicationId = this.applicationId;
          if (this.emergencyEditing == true) {
            //执行修改
            editEmergency(this.emergencyForm)
              .then(response => {
                if (response.data.code == 0) {
                  this.emergencyVisible = false;
                  this.getEmergency(this.applicationId);
                }
                this.emergencyLoading = false;
              })
              .catch(err => {
                this.emergencyLoading = false;
              });
          } else {
            //执行添加
            addEmergency(this.emergencyForm)
              .then(response => {
                if (response.data.code == 0) {
                  this.emergencyVisible = false;
                  this.getEmergency(this.applicationId);
                }
                this.emergencyLoading = false;
              })
              .catch(err => {
                this.emergencyLoading = false;
              });
          }
        } else {
          return false;
        }
      });
    },
    /**
     * 添加家庭成员，确定
     */
    familyAdd(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.familyLoading = true;
          this.familyForm.applicationId = this.applicationId;
          if (this.familyEditing == true) {
            //执行修改
            editFamily(this.familyForm)
              .then(response => {
                if (response.data.code == 0) {
                  this.familyVisible = false;
                  this.getFamily(this.applicationId);
                }
                this.familyLoading = false;
              })
              .catch(err => {
                this.familyLoading = false;
              });
          } else {
            //执行修改
            addFamily(this.familyForm)
              .then(response => {
                if (response.data.code == 0) {
                  this.familyVisible = false;
                  this.getFamily(this.applicationId);
                }
                this.familyLoading = false;
              })
              .catch(err => {
                this.familyLoading = false;
              });
          }
        } else {
          return false;
        }
      });
    },
    getStaffInf(formName) {
      this.$refs[formName].validateField("staffId", errorMessage => {
        if (!errorMessage) {
          selectStaffInfo(this.relationDialog.form.staffId)
            .then(response => {
              var staffInfo = response.data.data.smtStaff;
              if (!this.validatenull(staffInfo)) {
                this.existRelationStaff = true;
                this.relationDialog.form.name = staffInfo.name;
                this.relationDialog.form.badge = staffInfo.badge;
                this.relationDialog.form.sex = staffInfo.sex;
                this.relationDialog.form.compName = staffInfo.compName;
                this.relationDialog.form.deptName = staffInfo.depName;
                this.relationDialog.form.jobName = staffInfo.jobName;
              }
            })
            .catch(err => { console.error(err) });
        } else {
          return false;
        }
      });
    },
    /**
     * 添加任职关系，确定
     */
    relationAdd(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.relationDialog.loading = true;
          this.relationDialog.form.applicationId = this.applicationId;
          if (this.relationDialog.editing == true) {
            //执行修改
            editRelation(this.relationDialog.form)
              .then(response => {
                if (response.data.code == 0) {
                  this.relationDialog.visible = false;
                  this.getRelation(this.applicationId);
                }
                this.relationDialog.loading = false;
              })
              .catch(err => {
                this.relationDialog.loading = false;
              });
          } else {
            //执行添加
            addRelation(this.relationDialog.form)
              .then(response => {
                if (response.data.code == 0) {
                  this.relationDialog.visible = false;
                  this.getRelation(this.applicationId);
                }
                this.relationDialog.loading = false;
              })
              .catch(err => {
                this.relationDialog.loading = false;
              });
          }
        } else {
          return false;
        }
      });
    },
    /**
     * 添加教育经历，确定
     */
    educationAdd(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.educationLoading = true;
          this.educationForm.applicationId = this.applicationId;

          if (this.educationEditing == true) {
            //执行修改
            editEducation(this.educationForm)
              .then(response => {
                if (response.data.code == 0) {
                  this.educationVisible = false;
                  this.getEducation(this.applicationId);
                }
                this.educationLoading = false;
              })
              .catch(err => {
                this.educationLoading = false;
              });
          } else {
            //执行添加
            addEducation(this.educationForm)
              .then(response => {
                if (response.data.code == 0) {
                  this.educationVisible = false;
                  this.getEducation(this.applicationId);
                }
                this.educationLoading = false;
              })
              .catch(err => {
                this.educationLoading = false;
              });
          }
        } else {
          return false;
        }
      });
    },
    /**
     * 添加工作经验，确定
     */
    workAdd(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.workLoading = true;
          this.workForm.applicationId = this.applicationId;
          if (this.workEditing == true) {
            //执行修改
            editWork(this.workForm)
              .then(response => {
                if (response.data.code == 0) {
                  this.workVisible = false;
                  this.getWork(this.applicationId);
                }
                this.workLoading = false;
              })
              .catch(err => {
                this.workLoading = false;
              });
          } else {
            addWork(this.workForm)
              .then(response => {
                if (response.data.code == 0) {
                  this.workVisible = false;
                  this.getWork(this.applicationId);
                }
                this.workLoading = false;
              })
              .catch(err => {
                this.workLoading = false;
              });
          }
        } else {
          return false;
        }
      });
    },
    /**
     * 修改紧急联系人，打开
     */
    editEmergency(obj) {
      this.emergencyEditing = true;
      this.emergencyVisible = true;
      this.emergencyForm = Object.assign({}, obj);
    },
    /**
     * 修改家庭成员，打开
     */
    editFamily(obj) {
      this.familyEditing = true;
      this.familyVisible = true;
      this.familyForm = Object.assign({}, obj);
    },
    /**
     * 修改任职关系，打开
     */
    editRelation(obj) {
      this.relationDialog.editing = true;
      this.relationDialog.visible = true;
      this.existRelationStaff = true;

      let editObj = {};
      editObj.id = obj.orgrelationId;
      editObj.badge = obj.badege;
      editObj.name = obj.orgPersonName;
      editObj.relation = obj.relationType + "";
      editObj.compName = obj.orgPersonBu;
      editObj.deptName = obj.orgPersonDept;
      editObj.sex = obj.orgPersonGender;
      editObj.relationDetail = obj.relationDetail;
      editObj.jobName = obj.jobName;
      this.relationDialog.form = Object.assign({}, editObj);
    },
    /**
     * 修改教育经历，打开
     */
    editEducation(obj) {
      this.educationEditing = true;
      this.educationVisible = true;
      this.educationForm = Object.assign({}, obj);
    },
    /**
     * 修改工作经验，打开
     */
    editWork(obj) {
      this.workEditing = true;
      this.workVisible = true;
      this.workForm = Object.assign({}, obj);
    },
    /**
     * 删除，紧急联系人
     */
    delEmergency(id) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该紧急联系人信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delEmergency(id);
        })
        .then(data => {
          this.getEmergency(this.applicationId);
          _this.$notify({
            title: "成功",
            message: "删除成功",
            type: "success"
          });
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 删除，家庭成员
     */
    delFamily(id) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该家庭成员信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delFamily(id);
        })
        .then(data => {
          this.getFamily(this.applicationId);
          _this.$notify({
            title: "成功",
            message: "删除成功",
            type: "success"
          });
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 删除，任职关系
     */
    delRelation(id) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该任职关系信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delRelation(id);
        })
        .then(data => {
          this.getRelation(this.applicationId);
          _this.$notify({
            title: "成功",
            message: "删除成功",
            type: "success"
          });
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 删除，教育经历
     */
    delEducation(id) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该教育经历信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delEducation(id);
        })
        .then(data => {
          this.getEducation(this.applicationId);
          _this.$notify({
            title: "成功",
            message: "删除成功",
            type: "success"
          });
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 删除，工作经验
     */
    delWork(id) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该工作经验信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delWork(id);
        })
        .then(data => {
          this.getWork(this.applicationId);
          _this.$notify({
            title: "成功",
            message: "删除成功",
            type: "success"
          });
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 查询紧急联系人
     */
    getEmergency(applicationId) {
      getEmergency(applicationId)
        .then(response => {
          if (response.data.code == 0) {
            this.emergencyInfo = response.data.data;
          }
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 查询家庭成员
     */
    getFamily(applicationId) {
      getFamily(applicationId)
        .then(response => {
          if (response.data.code == 0) {
            this.familyInfo = response.data.data;
          }
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 查询任职关系
     */
    getRelation(applicationId) {
      getRelation(applicationId)
        .then(response => {
          if (response.data.code == 0) {
            this.relationInfo = response.data.data;
          }
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 查询教育经历
     */
    getEducation(applicationId) {
      getEducation(applicationId)
        .then(response => {
          if (response.data.code == 0) {
            this.educationInfo = response.data.data;
          }
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 查询工作经历
     */
    getWork(applicationId) {
      getWork(applicationId)
        .then(response => {
          if (response.data.code == 0) {
            this.workInfo = response.data.data;
          }
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 重置紧急联系人表单
     */
    rsEmergencyForm(formName) {
      this.emergencyVisible = false;
      this.$refs[formName].resetFields();
      this.$refs[formName].clearValidate();
    },
    /**
     * 重置家庭成员表单
     */
    rsFamilyForm(formName) {
      this.familyVisible = false;
      this.$refs[formName].resetFields();
      this.$refs[formName].clearValidate();
    },
    /**
     * 重置任职关系表单
     */
    rsRelationForm(formName) {
      this.relationDialog.visible = false;
      this.$refs[formName].resetFields();
      this.$refs[formName].clearValidate();
    },
    /**
     * 重置教育经历表单
     */
    rsEducationForm(formName) {
      this.educationVisible = false;
      this.$refs[formName].resetFields();
      this.$refs[formName].clearValidate();
    },
    /**
     * 重置工作经验表单
     */
    rsWorkForm(formName) {
      this.workVisible = false;
      this.$refs[formName].resetFields();
      this.$refs[formName].clearValidate();
    },
    /*
     * 提交
     */
    nextStep() {
      if (!this.hasEmergency) {
        this.$message.error("请填写紧急联系人");
        return;
      }
      if (this.allIsRequired) {
        if (!this.hasFamily) {
          this.$message.error("请填写家庭成员");
          return;
        }
        // if (!this.hasRelation) {
        //   this.$message.error("请填写任职关系");
        //   return;
        // }
        if (!this.hasEducation) {
          this.$message.error("请填写教育经历");
          return;
        }
        if (!this.hasWork) {
          this.$message.error("请填写工作经验");
          return;
        }
      }

      var params = {
        applicationId: this.applicationId
      };
      this.submitLoading = true;
      deliver(params)
        .then(response => {
          if (response.data.code == 0) {
            this.submitLoading = false;
            this.clearLoacalStorage();
            const src = `/platform/resumeFinish`;
            this.$router.push({
              path: src
            });
          }
          this.submitLoading = false;
        })
        .catch(err => {
          this.submitLoading = false;
        });
    },
    /**
     * 获取关系字典
     */
    getRelations() {
      getRelations()
        .then(response => {
          if (response.data.code == 0) {
            this.relations = response.data.data;
          }
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 获取学位字典
     */
    getDegrees() {
      getDegrees()
        .then(response => {
          if (response.data.code == 0) {
            this.degrees = response.data.data;
          }
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 获取学历字典
     */
    getEducations() {
      getEducations()
        .then(response => {
          if (response.data.code == 0) {
            this.educations = response.data.data;
          }
        })
        .catch(err => { console.error(err) });
    },
    /*
     * 根据关系code查询对应关系描述
     */
    relationLabel: function(code) {
      let obj = {};
      obj = this.relations.find(item => {
        return item.typeCode === code;
      });
      if (this.validatenull(obj)) {
        return code;
      } else {
        return obj.typeName;
      }
    },
    /*
     * 根据学历code查询对应学历描述
     */
    educationLabel: function(code) {
      let obj = {};
      obj = this.educations.find(item => {
        return item.typeCode === code;
      });
      if (this.validatenull(obj)) {
        return code;
      } else {
        return obj.typeName;
      }
    },
    /*
     * 根据学位code查询对应学位描述
     */
    degreeLabel: function(code) {
      let obj = {};
      obj = this.degrees.find(item => {
        return item.typeCode === code;
      });
      if (this.validatenull(obj)) {
        return code;
      } else {
        return obj.typeName;
      }
    },
    degreeChange(val) {
      // 4为无，只要选择了具体的学位，将是否为最高学位初始化为否，之后再自行选择
      if (val != 4) {
        // this.educationForm.isHighDegreeType = 0;
      }
    },
    /*
     * 清除本地存储
     */
    clearLoacalStorage() {
      localStorage.removeItem("applicationId");
      localStorage.removeItem("jcheName");
    }
  }
};
</script>

<style lang="scss" scoped>
::v-deep .my-basic-container {
  padding-bottom: 0px;
}
::v-deep .my-basic-inner {
  padding: 0;
}
</style>
