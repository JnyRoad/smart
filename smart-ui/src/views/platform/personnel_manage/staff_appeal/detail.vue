<!--人资管理：员工反馈详情 -->
<template>
  <div class="my-basic-container vehicle">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <el-form
          ref="editform"
          :rules="replyRules"
          :model="editform"
          size="mini"
        >
          <p class="box-orange">反馈信息</p>
          <table class="lit-table" border="false" v-if="!isEdit">
            <tr>
              <td>反馈人</td>
              <td>{{detailInfo.staffName}}</td>
            </tr>
            <tr>
              <td>手机号</td>
              <td>{{detailInfo.staffPhone}}</td>
            </tr>
            <tr>
              <td>BU</td>
              <td>{{detailInfo.compName}}</td>
            </tr>
            <tr>
              <td>部门</td>
              <td>{{detailInfo.depName}}</td>
            </tr>
            <tr>
              <td>工号</td>
              <td>{{detailInfo.badge}}</td>
            </tr>
            <tr>
              <td>反馈类型</td>
              <td>{{detailInfo.appealTypeDesc}}</td>
            </tr>
            <tr>
              <td>反馈时间</td>
              <td>{{detailInfo.createTime}}</td>
            </tr>
            <tr>
              <td>反馈描述</td>
              <td>{{detailInfo.appealDesc}}</td>
            </tr>
            <tr v-if="detailInfo.appealImgs !== null">
              <td>申诉图片</td>
              <td>
                <template v-for="(item, index) in detailInfo.appealImgs">
                  <dl class="img-info" :key="index">
                    <dt class="img-outer">
                      <div class="img-inner">
                        <viewer >
                          <img :src="item || errorImgPeaple()" :onerror="errorImgPeaple()" />
                        </viewer>
                      </div>
                    </dt>
                    <dd class="desc-info"></dd>
                  </dl>
                </template>
              </td>
            </tr>
            <tr>
              <td>状态</td>
              <td>{{detailInfo.statusDesc}}</td>
            </tr>
          </table>
          <div class="gray-line"></div>
          <p class="box-orange">回复信息</p>
          <el-row>
            <el-col :span="19">
              <table class="lit-table">
                <tr>
                  <td>回复内容</td>
                  <td>
                    <el-form-item prop="replyDesc" class="myarea">
                      <el-input
                        rows="5"
                        type="textarea"
                        prop="replyDesc"
                        :disabled="detailInfo.isChange"
                        :readonly="detailInfo.status === 2"
                        v-model="editform.replyDesc"
                        maxlength="100"
                        placeholder="回复内容"
                        clearable
                        @keyup.native="contentChange($event)"
                      ></el-input>
                      <span class="mytip">{{usedNum}}/{{totalNum}}</span>
                    </el-form-item>
                  </td>
                </tr>
                <tr>
                  <td>回复人</td>
                  <td>{{detailInfo.replyName}}</td>
                </tr>
                <tr>
                  <td>回复时间</td>
                  <td>{{detailInfo.replyTime}}</td>
                </tr>
              </table>
            </el-col>
          </el-row>
          <div class="btns-bottom" v-if="detailInfo.status === 1 && !detailInfo.isChange">
            <el-button type="primary" @click="saveReply('editform')">确定</el-button>
            <el-button
              type="primary"
              @click="changeFormVisible=true"
            >转交Ta人</el-button>
            <el-button type="primary" @click="saveCancel('editform')" plain>取消</el-button>
          </div>
        </el-form>
      </section>

      <!-- 转交Ta人弹窗 -->
      <el-dialog
        title="选择人员"
        class="dialog_form"
        width="550px"
        @close="resetchangeForm('changeForm')"
        :visible.sync="changeFormVisible"
      >
        <el-form ref="changeForm" :model="changeForm" :rules="changeRules" label-width="80px">
          <el-form-item label="工号" prop="stfBadge">
            <el-input v-model="changeForm.stfBadge" clearable>
              <el-button type="info" slot="append" @click="queryStaffList('changeForm')" icon="el-icon-search"></el-button>
            </el-input>
          </el-form-item>
          <el-form-item prop="changeBadge">
              <el-radio-group v-model="changeForm.changeBadge" class="mGroup">
                <el-radio
                  v-for="item in staffListInfo"
                  :key="item.badge"
                  :value="item.badge"
                  :label="item.badge"
                >{{item.info}}</el-radio>
              </el-radio-group>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="changeSubmit('changeForm')">确 定</el-button>
          <el-button type="primary" @click="resetchangeForm('changeForm')" plain>取 消</el-button>
        </div>
      </el-dialog>
    </el-scrollbar>
  </div>
</template>
<script>
import {
  getDetails,
  saveReply,
  queryStaffList,
  changeStaff,
} from "./staff_appeal";
import { allPark } from "@/api/platform/_publicService";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
export default {
  data() {
    var driverLicenseIdValidator = (rule, value, callback) => {
      var driverLicenseIdImg = this.deviceInfo.driverLicenseId;
      if (
        driverLicenseIdImg == null ||
        driverLicenseIdImg == "" ||
        driverLicenseIdImg == undefined
      ) {
        callback(new Error("请选择驾驶证照片！"));
      } else {
        callback();
      }
    };
    return {
      isEdit: false, //是否是编辑状态
      detailInfo: {},
      editform: {
        id: undefined,
        replyDesc: "",
      },
      usedNum: 0,
      totalNum: 100,
      staffListInfo: [], //员工列表信息
      changeFormVisible: false, //转交Ta人弹窗
      parkData: [],
      replyRules: {
        replyDesc: [
          { required: true, message: "请输入回复内容", trigger: "blur" },
        ],
      },
      changeForm: {
        id: undefined, //记录标识
        changeBadge: undefined, //转交员工工号
      },
      changeRules: {
        stfBadge:[
          { required: true, message: "请输入员工工号", trigger: "blur" },
        ],
        changeBadge: [
          { required: true, message: "请选择员工信息", trigger: "change" },
        ]
      },
    };
  },
  created() {
    getDetails(this.$route.params.id).then((response) => {
      let result = response.data.data;
      if (!this.validatenull(result)) {
        this.detailInfo = result;
        this.editform.id = result.id;
        this.editform.replyDesc = result.replyDesc;
        this.doClearValidate('editform')
      }
    });
  },
  computed: {
    ...mapGetters(["permissions"]),
  },
  methods: {
    async getParks() {
      const res = await allPark();
      this.parkData = res.data.data;
    },
    contentChange(e){
      let l = this.editform.replyDesc.length
      this.usedNum = l
    },
    goBack() {
      this.$router.push({
        path: `/platform/personnel_manage/staff_appeal`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm,
        },
      });
    },
    editInfo() {
      this.isEdit = true;
      this.doClearValidate("editform");
    },
    saveReply(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.editform.id = this.$route.params.id;
          saveReply(this.editform)
            .then((response) => {
              this.isEdit = false;
              this.$router.go(-1);
            })
            .catch(err => { console.error(err) });
        } else {
          return false;
        }
      });
    },
    resetEditform(formName) {
      this.$refs[formName].clearValidate();
    },
    resetchangeForm(formName) {
      this.changeFormVisible = false
      this.staffListInfo = [];
      this.$refs[formName].resetFields();
    },
    changeSubmit(formName) {
      this.changeForm.id = this.$route.params.id;
      if(this.staffListInfo.length && this.staffListInfo.length>0){
        this.$refs[formName].validateField("changeBadge", errorMessage => {
        if (!errorMessage) {
          changeStaff(this.changeForm)
            .then((response) => {
              this.changeFormVisible = false
              this.isEdit = false;
              this.$router.go(-1);
            })
            .catch(err => { console.error(err) });
        } else {
          return false;
        }
      });
      }else{
        this.$message.error('请先输入工号查询员工信息！');
      }
    },
    saveCancel(formName) {
      this.isEdit = false;
      this.resetEditform(formName);
      this.goBack();
    },
    queryStaffList(formName) {
      this.$refs[formName].validateField("stfBadge", errorMessage => {
        if (!errorMessage) {
          queryStaffList(this.changeForm.stfBadge).then((response) => {
            this.staffListInfo = response.data.data;
            if(!(this.staffListInfo.length && this.staffListInfo.length>0)){
              this.staffListInfo = []
              this.$message.error('未查到相关工号信息，请检查工号输入是否有误！');
            }
          });
        } else {
          return false;
        }
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.myarea{
  position: relative;
  .mytip{
    position: absolute;
    bottom: 0;
    right: 10px;
    color: #999;
    font-size: 12px;
  }
}
.mGroup{
  .el-radio+.el-radio{
    margin-left: 0;
  }
  .el-radio{
    margin: 0 10px 10px 0;
  }
}
.noPading {
  padding: 12px 20px;
}
.img-info {
  width: 100px;
  margin: 0 20px 0 0;
  float: left;
  .img-inner {
    top: 5px;
    right: 5px;
    bottom: 5px;
    left: 5px;
  }
}
.lit-table {
  width: 550px;
  margin: 0 0 20px 0;
}
.form-outer {
  max-width: 400px;
  margin-left: 30px;
}
.desc-info {
  color: #666;
  font-size: 12px;
}
.avatar-uploader-icon {
  width: 100%;
  height: 100%;
  font-size: 12px !important;
  font-style: normal;
  text-align: center;
  color: #999;
  // padding-top: 50%;
  display: inline-block;
  background-position: center;
  background-size: 100% 100%;
  background-repeat: no-repeat;
}
</style>
