<!--
- @name 访客预约 步骤栏
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-12
-->

<template>
  <div class="step-outer">
    <div class="step-inner">
      <div class="step-row1">
        <div class="dot-outer">
          <div class="dot">
            <span class="done"></span>
          </div>
        </div>
        <div class="dot-outer dot-outer2">
          <div class="dot">
            <span class="num" v-if="curIndex===1">2</span>
            <span class="done" v-else></span>
          </div>
        </div>
        <div class="dot-outer dot-outer3">
          <div class="dot">
            <span class="num" v-if="curIndex!==3">3</span>
            <span class="done" v-else></span>
          </div>
        </div>
      </div>
      <div class="step-row2">
        <div>被访信息</div>
        <div>访客信息</div>
        <div>提交信息</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  components: {},
  data() {
    return {}
  },
  computed: {},
  props: {
    curIndex: {
      type: Number,
      default: 1
    }
  },
  methods: {},
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
  .step-outer{
    position: fixed;
    left: 0;
    top: 0;
    right: 0;
    z-index: 11;
    display: flex;
    justify-content: center;
    align-items: center;
    text-align: center;
    height: rem(150);
    padding: 0 rem(30);
    background-image: linear-gradient(to top, #ed6d01, #ff9126);
    .step-inner{
      width: 100%;
    }
    .step-row1{
      display: flex;
      .dot-outer{
        position: relative;
        flex: 1;
        text-align: center;
        margin-bottom: rem(12);
        .dot{
          position: relative;
          z-index: 2;
          width: rem(45);
          height: rem(45);
          border-radius: 50%;
          margin: 0 auto;
          border: 2px solid #fff;
          // opacity: .8;
          text-align: center;
          color: #fff;
          line-height: rem(35);
          background: #F9861B;
          .done::after{
            content: '';
            width: rem(12);
            height: rem(23);
            border: 2px solid #fff;
            border-top: none;
            border-left: none;
            display: inline-block;
            transform: rotate(45deg);
          }
        }
        &::after{
          content: '';
          position: absolute;
          top: 50%;
          right: 0;
          width: 50%;
          display: inline-block;
          border-bottom: 1px solid #fff;
          opacity: .8;
        }
      }
      .dot-outer2{
        &::after{
          width: 100%;
          left: 0;
        }
      }
      .dot-outer3{
        &::after{
          left: 0;
          right: auto;
        }
      }
    }
    .step-row2{
      display: flex;
      >div{
        flex: 1;
        text-align: center;
        color: #fff;
        opacity: .8;
      }
    }
  }
</style>
