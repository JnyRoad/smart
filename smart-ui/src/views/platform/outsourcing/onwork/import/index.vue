<!--
- @name 导入文件
- @author yang.chuan <yang.chuan@bjtce.com>
- @date 2020-10-12
-->

<template>
  <div class="container">
    <step1 @next="step = 2" v-show="step === 1" v-model="dataList"></step1>
    <step2 @back="step = 1" v-if="step === 2" :dataList="dataList" @complete="complete"></step2>
  </div>
</template>

<script>
import step1 from './step1'
import step2 from './step2'
export default {
  components: {
    step1,
    step2
  },
  data() {
    return {
      step: 1,
      dataList: []
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    complete() {
      this.$emit('complete')
    }
  },
  filters: {},
  /**
   * 生命周期 created
   */
  created() { },
  /**
   * 生命周期 mounted
   */
  mounted() { },
  /**
   * 生命周期 beforeDestroy
   */
  beforeDestroy() { }
}
</script>

<style lang="scss" scoped>
.container {
  height: 500px;
  ::v-deep {
    .el-scrollbar {
      height: 100%;
    }
  }
}
</style>
