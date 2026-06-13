<template>
  <div class="search-box_inner" :class="{'folded': toFold}">
    <el-collapse-transition>
      <slot></slot>
    </el-collapse-transition>
    <div class="toggleSearch" v-if="!hideOprateBtn">
      <el-button v-if="toFold" type="text" icon="el-icon-arrow-down" @click="toFold = false">展开查询条件</el-button>
      <el-button v-else type="text" icon="el-icon-arrow-up" @click="toFold = true">收起查询条件</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'tce-search-bar',
  data() {
    return {
      toFold: true // 默认false,是收起的
    }
  },
  props: {
    hideOprateBtn: {
      type: Boolean,
      default: () => false // 默认false,是显示按钮的
    }
  },
  mounted() {
    this.hideOprateBtn ? this.toFold = false : this.toFold = true
  },
  methods: {}
}
</script>

<style lang="scss" scoped>
.search-box_inner {
  height: auto;
  overflow: hidden;
  position: relative;
  // border-bottom: 1px solid #EDEDED;
  .toggleSearch ::v-deep {
    position: absolute;
    bottom: 1px;
    right: 0;
    .el-button {
      padding: 0;
      font-size: 12px;
      [class^='tce-icon-'],
      [class*=' tce-icon-'] {
        font-size: 12px !important;
      }
    }
  }
  &.folded {
    height: 69px;
  }
}
</style>
