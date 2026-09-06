<template>
  <div class="login-container"  @keyup.enter.native="handleLogin" :class="{'padLoginCss': isPad}">
    <div class="login-top">
      <div class="login-weaper">
        <div class="login-left">
          <div><img src="/img/yuTongLogo.png"/></div>
          <div><img src="/img/slogan.png"/></div>
        </div>
        <div class="login-border">
          <div class="login-main">
            <h1 >TCE-云视智慧园区</h1>
            <h4 class="login-title">
              欢迎登录
            </h4>
            <userLogin v-if="activeName==='user'" :isPad="isPad"></userLogin>
          </div>
        </div>
      </div>
    </div>
    <div class="login-btm">
      <div class="login-inner clear">
        <div class="f-img">
          <div class="qrcode">
            <img src="/img/qrcode.png"/>
          </div>
          <p>裕同科技公众号</p>
        </div>
        <div class="f-info">
          <p>官网地址<span>http://www.szyuto.com/cn</span></p>
          <p>企业地址<span>深圳市宝安区石岩街道石环路1号</span></p>
          <p>联系电话<span>+86 755 33873999（总机）</span><span>+86 755 33873999-88708（业务咨询）</span></p>
          <p class="copy-r">©2019 深圳市裕同包装科技股份有限公司版权所有 粤ICP备16031472号-7</p>
        </div>
      </div>
    </div>
    <top-color v-show="false"></top-color>
  </div>
</template>
<script>
  import { printLoginDestination } from '@/api/platform/print/handoff';
  import userLogin from "./userlogin";
  import {mapGetters} from "vuex";
  import {getStore, setStore} from "@/util/store";
  import {dateFormat} from "@/util/date";
  import {validatenull} from "@/util/validate";
  import topColor from "@/page/index/top/top-color";

  export default {
    name: "login",
    components: {
      userLogin,
      topColor
    },
    data() {
      return {
        time: "",
        isPad: false,
        activeName: "user",
        socialForm: {}
      };
    },
    watch: {
      $route: {
        handler() {
          const params = this.$route.query
          if (validatenull(params.state) && validatenull(params.code)) return

          this.socialForm.state = params.state
          this.socialForm.code = params.code
          const loading = this.$loading({
            lock: true,
            text: `登录中,请稍后。。。`,
            spinner: 'el-icon-loading'
          })
          this.$store.dispatch('LoginBySocial', this.socialForm).then(
            () => {
              loading.close()
              this.$router.push(printLoginDestination(this.$route.query) || {path: this.tagWel.value});
            }).catch(() => {
            loading.close()
          })

        },
        immediate: true
      }
    },
    created() {
      this.isPad = this.$route.meta.isPad
      this.getTime();
      setInterval(() => {
        this.getTime();
      }, 1000);
    },
    mounted() {
    },
    computed: {
      ...mapGetters(["website",'tagWel'])
    },
    props: [],
    methods: {
      getTime() {
        this.time = dateFormat(new Date());
      }
    }
  };
</script>

<style lang="scss">
  .hide{
    width: 0;
    position: absolute
  }
  .clear::after{
    content: '.';
    display: block;
    visibility: hidden;
    height: 0;
    clear: both;
  }
  .login-container {
    background: #081740;
    position: relative;
    width: 100%;
    height: 100%;
    margin: 0 auto;
    padding-bottom: 200px;
    // animation: animate-cloud 20s linear infinite;
  }
  .login-top{
    position: relative;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    background: #081740 url("/img/login_stars.png") left bottom no-repeat;
    background-size: 100% 100%;
  }
  .login-weaper {
    margin: 0 auto;
    width: 85%;
    display: flex;
    // box-shadow: -4px 5px 10px rgba(0, 0, 0, 0.4);
  }

  .login-left,
  .login-border {
    position: relative;
    min-height: 500px;
  }

  .login-left {
    justify-content: center;
    flex-direction: column;
    background-size: 80% auto;
    color: #fff;
    width: 70%;
    position: relative;
  }

  .login-border {
    align-items: center;
    display: flex;
    border-left: none;
    border-radius: 5px;
    background-color: #fff;
    width: 30%;
    min-width: 450px;
    float: left;
    box-sizing: border-box;
    margin-top: 50px;
  }

  .login-main {
    margin: 0 auto;
    width: 65%;
    box-sizing: border-box;
  }

 .login-main > h1 {
    text-align: center;
    margin-top: 0;
    margin-bottom: 0.8em;
  }
  .login-main > h3 {
    margin-bottom: 20px;
  }

  .login-main > p {
    color: #76838f;
  }

  .login-title {
    color: #333;
    margin-bottom: 40px;
    font-weight: 500;
    font-size: 22px;
    text-align: center;
    letter-spacing: 4px;
  }

  .login-select {
    input {
      color: #333;
      font-size: 18px;
      font-weight: 400;
      border: none;
      text-align: center;
    }
  }

  .login-menu {
    margin-top: 40px;
    width: 100%;
    text-align: center;

    a {
      color: #999;
      font-size: 12px;
      margin: 0px 8px;
    }
  }

  .login-submit {
    width: 100%;
    height: 45px;
    font-size: 18px;
    letter-spacing: 2px;
    font-weight: 300;
    cursor: pointer;
    margin-top: 30px;
    font-family: "neo";
    transition: 0.25s;
  }

  .login-form {
    margin: 10px 0;

    i {
      color: #333;
    }

    .el-form-item__content {
      width: 100%;
    }

    .el-form-item {
      margin-bottom: 12px;
    }

    .el-input {
      input {
        padding-bottom: 10px;
        text-indent: 5px;
        background: transparent;
        border: none;
        border-radius: 0;
        color: #333;
        border-bottom: 1px solid rgb(235, 237, 242);
      }

      .el-input__prefix {
        i {
          padding: 0 5px;
          font-size: 16px !important;
        }
      }
    }
  }

  .login-code {
    display: flex;
    align-items: center;
    justify-content: space-around;
    margin: 0 0 0 10px;
  }

  .login-code-img {
    margin-top: 2px;
    width: 100px;
    height: 38px;
    background-color: #fdfdfd;
    border: 1px solid #f0f0f0;
    color: #333;
    font-size: 14px;
    font-weight: bold;
    letter-spacing: 5px;
    line-height: 38px;
    text-indent: 5px;
    text-align: center;
  }
  .login-btm{
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    min-height: 200px;
    background:#F6F6F7 url('/img/login_btm.png') left bottom no-repeat;
    background-size: 100% 100%;
    display: flex;
    align-items: center;
  }
  .login-inner{
    text-align: center;
    margin: 0 auto;
  }
  .f-img{
    display: inline-block;
    color: #999;
    font-size: 12px;
    margin-right: 60px;
  }
  .qrcode{
    width: 100px;
    height: 100px;
    margin-bottom: 15px;
    background: #EFEFEF;
  }
  .qrcode img{
    width: 100%;
    height: 100%;
  }
  .f-info{
    display: inline-block;
    text-align: left;
  }
  .f-info p{
    margin-bottom: 10px;
  }
  .f-info span{
    padding-left: 20px;

  }
  .copy-r{
    color: #999;
    font-size: 12px;
    padding-top: 20px;
  }
  .login-main{
    .el-form-item .el-input__inner,
    .el-form-item .el-input__inner:focus{
      border-bottom-color: #ed6d00;
    }
  }

  @media screen and (max-width: 1366px) {
    .login-left, .login-border{
      min-height: 420px;
    }
    .login-border{
      min-width: 410px;
    }
  }
  .padLoginCss{
    .login-btm{
      display: none;
    }
  }
</style>
