<!--
- @name 微信授权
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-10
-->

<template>
  <div class="login">

  </div>
</template>

<script>
import { APPIP } from '@/conf'
export default {
  components: { },
  data() {
    return {
      // appid: 'wx5c0d26056102d41e',
      urlTest: 'http://192.168.8.227:6062/#/xuchang/login/wechat/code'
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    wxlogin () {
      let url = encodeURIComponent(`${window.location.origin}/#/xuchang/login/wechat/code`)
      window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${APPIP}&redirect_uri=${url}&response_type=code&scope=snsapi_base&state=123#wechat_redirect`
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.wxlogin()
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
