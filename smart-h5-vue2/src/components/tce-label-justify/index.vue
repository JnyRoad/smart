<template>
  <div class="tce-label-justify">
    <div class="tce-label-justify-inner">
      <div v-for="(item,index) of getValue" :key="index">{{item}}</div>
    </div>
    <div class="tce-label-justify-fill">
      <template v-if="fill">{{fill}}</template>
      <template v-else-if="len">{{getLine}}</template>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {}
  },
  props: {
    label: {
      type: String,
      value: ''
    },
    fill: {
      type: String,
      value: ''
    },
    len: {
      type: Number,
      value: 0
    }
  },
  computed: {
    getValue() {
      return [...this.label]
    },
    getLine() {
      return new Array(this.len).fill('　').join('')
    }
  }
}
</script>

<style lang="scss" scoped>
.tce-label-justify {
  &-inner {
    display: flex;
    justify-content: space-between;
    position: relative;
    z-index: 1;
  }

  &-fill {
    display: block;
    opacity: 0;
    height: 0px;
    overflow: hidden;
    position: relative;
    z-index: 0;
  }
}
</style>
