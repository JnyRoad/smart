<!--
- @name 二级列表页面-列表
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-12
-->

<template>
  <div class="d-list">
    <div class="d-item" @click="toPage(item)" v-for="(item, index) in list" :key="index">
      <div class="left">
        <div class="img">
          <tce-image :src="item.imgSrc" :width="0" :height="0"></tce-image>
        </div>
        <div>
          <div class="title -ellipsis">{{item.title}}</div>
          <div class="tip -ellipsis" v-if="item.tip">{{item.tip}}</div>
        </div>
      </div>
      <span class="-arrow"></span>
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
  .d-list{
    padding: rem(30) rem(20);
    .d-item{
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 0 0 rem(20);
      height: rem(150);
      background: #f1f3f5;
      border-radius: 5px;
      padding: 0 rem(30);
      background: #fff;
      overflow: hidden;
    }
    .left{
      display: flex;
      align-items: center;
      overflow: hidden;
      >div{
        overflow: hidden;
      }
    }
    .img{
      position: relative;
      width: rem(85);
      height: rem(85);
      flex: none;
      display: inline-block;
      margin-right: rem(30);
    }
    .title{
      color: #333;
      font-size: 16px;
      margin: 0 0 rem(20) 0;
    }
    .tip{
      color: #999;
      font-size: 12px;
    }
    .-arrow{
      display: inline-block;
      width: rem(40);
    }
  }
</style>
