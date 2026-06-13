<!--简历登记，第二步  -->
<template>
  <div class="my-basic-container resume">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top">
          <div class="top-logo">
            <img src="img/logo.png" />
            <span class="top-titile">
              裕同集团员工入职简历登记
              <span class="steps">【步骤2/3】</span>
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
                <span class="steps">【步骤2/3】</span>
              </div>
            </el-col>
          </el-row>-->
        </div>
        <div class="cont">
          <el-row class="flex" justify="center">
            <el-col :lg="5" class="hidden-xs-only hidden-sm-only">&nbsp;</el-col>
            <el-col :lg="6" :xs="24" :sm="24">
              <div class="img-bk">
                <p class="p-title">上传人脸正面照</p>
                <p class="tips">格式为png、jpg、jpeg、bmp</p>
                <div class="img-info face-A">
                  <div class="img-outer">
                    <div class="img-inner">
                      <moduleUpload
                        ref="moduleUpload"
                        @complete="cardAUpload"
                        :previewImage="cardA"
                      ></moduleUpload>
                    </div>
                    <div class="status-dv status-succeed" v-if="ocrSucceed && ocrPhote"></div>
                    <div class="status-dv status-failed" v-if="!ocrSucceed && ocrPhote"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :lg="1" class="hidden-xs-only hidden-sm-only">&nbsp;</el-col>
            <el-col :lg="7" :xs="24" :sm="24">
              <el-form
                ref="addForm"
                :disabled="!ocrSucceed"
                :model="addForm"
                :rules="addRules"
                label-width="80px"
                label-position="left"
              >
                <el-row>
                  <el-col :span="24">
                    <p class="p-title">绑定手机信息</p>
                    <p class="tips">【注意：当完成“上传人脸正面照”上传后，才可绑定手机信息】</p>
                    <el-row>
                      <el-col :span="24">
                        <el-form-item label="手机号码" prop="mobile">
                          <el-input v-model="addForm.mobile" placeholder="请输入手机号码" clearable></el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-row>
                      <el-col :lg="17" :xs="15" :sm="15">
                        <el-form-item label="验证码" prop="smsCode">
                          <el-input v-model="addForm.smsCode" placeholder="请输入验证码" clearable></el-input>
                        </el-form-item>
                      </el-col>
                      <el-col :lg="1" :xs="1" :sm="1">&nbsp;</el-col>
                      <el-col :lg="6" :xs="4" :sm="4">
                        <el-button
                          v-if="!sended"
                          type="primary"
                          @click="sendCode('addForm')"
                          plain
                        >发送验证码</el-button>
                        <el-button v-else type="text" disabled>{{timing}}s后重发</el-button>
                      </el-col>
                    </el-row>
                  </el-col>
                </el-row>
              </el-form>
            </el-col>
            <el-col :lg="5" class="hidden-xs-only hidden-sm-only">&nbsp;</el-col>
          </el-row>
          <el-row>
            <div class="btndv">
              <el-button
                type="primary"
                @click="nextStep('addForm')"
                :disabled="!ocrSucceed"
                :loading="submitLoading && ocrSucceed"
              >下一步</el-button>
            </div>
          </el-row>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/resume/index" as *;
</style>

<script>
import { addFace, sendMsg, bindMobile } from "@/api/platform/resume/index";
import { mapGetters } from "vuex";
import { isMobile } from "@/util/validate";
import moduleUpload from './_upload';
var loading = "";
export default {
  name: "resume",
  components: {
    moduleUpload
  },
  data() {
    var validatePhone = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error("请输入电话"));
      } else {
        if (!isMobile(value.replace(/(^\s*)|(\s*$)/g, ""))) {
          callback(new Error("请输入正确的电话"));
        } else {
          callback();
        }
      }
    };
    return {
      cardAimg: "/img/rm_face.png",
      cardA: "", //认脸照
      ocrSucceed: false, //人脸识别是否成功
      submitLoading: false,
      ocrPhote: false, //false 未开始认证， true 开始认证
      applicationId: "",
      addForm: {
        applicationId: "", //应聘者id
        mobile: "",
        smsCode: ""
      },
      addRules: {
        mobile: [
          { required: true, message: "请输入手机号码", trigger: "blur" },
          { validator: validatePhone, trigger: "blur" }
        ],
        smsCode: [{ required: true, message: "请输入验证码", trigger: "blur" }]
      },
      sended: false, //是否已点击发送验证码
      seconds: 60, //倒计时总秒数
      timing: 60 //倒计时，实时秒数，初始值跟seconds即可
    };
  },
  created: function() {
    this.applicationId = localStorage.getItem("applicationId");
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    /*
     * 占位图
     */
    cardAdefault() {
      return 'this.onload=null;this.src=" ' + this.cardAimg + '";';
    },
    takePhoto: function (data) {
      this.cardA = data;
      this.ocrReading();
    },
    /*
     * 选择上传图片
     */
    cardAUpload(e) {
      this.ocrPhote = false;
      this.cardA = e;
      //读取照片后就开始识别
      this.ocrReading();
    },
    /*
     * 下一步
     */
    nextStep(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.addForm.applicationId = this.applicationId;
          this.submitLoading = true;
          bindMobile(this.addForm)
            .then(response => {
              if (response.data.code == 0) {
                const src = `/platform/info`;
                this.$router.push({
                  path: src
                });
              }
              this.submitLoading = false;
            })
            .catch(err => {
              this.timing = 0;
              this.submitLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    /*
     * 发送短信验证码
     */
    sendCode(formName) {
      this.$refs[formName].validateField("mobile", errorMessage => {
        if (!errorMessage) {
          this.sended = true;
          this.timeCode(); //倒计时
          sendMsg(this.addForm.mobile).then(response => {});
        } else {
          return false;
        }
      });
    },
    /*
     * 发送短信验证码后的倒计时
     */
    timeCode() {
      let _this = this;
      var timeC = setInterval(function() {
        if (_this.timing == 0) {
          _this.sended = false;
          _this.timing = _this.seconds;
          clearInterval(timeC);
          return;
        }
        _this.timing--;
      }, 1000);
    },
    /*
     * loading层
     */
    openFullScreen() {
      loading = this.$loading({
        lock: true,
        text: "Loading",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, 0.7)"
      });
    },
    /*
     * 识别人脸
     */
    ocrReading() {
      this.openFullScreen();
      var identity = {
        applicationId: this.applicationId,
        facePhoto: this.cardA
      };
      addFace(identity)
        .then(response => {
          var info = response.data.data;
          this.ocrPhote = true;
          if (response.data.code == 0) {
            this.ocrSucceed = true;
          } else {
            this.ocrSucceed = false;
          }
          loading.close();
        })
        .catch(err => {
          this.ocrPhote = true;
          this.ocrSucceed = false;
          loading.close();
        });
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
