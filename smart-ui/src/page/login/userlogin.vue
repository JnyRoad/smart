<template>
  <el-form class="login-form" status-icon :rules="loginRules" ref="loginForm" :model="loginForm" label-width="0">
    <el-input type="password" class="hide" id="loginPassword"></el-input>
    <el-input type="text" class="hide" id="loginUserName"></el-input>
    <el-form-item prop="username">
      <el-input size="small" @keyup.enter.native="handleLogin" v-model="loginForm.username" auto-complete="off"
        placeholder="请输入用户名">
        <i slot="prefix" class="icon-yonghu"></i>
      </el-input>
    </el-form-item>
    <el-form-item prop="password">
      <el-input size="small" @keyup.enter.native="handleLogin" :type="passwordType" v-model="loginForm.password"
        auto-complete="off" placeholder="请输入密码">
        <i class="el-icon-view el-input__icon" slot="suffix" @click="showPassword"></i>
        <i slot="prefix" class="icon-mima"></i>
      </el-input>
    </el-form-item>
    <el-form-item prop="">
      <el-checkbox v-model="rememberPwd">记住密码</el-checkbox>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="small" @click.native.prevent="handleLogin" class="login-submit">登录
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import { randomLenNum } from "@/util/util";
import { mapGetters } from "vuex";

export default {
  name: "userlogin",
  data() {
    return {
      rememberPwd: false, //true, 勾选记住密码， false 未勾选
      isInvalid: true, //true,已过期，false, 未过期
      loginInfo: {}, //本地存储获取的登陆信息
      socialForm: {
        code: '',
        state: ''
      },
      loginForm: {
        username: "",
        password: "",
        code: "",
        redomStr: ""
      },
      checked: false,
      code: {
        src: "/code",
        value: "",
        len: 4,
        type: "image"
      },
      loginRules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" }
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { min: 4, message: "密码长度最少为4位", trigger: "blur" }
        ]
        //          ,
        //          code: [
        //            {required: true, message: "请输入验证码", trigger: "blur"},
        //            {min: 4, max: 4, message: "验证码长度为4位", trigger: "blur"}
        //          ]
      },
      passwordType: "password"
    };
  },
  created() {
    this.refreshCode();

    //获取本地存储登录信息，判断是否过期
    if (!this.validatenull(localStorage.getItem('loginInfo'))) {
      this.loginInfo = JSON.parse(localStorage.getItem('loginInfo'));
    }

    if (!this.validatenull(this.loginInfo)) {
      var time = this.loginInfo.time;
      var date = this.loginInfo.date;
      var dateNow = new Date().getTime();
      if ((parseInt(time) + parseInt(date)) < dateNow) { //过期
        this.isInvalid = true;
        this.rememberPwd = false;
        this.loginForm.username = null;
        this.loginForm.password = null;
        this.clearPwd();
      } else {
        this.isInvalid = false;
        this.rememberPwd = true;
        this.loginForm.username = this.loginInfo.username;
        this.loginForm.password = this.loginInfo.password;
      }
    } else {
      this.isInvalid = true;
    }

  },
  mounted() {
  },
  computed: {
    ...mapGetters(["tagWel"])
  },
  props: {
    isPad: Boolean
  },
  methods: {
    refreshCode() {
      this.loginForm.code = ''
      this.loginForm.randomStr = randomLenNum(this.code.len, true)
      this.code.type === 'text'
        ? (this.code.value = randomLenNum(this.code.len))
        : (this.code.src = `${this.codeUrl}?randomStr=${this.loginForm.randomStr}`)
    },
    showPassword() {
      this.passwordType == ''
        ? (this.passwordType = 'password')
        : (this.passwordType = '')
    },
    handleLogin() {
      let _this = this;
      _this.$refs.loginForm.validate(valid => {
        if (valid) {
          if (_this.rememberPwd) {//记住密码
            if (_this.isInvalid) {//过期或者空的本地存储的时候才去保存新的
              _this.savePwd(_this.loginForm);
            }
          } else {
            _this.clearPwd();
          }
          //创建临时用户名本地存储，弱密码修改时用到该用户名
          localStorage.setItem("tempUserName", this.loginForm.username);
          _this.$store.dispatch("LoginByUsername", _this.loginForm).then((res) => {
            // console.log(res)
            localStorage.setItem("parkList", res.data.parkList);
            // code判断时注意，成功登陆进来的返回内容没有code
            // 新增判断密码是否符合规范
            if (res.status === 200 && res.data.access_token) {
              if (_this.checkPassword(_this.loginForm.password)) {
                _this.$router.push({ path: '/info' })
                this.$message.error('您的密码不符合规范，请及时修改！')
                return
              }
            }
            if (_this.isPad) {
              _this.$router.push({ path: '/platform/dormitory/pad' });
            } else {
              _this.$router.push({ path: _this.tagWel.value });
            }
          }).catch((err) => {
            _this.refreshCode()
            if (err.message.indexOf("isStrongPwd") == -1) {
              this.$message.error(err.message);
            }
          })
        }
      });
    },
    checkPassword(password) {
      if (password.length < 8) {
        return true; // 密码不少于 8 位
      }
      if (!/^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[\W_]).+$/.test(password)) {
        return true; // 包含数字、大写字母和小写字母
      }
      if (/([a-zA-Z0-9])\1{2,}/.test(password)) {
        return true; // 不包含三个及以上相同数字或字母
      }
      if (/([a-zA-Z0-9]{3,})\1{1,}/.test(password)) {
        return true; // 不包含三个及以上连续数字或字母
      }
      return false;
    },
    savePwd(form) {//重新保存登录信息
      var obj = new Object();
      obj.username = form.username;
      obj.password = form.password;
      obj.time = 1000 * 60 * 60 * 24 * 5; //有效期 5天
      obj.date = new Date().getTime(); //当前时间戳
      var objString = JSON.stringify(obj);

      localStorage.setItem("loginInfo", objString);

    },
    clearPwd() { //清除登录信息
      localStorage.setItem("loginInfo", '');
      this.loginInfo = {};
    }
  }
};
</script>

<style></style>
