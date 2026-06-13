<template>
  <div class="login">
    <div class="title">
      <span class="f1">欢迎来到</span>
      <span class="f2">裕慧家园</span>
    </div>
    <div class="type">
      <span>绑定员工号</span>
    </div>
    <div class="login_pwd">
      <div class="info userName">
        <span class="icon"></span>
        <div class="input">
          <input placeholder="输入员工号" type="input" ref="username1" v-model="formData.badge" :autocomplete="false" key="badge"/>
        </div>
      </div>
       <div class="info userName">
        <span class="icon"></span>
        <div class="input">
          <input placeholder="输入身份证后六位" type="input" ref="lastCertNum" v-model="formData.lastCertNum" :autocomplete="false" key="lastCertNum"/>
        </div>
      </div>
      <button @click="submit" class="tce-button tce-button--primary is-round">绑定</button>
    </div>
  </div>
</template>

<script>
import { wechatBadge } from '@/services/login'
import { APPIP, PARKID } from '@/conf'
export default {
  components: {
  },
  data() {
    return {
      appid: 'wx5c0d26056102d41e',
      formData: {
        badge: null,
        lastCertNum: null
      },
      lhref: '',
      code: '',
      isGet: false,
      resCode: ''
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    wxlogin () {
      let url = encodeURIComponent(`${window.location.origin}/#/xuchang/login/wechat/code`)
      window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${APPIP}&redirect_uri=${url}&response_type=code&scope=snsapi_base&state=123#wechat_redirect`
    },
    toBadge () {
      let url = encodeURIComponent(`${window.location.origin}/#/xuchang/login/logon_badge`)
      window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${APPIP}&redirect_uri=${url}&response_type=code&scope=snsapi_base&state=123#wechat_redirect`
    },
    getUrlParameter(name, url = window.location.href) {
      this.lhref = window.location.href
      var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)')
      var r = window.location.search.substr(1).match(reg)
      if (url) {
        if (url.indexOf('?') > -1) {
          url = url.substr(url.indexOf('?') + 1)
        }
        r = url.match(reg)
      }
      if (r != null) return r[2]
      return null
    },
    async submit() {
      const me = this
      this.code = this.getUrlParameter('code')
      // debugger
      if (this.formData.badge !== null && this.formData.lastCertNum !== null) {
        this.$loading.show()
        const res = await wechatBadge({
          parkId: PARKID,
          code: this.code,
          badge: this.formData.badge,
          lastCertNum: this.formData.lastCertNum
        })
        this.$loading.hide()
        if (res.code === 0) {
          this.wxlogin()
        } else {
          this.$tceMobile.toast(res.message)
          setTimeout(() => {
            me.toBadge()
          }, 2000)
        }
      }
    }
  },
  filters: {},
  /**
   * 生命周期 created
   */
  created() {},
  /**
   * 生命周期 mounted
   */
  mounted() {},
  /**
   * 生命周期 beforeDestroy
   */
  beforeDestroy() {}
}
</script>

<style lang="scss">
  .login{
    padding: rem(130) rem(50) 0;
    .title{
      font-size: 20px;
      .f1{
        color: #9eabc3;
      }
    }
    .type{
      margin-top: rem(100);
      font-size: 18px;
      color: #666;
      span{
        display: inline-block;
        padding-bottom: 5px;
        border-bottom: 2px solid transparent;
      }
      .cur_f{
        color: $TCE-Color;
        border-bottom: 2px solid $TCE-Color;
      }
      .f1{
        margin-right: rem(50);
      }
    }
    .info{
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: rem(100);
      border-radius: rem(50);
      background: #f3f5f8;
      // margin-top: rem(100);
      margin: rem(100) 0;
      padding: 0 rem(30);
      .icon{
        width: rem(50);
        height: rem(50);
        display: inline-block;
        background-repeat: no-repeat;
        background-size: 100% 100%;
        margin-right: rem(30);
      }
      .input{
        flex: 1;
        input{
          width: 100%;
          background: transparent;
        }
      }
    }
    .userName{
      .icon{
        background-image: url('./img/icon_userName.png');
      }
    }
    .pwd{
      .icon{
        background-image: url('./img/icon_pwd.png');
      }
    }
    .phone{
      .icon{
        background-image: url('./img/icon_phone.png');
      }
    }
    .code{
      .icon{
        background-image: url('./img/icon_code.png');
      }
    }
  }
</style>
