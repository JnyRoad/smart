<!--
- @name 登录-密码
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-10
-->

<template>
  <div class="login">
    <div class="title">
      <span class="f1">欢迎来到</span>
      <span class="f2">裕慧家园</span>
    </div>
    <div class="type">
      <span class="f1" :class="{'cur_f':type==='code'}" @click="changeType('code')">短信登录</span>
      <span class="f2" :class="{'cur_f':type==='pwd'}" @click="changeType('pwd')">密码登录</span>
    </div>
    <pwd v-if="type==='pwd'"></pwd>
    <msgCode v-if="type==='code'"></msgCode>
  </div>
</template>

<script>
import pwd from './components/pwd'
import msgCode from './components/msgCode'

export default {
  components: {
    pwd,
    msgCode
  },
  data() {
    return {
      type: 'code'
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    changeType(type) {
      this.type = type
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
