<!--
- @name 首页-切换园区
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-11
-->

<template>
  <div class="shadow" v-if="currVisible" @click="close">
    <div class="cont" @click.stop="test">
      <div class="top">
        <div class="title">选择园区</div>
        <div class="btn" @click="done">确定</div>
      </div>
      <div class="bottom">
        <div class="list">
          <div class="item -ellipsis" :class="{'cur': curObj.id===item.id}" @click="selectItem(item)" v-for="(item, index) in list" :key="index">
            {{item.parkName}}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  components: {},
  data() {
    return {
      currVisible: false,
      curObj: {},
      list: [
        { parkName: '许昌园区', id: 1 },
        { parkName: '石岩园区', id: 2 },
        { parkName: '大岭山园区', id: 3 }
      ]
    }
  },
  computed: {},
  props: {
    curPark: {
      type: Object,
      default: function() {
        return {}
      }
    },
    visible: Boolean
  },
  watch: {
    curPark: {
      handler() {},
      immediate: true
    },
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      } else {
        if (this.curPark && this.curPark.id) {

        }
      }
    }
  },
  methods: {
    test() {},
    selectItem(item) {
      this.curObj = item
    },
    done() {
      if (this.curObj && this.curObj.id) {
        this.$emit('done', this.curObj)
        this.currVisible = false
      }
    },
    close() {
      this.currVisible = false
    },
    open() {
      this.currVisible = true
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
  .shadow{
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 2;
    background: rgba(0, 0, 0, 0.4);
    .cont{
      position: fixed;
      left: 0;
      right: 0;
      bottom: 0;
      height: rem(667);
      background: #f0f2f5;
      border-radius: 10px 10px 0 0;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      .top{
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0 rem(25);
        height: rem(100);
        background: #fff;
        margin-bottom: rem(10);
        .title{
          font-size: 18px;
          font-weight: bold;
        }
        .btn{
          color: $TCE-Color;
        }
      }
      .bottom{
        flex: 1;
        background: #fff;
        padding: rem(50) rem(15);
        .list{
          display: flex;
          flex-wrap: wrap;
        }
        .item{
          width: rem(220);
          height: rem(64);
          line-height: rem(64);
          text-align: center;
          border-radius: rem(32);
          background: #FAFAFA;
          border: 1px solid #DEDEDE;
          overflow: hidden;
          margin: 0 rem(10) rem(20);
          color: #666;
          padding: 0 rem(20);
        }
        .item.cur{
          color: $TCE-Color;
          background: #FFF7F1;
          border: 1px solid $TCE-Color;
        }
      }
    }
  }
</style>
