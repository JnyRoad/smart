<!--
- @name 待我审批-物品放行-审批结果
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-27
-->

<template>
  <div class="page3-detail">
    <div class="block block-code">
      <div class="codeImg">
      </div>
      <div class="tip"> 已发送成功，等待被访对象审批 </div>
      <div class="tip2">审批通过后，我们会以短信或公众号的方式通知您，请注意查看</div>
      <button @click="next" class="tce-button tce-button--textbtn">再预约一次</button>
    </div>
  </div>
</template>

<script>

export default {
  data() {
    return {}
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    next() {
      this.$router.push({
        path: '/xuchang/visitor'
      })
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    // 成功后清除缓存
    // localStorage.removeItem('visitorInfo')
    // localStorage.removeItem('oldAreaTypeList')
    // localStorage.removeItem('newAreaTypeList')
    // localStorage.removeItem('carList')
    // localStorage.removeItem('fellowInfo')
    // localStorage.removeItem('hostInfor')
    // localStorage.removeItem('visitorPhone')
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

<style lang="scss" scoped>
  .page3-detail{
    width: 100%;
    min-height: 100%;
    padding: rem(20) rem(20) rem(200) rem(20);
    .block-code{
      background: transparent;
      padding: rem(100) 0 rem(50) 0;
      .codeImg{
        width: rem(500);
        height: rem(400);
        margin: 0 auto;
        background: url('./img/result.jpg') no-repeat;
        background-size: 100% auto;
      }
      .tip{
        margin-top: rem(30);
        text-align: center;
        font-weight: bold;
        font-size: 16px;
      }
      .tip2{
        padding: rem(30) rem(180);
        text-align: center;
        color: #999;
        font-size: 12px;
        line-height: 20px;
      }
      .tce-button{
        color: $TCE-Color;
      }
    }
  }
</style>
