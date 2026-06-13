<!--访客预约，预约记录  -->
<template>
  <div class="my-basic-container visitor">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="content">
          <div class="tip1">使用说明：设置访客记录邮件推送，系统会定时生成excel文件，发送到推送人邮箱里</div>

          <el-form
            ref="emailForm"
            :rules="emailRule"
            :inline="false"
            class="dot-form"
            :model="emailForm"
            label-width="150px"
          >
            <el-form-item label="所属园区" class="w1" prop="parkId">
              <parkSelect v-model="emailForm.parkId" @doChange="getPushEmail"></parkSelect>
            </el-form-item>
            <el-form-item label="设置记录统计周期" class="w1" prop="type">
              <el-select v-model="emailForm.type" placeholder="请选择统计周期">
                <el-option
                  v-for="item in periods"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="设置记录推送人">
              <div>
                <p>
                  已添加
                  <span style="color:#ed6d00">{{emailForm.emails.length}}</span> 位推送人信息
                </p>
                <table class="tbl">
                  <tr>
                    <td>推送人姓名</td>
                    <td>推送人邮箱</td>
                  </tr>
                  <tr v-for="(item, index) in emailForm.emails" :key="index">
                    <td>
                      <el-input type="text" v-model="item.receiver" readonly></el-input>
                    </td>
                    <td>
                      <el-input type="text" v-model="item.email" readonly></el-input>
                      <div class="line-btn">
                        <el-button type="text" icon="el-icon-delete" @click="delLine(index)"></el-button>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <el-form-item label prop="receiver">
                        <el-input type="text" v-model="emailForm.receiver" placeholder="点击输入姓名"></el-input>
                      </el-form-item>
                    </td>
                    <td>
                      <el-form-item label prop="email">
                        <el-input type="text" v-model="emailForm.email" placeholder="点击输入邮箱"></el-input>
                      </el-form-item>
                      <div class="line-btn">
                        <el-button type="text" icon="el-icon-plus" @click="addLine"></el-button>
                      </div>
                    </td>
                  </tr>
                </table>
                <div class="tip2">
                  <p>1、输入姓名、邮箱。（输入后要点击右侧的 " + " 才视为完成一条推送人信息的添加 ）</p>
                  <p>2、推送的邮件名为：{园区名称}-访客记录报表-统计起止时间</p>
                  <p>如“大岭山园区-访客记录报表-20190901-20190902”</p>
                </div>
              </div>
            </el-form-item>
            <el-form-item label>
              <el-button type="primary" @click="saveInfo('emailForm')">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, addObj, editObj } from "@/api/platform/visitor/email_push";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
const periodOption = [
  {
    label: "每一天",
    value: 0
  },
  {
    label: "每一周",
    value: 1
  },
  {
    label: "每一月",
    value: 2
  }
];
export default {
  name: "visitor",
  data() {
    var nameValid = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error("请输入推送人姓名"));
      } else {
        if (/^[0-9a-zA-Z\u4e00-\u9fa5_\s]{2,20}$/.test(value) == false) {
          callback(
            new Error(
              "姓名应包含2-20个字符，可为汉字、数字、字母（大小写）、下划线!"
            )
          );
        } else {
          callback();
        }
      }
    };
    var emailvalid = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error("请输入推送人邮箱"));
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
    };
    return {
      emailForm: {
        parkId: undefined,
        type: "",
        emails: []
      },
      searchForm: {
        parkId: undefined
      },
      periods: periodOption,
      emailRule: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        type: [
          { required: true, message: "请选择统计周期", trigger: "change" }
        ],
        receiver: [{ validator: nameValid, trigger: "blur" }],
        email: [{ validator: emailvalid, trigger: "blur" }]
      }
    };
  },
  created() {},
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    getPushEmail(val) {
      this.searchForm.parkId = val;
      this.fetchList();
    },
    fetchList() {
      fetchList(this.searchForm)
        .then(response => {
          var data = response.data.data;
          if (data.length > 0) {
            this.emailForm.emails = data;
            this.emailForm.type = data[0].type;
          } else {
            this.emailForm.emails = [];
            this.emailForm.type = null;
          }
        })
        .catch(err => { console.error(err) });
    },
    saveInfo(formName) {
      if (this.emailForm.emails.length > 0) {
        this.$refs.emailForm.validateField("type", msg => {
          if (this.validatenull(msg)) {
            editObj(this.emailForm)
              .then(response => {
                var dataResult = response.data.data;
                if (dataResult == true) {
                  this.fetchList();
                  this.$notify({
                    title: "成功",
                    message: msg,
                    type: "success",
                    duration: 2000
                  });
                } else if (dataResult === false) {
                  this.$notify({
                    title: "失败",
                    message: msg,
                    type: "error",
                    duration: 2000
                  });
                }
              })
              .catch(err => { console.error(err) });
          }
        });
      } else {
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.$message({
              message: '请点击推送人列表右侧的"+"完成推送人信息的添加！',
              type: "warning"
            });
          } else {
            return false;
          }
        });
      }
    },
    addLine() {
      let nameIsOk = false;
      let emailIsOk = false;
      this.$refs.emailForm.validateField("receiver", msg => {
        if (this.validatenull(msg)) {
          nameIsOk = true;
        } else {
          nameIsOk = false;
        }
      });
      this.$refs.emailForm.validateField("email", msg => {
        if (this.validatenull(msg)) {
          emailIsOk = true;
        } else {
          emailIsOk = false;
        }
      });
      if (nameIsOk && emailIsOk) {
        this.emailForm.emails.push({
          receiver: this.emailForm.receiver,
          email: this.emailForm.email
        });
        this.emailForm.receiver = "";
        this.emailForm.email = "";
      }
    },
    delLine(index) {
      this.emailForm.emails.splice(index, 1);
    }
  }
};
</script>

<style lang="scss" scoped>
.content ::v-deep {
  width: 800px;
  padding: 50px 0 0 60px;
  .w1 {
    width: 400px;
  }
  .tip1 {
    color: #ed6d00;
    font-size: 16px;
    padding-bottom: 10px;
    margin-bottom: 20px;
  }
  .tip2 {
    color: #999;
    font-size: 12px;
    line-height: 25px;
  }
  .el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label:before {
    content: "";
  }
  .tbl {
    width: 100%;
    margin-bottom: 50px;
    .el-input__inner {
      text-align: center;
      border: none;
      outline: none;
    }
    td {
      width: 50%;
      text-align: center;
      border: 1px solid #e0e0e0;
      position: relative;
    }
    .line-btn {
      position: absolute;
      right: -40px;
      top: 0;
      .el-button {
        font-size: 18px;
      }
    }
  }
}
</style>
