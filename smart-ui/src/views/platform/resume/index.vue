<!--简历登记，第一步  -->
<template>
  <div class="my-basic-container resume">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top">
          <div class="top-logo">
            <img src="img/logo.png" />
            <span class="top-titile">
              裕同集团员工入职简历登记
              <span class="steps">【步骤1/3】</span>
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
                <span class="steps">【步骤1/3】</span>
              </div>
            </el-col>
          </el-row>-->
        </div>
        <div class="cont">
          <el-row class="flex" justify="center">
            <el-col :lg="2" class="hidden-xs-only hidden-sm-only">&nbsp;</el-col>
            <el-col :lg="11" :xs="24" :sm="24">
              <el-row>
                <el-col :lg="12" :xs="24" :sm="24">
                  <div class="img-bk">
                    <p class="p-title">上传身份证正面照</p>
                    <p class="tips">格式为png、jpg、jpeg、bmp</p>
                    <div class="img-info card-A">
                      <div class="img-outer">
                        <div class="img-inner">
                          <imgUpload
                            ref="imgUpload1"
                            @complete="cardAUpload"
                            :previewImage="cardA"
                          />
                          <div class="up-dv">
                            <i class="el-icon-upload2"></i>点击上传
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-col>
                <el-col :lg="12" :xs="24" :sm="24">
                  <div class="img-bk">
                    <p class="p-title">上传身份证反面照</p>
                    <p class="tips">格式为png、jpg、jpeg、bmp</p>
                    <div class="img-info card-B">
                      <div class="img-outer">
                        <div class="img-inner">
                          <imgUpload
                            ref="imgUpload1"
                            @complete="cardBUpload"
                            :previewImage="cardB"
                          />
                          <div class="up-dv">
                            <i class="el-icon-upload2"></i>点击上传
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-col>
              </el-row>
              <el-row>
                <div class="btndv litbtn">
                  <el-button
                    type="primary"
                    @click="ocrReading"
                    :disabled="!hasCardA || !hasCardB"
                    plain
                  >识别身份证信息</el-button>
                </div>
              </el-row>
            </el-col>
            <el-col :lg="1" class="hidden-xs-only hidden-sm-only">&nbsp;</el-col>
            <el-col :lg="8" :xs="24" :sm="24">
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
                    <p class="p-title">填写基础信息</p>
                    <p class="tips">请手动填写下面的信息，并确保信息准确无误</p>
                    <el-row>
                      <el-col :span="24">
                        <el-form-item label="电子邮箱" prop="email">
                          <el-input
                            v-model="addForm.email"
                            type="textarea"
                            placeholder="请输入"
                            autosize
                            clearable
                          ></el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-row>
                      <el-col :span="24">
                        <el-form-item label="婚姻状况" prop="maritalStatus">
                          <el-select v-model="addForm.maritalStatus" placeholder="请选择">
                            <el-option label="未婚" :value="1"></el-option>
                            <el-option label="已婚" :value="2"></el-option>
                            <el-option label="丧偶" :value="3"></el-option>
                            <el-option label="离异" :value="4"></el-option>
                            <el-option label="其他" :value="5"></el-option>
                          </el-select>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <p class="p-title" style="margin-top: 10px;">识别身份证信息</p>
                    <p class="tips">相关身份信息待上传身份证照后自动识别，若信息有误请手动修正</p>
                    <el-row>
                      <el-col :lg="13" :xs="24" :sm="24">
                        <el-form-item label="姓名" prop="name">
                          <el-input v-model="addForm.name" type="textarea" autosize clearable></el-input>
                        </el-form-item>
                      </el-col>
                      <el-col :lg="1" class="hidden-xs-only hidden-sm-only">&nbsp;</el-col>
                      <el-col :lg="10" :xs="24" :sm="24">
                        <el-form-item label="民族" prop="ethnicity">
                          <el-input v-model="addForm.ethnicity" type="textarea" autosize clearable></el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-row>
                      <el-col :lg="13" :xs="24" :sm="24">
                        <el-form-item label="身份证号" prop="identification">
                          <el-input
                            v-model="addForm.identification"
                            type="textarea"
                            autosize
                            clearable
                          ></el-input>
                        </el-form-item>
                      </el-col>
                      <el-col :lg="1" class="hidden-xs-only hidden-sm-only">&nbsp;</el-col>
                      <el-col :lg="10" :xs="24" :sm="24">
                        <!-- <el-form-item label="出生年月" prop="birthday" >
                          <el-input v-model="addForm.birthday" type="textarea" autosize clearable></el-input>
                        </el-form-item>-->
                        <el-form-item label="出生年月" prop="birthday">
                          <el-date-picker
                            v-model="addForm.birthday"
                            type="date"
                            value-format="yyyy-MM-dd"
                            format="yyyy-MM-dd"
                            placeholder
                            :picker-options="birthDayOption"
                            clearable
                          ></el-date-picker>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-row>
                      <el-col :span="24">
                        <el-form-item label="性别" prop="gender">
                          <el-radio v-model="addForm.gender" label="男">男</el-radio>
                          <el-radio v-model="addForm.gender" label="女">女</el-radio>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-row>
                      <el-col :span="24">
                        <el-form-item label="家庭住址" prop="address">
                          <el-input v-model="addForm.address" type="textarea" autosize clearable></el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-row>
                      <el-col :lg="13" :xs="24" :sm="24">
                        <el-form-item label="签发机关" prop="signOrg">
                          <el-input v-model="addForm.signOrg" type="textarea" autosize clearable></el-input>
                        </el-form-item>
                      </el-col>
                      <el-col :lg="1" class="hidden-xs-only hidden-sm-only">&nbsp;</el-col>
                      <el-col :lg="10" :xs="24" :sm="24">
                        <el-form-item label="有效期限" prop="validityDate">
                          <el-input
                            v-model="addForm.validityDate"
                            type="textarea"
                            autosize
                            clearable
                          ></el-input>
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </el-col>
                </el-row>
              </el-form>
            </el-col>
            <el-col :lg="2" class="hidden-xs-only hidden-sm-only">&nbsp;</el-col>
          </el-row>
          <el-row>
            <div class="btndv">
              <el-button type="primary" @click="nextStep('addForm')" :disabled="!ocrSucceed">下一步</el-button>
            </div>
          </el-row>
        </div>
      </section>
    </el-scrollbar>
    <el-dialog title :visible.sync="dialogVisible" :show-close="false" class="info-dialog">
      <div class="tips">请核对身份证信息，确认无误点击确定。如有误，点击取消后，在输入框可编辑身份信息再提交</div>
      <div class="info">
        <div>
          <span>姓名：</span>
          <span>{{addForm.name}}</span>
        </div>
        <div>
          <span>民族：</span>
          <span>{{addForm.ethnicity}}</span>
        </div>
        <div>
          <span>身份证号：</span>
          <span>{{addForm.identification}}</span>
        </div>
        <div>
          <span>出生年月：</span>
          <span>{{addForm.birthday}}</span>
        </div>
        <div>
          <span>性别：</span>
          <span>{{addForm.gender}}</span>
        </div>
        <div>
          <span>家庭住址：</span>
          <span>{{addForm.address}}</span>
        </div>
        <div>
          <span>签发机关：</span>
          <span>{{addForm.signOrg}}</span>
        </div>
        <div>
          <span>有效期限：</span>
          <span>{{addForm.validityDate}}</span>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="infoSubmit">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/resume/index" as *;
</style>

<script>
import { ocrRead, saveIdentification } from "@/api/platform/resume/index";
import { mapGetters } from "vuex";
var loading = "";
export default {
  name: "resume",
  data() {
    var validateEmail = (rule, value, callback) => {
      if (
        this.jcheName != "员工层" &&
        this.jcheName != "技工层" &&
        this.jcheName != "班组长层"
      ) {
        if (this.validatenull(value)) {
          callback(new Error("请输入邮箱地址"));
        } else {
          if (
            !/^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((.[a-zA-Z0-9_-]{2,3}){1,2})$/.test(
              value.replace(/(^\s*)|(\s*$)/g, "")
            )
          ) {
            callback(new Error("请输入正确的邮箱地址"));
          } else {
            callback();
          }
        }
      } else {
        if (!this.validatenull(value)) {
          if (
            !/^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((.[a-zA-Z0-9_-]{2,3}){1,2})$/.test(
              value.replace(/(^\s*)|(\s*$)/g, "")
            )
          ) {
            callback(new Error("请输入正确的邮箱地址"));
          } else {
            callback();
          }
        } else {
          callback();
        }
      }
    };

    var validatorvalidityDate = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error("请输入身份证有效期！"));
      } else {
        if (value.indexOf(".") > 0) {
          this.addForm.validityDate = value.replace(/\./g, "");
          callback();
        } else {
          callback();
        }
        // 判断身份证有效期
        // if (!/^\d{8}\-\d{8}$/.test(value)) {
        //   callback(
        //     new Error(
        //       "请输入yyyymmdd-yyyymmdd格式的有效期限，例如20191105-20291105"
        //     )
        //   );
        // } else {
        //   var dtNow = new Date(); //当前日期
        //   var nyy = dtNow.getFullYear();
        //   var nmm = dtNow.getMonth();
        //   var ndd = dtNow.getDate();
        //   var nDate = new Date(nyy, nmm, ndd);
        //   var endTime = value.split("-")[1]; //身份证有效期 的截止日期
        //   var yy = endTime.substr(0, 4);
        //   var mm = endTime.substr(4, 2) - 1; //month的值是0~11
        //   var dd = endTime.substr(6, 2);
        //   var endDate = new Date(yy, mm, dd);
        //   var _time = endDate - nDate;
        //   if (_time < 0) {
        //     callback(
        //       new Error("身份证有效期已过期，请使用在有效期内的证件进行认证！")
        //     );
        //   } else {
        //     callback();
        //   }
        // }
      }
    };

    return {
      dialogVisible: false,
      cardAimg: "/img/rm_cardA.png",
      cardBimg: "/img/rm_cardB.png",
      cardA: "", //身份证正面
      cardB: "", //身份证反面
      hasCardA: false, //是否已上传身份证正面
      hasCardB: false, //是否已上传身份证反面
      ocrSucceed: false, //是否完成识别身份证
      birthDayOption: {
        //出生年月只能选择今天和今天之前的
        disabledDate(time) {
          return time.getTime() > Date.now(); //
        }
      },
      recruitId: "",
      jcheName: "",
      addForm: {
        applicationId: null,
        recruitId: "", //招聘岗位id
        jcheName: "", //职层
        cardFrontImg: "",
        email: "", //工作邮件
        maritalStatus: 1, //婚姻状况 默认未婚
        name: "",
        ethnicity: "",
        identification: "",
        birthday: "",
        gender: "",
        address: "",
        signOrg: "",
        validityDate: ""
      },
      addRules: {
        email: [{ validator: validateEmail, trigger: "blur" }],
        name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
        ethnicity: [{ required: true, message: "请输入民族", trigger: "blur" }],
        identification: [
          { required: true, message: "请输入身份证号", trigger: "blur" }
        ],
        birthday: [
          { required: true, message: "请输入出生日期", trigger: "blur" }
        ],
        gender: [{ required: true, message: "请输入性别", trigger: "blur" }],
        address: [
          { required: true, message: "请输入家庭住址", trigger: "blur" }
        ],
        signOrg: [
          { required: true, message: "请输入签发机关", trigger: "blur" }
        ],
        validityDate: [
          { required: true, message: "请输入有效期限", trigger: "blur" },
          { validator: validatorvalidityDate, trigger: "blur" }
        ]
      }
    };
  },
  created: function() {
    this.recruitId = this.$route.params.id;
    this.clearLoacalStorage();
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    /*
     * 身份证正面占位图
     */
    cardAdefault() {
      return 'this.onload=null;this.src=" ' + this.cardAimg + '";';
    },
    /*
     * 身份证反面占位图
     */
    cardBdefault() {
      return 'this.onload=null;this.src=" ' + this.cardBimg + '";';
    },
    /*
     * 选择上传身份证正面照片
     */
    cardAUpload(e) {
      this.cardA = e;
      this.hasCardA = true;
    },
    /*
     * 判断图片格式是否正确
     */
    checkImgType(file) {
      if (this.validatenull(file.type)) {
        this.$message.error("解析图片格式异常，请尝试使用手机相机拍照功能！");
        return false;
      }
      if (
        file.type === "image/jpeg" ||
        file.type === "image/png" ||
        file.type === "image/bmp"
      ) {
        return true;
      } else {
        this.$message.error("上传头像图片只能是 JPG、PNG、 BMP格式!");
        return false;
      }
    },
    /*
     * 选择上传身份证反面照片
     */
    cardBUpload(e) {
      this.cardB = e;
      this.hasCardB = true;
    },
    /*
     * 下一步
     */
    nextStep(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.dialogVisible = true;
        } else {
          return false;
        }
      });
    },
    /*
     * 信息确认
     */
    infoSubmit() {
      this.addForm.cardFrontImg = this.cardA.replace(
        "data:image/jpeg;base64,",
        ""
      );
      this.openFullScreen();
      saveIdentification(this.addForm)
        .then(response => {
          if (response.data.code == 0) {
            var applicationId = response.data.data;
            loading.close();
            localStorage.setItem("applicationId", applicationId);
            const src = `/platform/face`;
            this.$router.push({
              path: src
            });
          }
        })
        .catch(function(err) {
          loading.close();
        });
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
     * 识别身份证信息
     */
    ocrReading() {
      this.openFullScreen();
      var identity = {
        id: this.recruitId,
        idCardFrontImg: this.cardA,
        idCardBackImg: this.cardB
      };
      ocrRead(identity)
        .then(response => {
          if (response.data.code == 0) {
            this.ocrSucceed = true;
            this.addForm = response.data.data;
            this.addForm.maritalStatus = 1;
            localStorage.setItem("jcheName", this.addForm.jcheName);
            this.jcheName = this.addForm.jcheName;
          }
          loading.close();
        })
        .catch(function(err) {
          loading.close();
        });
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
