<!--
- @name 登录-密码
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-10
-->

<template>
  <div class="login_pwd">
    <div class="info userName">
      <span class="icon"></span>
      <div class="input">
        <input placeholder="点击输入用户名" type="input" ref="username1" v-model="formData.username" :autocomplete="false" key="userName1"/>
      </div>
    </div>
    <div class="info pwd">
      <span class="icon"></span>
      <div class="input">
        <input placeholder="点击输入密码" type="password" ref="password1" v-model="formData.password" :autocomplete="false" key="password1"/>
      </div>
    </div>
    <div class="agree">
      <cube-radio v-model="isRead" :option="radioOption"> </cube-radio>
      <span>《裕慧家园用户协议及保密承诺》</span>
    </div>
    <button @click="submit" class="tce-button tce-button--primary is-round">登录</button>
  </div>
</template>

<script>
export default {
  components: {},
  data() {
    return {
      isRead: 1,
      formData: {
        userName: '',
        password: ''
      },
      radioOption: {
        label: '我已阅读并同意',
        value: true
      }
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    async submit() {

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

<style lang="scss" scoped>
  .agree ::v-deep{
    display: flex;
    align-items: center;
    font-size: 12px;
    margin: rem(40) 0;
    .cube-radio{
      padding: 0;
      .border-bottom-1px:after{
        display: none;
      }
    }
  }
</style>
