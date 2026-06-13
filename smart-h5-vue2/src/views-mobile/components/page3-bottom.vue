<!--
- @name 底部固定容器
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-12
-->

<template>
  <div class="page3-bottom">
    <slot name="inner"></slot>
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
    }
  },
  watch: {
    list: {
      handler() {},
      immediate: true
    }
  },
  methods: {
    toPage(item) {
      this.$router.push({
        path: item.pageSrc
      })
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
  .page3-bottom{
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 99;
    height: rem(160);
    height: calc(#{rem(160)} + constant(safe-area-inset-bottom));
    height: calc(#{rem(160)} + env(safe-area-inset-bottom));
    padding-bottom: constant(safe-area-inset-bottom);
    padding-bottom: env(safe-area-inset-bottom);
    box-sizing: border-box;
    background: #fff;
    box-shadow: -2px 0 11px rgba(0, 0, 0, 0.2 );
  }
</style>
