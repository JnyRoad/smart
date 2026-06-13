<!--
- @name 发起提交、查看数据 切换
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-12
-->

<template>
  <div class="tab-outer">
    <div class="tab-item" :class="{'cur': curIndex===item.value}" v-for="(item, index) in list" :key="index" @click="doClick(index)">
      {{item.label}}
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
    list: {
      type: Array,
      default: function() {
        return []
      }
    },
    curIndex: {
      type: Number,
      default: 0
    }
  },
  watch: {
    list: {
      handler() {},
      immediate: true
    }
  },
  methods: {
    doClick(index) {
      if (index === 0) {
        this.$emit('doApply')
      } else if (index === 1) {
        this.$emit('checkList')
      }
    }
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
  .tab-outer{
    position: fixed;
    left: 0;
    top: 0;
    right: 0;
    z-index: 2;
    display: flex;
    justify-content: space-around;
    align-items: center;
    text-align: center;
    height: rem(90);
    padding: 0 rem(30);
    background: #fff;
    font-size: 16px;
    font-weight: bold;
    .tab-item{
      position: relative;
      height: 100%;
      line-height: rem(90);
    }
    .tab-item::after{
      position: absolute;
      left: 5px;
      right: 5px;
      bottom: -1px;
      content: '';
      display: inline-block;
      height: 2px;
      background: transparent;
    }
    .cur{
      color: $TCE-Color;
      &::after{
        background: $TCE-Color;
      }
    }
  }
</style>
