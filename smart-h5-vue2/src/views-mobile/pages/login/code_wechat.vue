<!--
- @name 微信授权
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-10
-->

<template>
  <div class="login">
    <!-- <div>
      lhref:{{lhref}}
    </div>
    <div style="margin-top: 20px;">
      code:{{code}}
    </div>
    <div style="margin-top: 20px;">
      resCode:{{resCode}}
    </div> -->
  </div>
</template>

<script>
import { getTokenByCode } from '@/services/login'
import http from '@/util/http'
import store from '@/store'
import { APPIP } from '@/conf'

export default {
  components: { },
  data() {
    return {
      lhref: '',
      code: '',
      isGet: false,
      resCode: ''
      // appid: 'wx5c0d26056102d41e',
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
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
    // 根据code获取工号
    async getTokenByCode() {
      const res = await getTokenByCode({
        code: this.code,
        // code: '031SgD1006bcBM1b6f200SFQNZ1SgD1a',
        type: 'F'
      })
      if (res && res.access_token) {
        this.isGet = true
        this.resCode = res
        store.commit('SET_ACCESS_TOKEN', this.resCode.access_token)
        store.commit('SET_EXPIRES_IN', this.resCode.expires_in)
        store.commit('SET_REFRESH_TOKEN', this.resCode.refresh_token)
        http.refreshAuthorizationHeader(this.resCode.access_token)
        this.$router.push({
          path: '/xuchang/home'
        })
      } else if (res.code === 1 && (res.data === '账号未绑定工号，请先绑定' || res.data === '员工状态异常')) {
        this.toBadge()
      } else {
        this.$tceMobile.toast(res.message)
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.code = this.getUrlParameter('code')
    if (this.code) {
      // 传给后端 获取token
      const thisToken = store.getters.access_token
      if (thisToken && thisToken !== 'invalid_token') {
        http.refreshAuthorizationHeader(thisToken)
        this.$router.push({
          path: '/xuchang/home'
        })
      } else {
        http.refreshAuthorizationHeader('c21hcnQ6c21hcnQ=', true)
        this.getTokenByCode()
      }
    }
  },
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
      margin-top: rem(50);
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
