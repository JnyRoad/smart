<!--
- @name 二级列表页面-标题
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-12
-->

<template>
  <div class="d-title" >
    <div class="d-left">
      <div>
        <div class="f1 -ellipsis">{{info.title}}</div>
        <!-- <div class="f2 -ellipsis">园区测试</div> -->
      </div>
    </div>
    <div class="d-right">
      <tce-image :src="info.imgSrc" :width="0" :height="0"></tce-image>
    </div>
  </div>
</template>

<script>
export default {
  components: {},
  data() {
    return {
    }
  },
  computed: {},
  props: {
    info: {
      type: Object,
      default: function() {
        return {}
      }
    }
  },
  watch: {
    info: {
      handler() {},
      immediate: true
    }
  },
  methods: {
  },
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
  .d-title{
    height: rem(240);
    background: #fff;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    position: relative;
    padding: 0 rem(60);
    .d-left{
      position: absolute;
      top: 0;
      bottom: 0;
      left: rem(60);
      display: flex;
      align-items: center;
      width: 50%;
      overflow: hidden;
      >div{
        overflow: hidden;
      }
      .f1{
        font-size: 20px;
        font-weight: bold;
        margin-bottom: rem(30);
      }
      .f2{
        color: #999;
      }
    }
    .d-right{
      width: rem(267);
      height: rem(167);
    }
  }
</style>
