<!--
- @name 登录-验证码
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-10
-->

<template>
  <div class="login_code">
    <div class="info phone">
      <span class="icon"></span>
      <div class="input">
        <input placeholder="点击输入手机号" type="input" ref="phone" v-model="formData.phone" :autocomplete="false" key="phone1"/>
      </div>
    </div>
    <div class="info code">
      <span class="icon"></span>
      <div class="input">
        <input placeholder="点击输入验证码" type="input" ref="code" v-model="formData.code" :autocomplete="false" key="code1"/>
      </div>
      <div>
        <identifyingCode class="identifying-code" :phone="formData.phone" :duration="120"></identifyingCode>
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
import identifyingCode from '@/components/identifying-code/index.vue'
import store from '@/store'
import http from '@/util/http'

export default {
  components: {
    identifyingCode
  },
  data() {
    return {
      isRead: 1,
      formData: {
        phone: '',
        code: ''
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
      store.commit('SET_ACCESS_TOKEN', this.formData.phone)
      http.refreshAuthorizationHeader(this.formData.phone)
      // store.commit('SET_ACCESS_TOKEN', 'c21hcnQ6c21hcnQ')
      // http.refreshAuthorizationHeader('c21hcnQ6c21hcnQ=', true)
      this.$router.push({
        path: '/xuchang/home'
      })
      // await this.$refs.tceForm.verification()
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
  .identifying-code{
    ::v-deep {
      > div {
        color: $TCE-Color;
      }
    }
  }
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
