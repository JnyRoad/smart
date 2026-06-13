<!--
- @name 帮助中心
-->

<template>
  <div class="dorm">
    <div class="d-title" >
      <div class="d-left">
        <div>
          <div class="f1 -ellipsis">帮助中心</div>
        </div>
      </div>
      <div class="d-right">
        <tce-image :src="titleInfo.imgSrc" :width="0" :height="0"></tce-image>
      </div>
    </div>
    <div class="d-list">
      <cube-scroll ref="scroll" :options="options" :data="dataList" @pulling-down="onPullingDown" @pulling-up="onPullingUp">
        <div class="d-item" @click="toPage(item)" v-for="(item, index) in dataList" :key="index">
          <div class="left">
            <div>
              <div class="title -ellipsis">{{ item.questionTitle }}</div>
            </div>
          </div>
          <span class="-arrow"></span>
        </div>
        <tce-empty v-if="!dataList || dataList.length === 0"></tce-empty>
      </cube-scroll>
    </div>
  </div>
</template>

<script>
import bg from './img/bg.png'
import { getHelpList } from '@/services/help'
// import page2Titel from '@/views-mobile/components/page2-title'

export default {
  components: {
    // page2Titel
  },
  data() {
    return {
      titleInfo: {
        title: '帮助中心',
        imgSrc: bg
      },
      options: {
        pullDownRefresh: {
          threshold: 60,
          stop: 44,
          stopTime: 1000,
          txt: '更新成功'
        },
        pullUpLoad: true
      },
      dataList: [],
      current: 1
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    toPage(item) {
      this.$router.push({
        path: '/xuchang/help/detail',
        query: {
          id: item.questionId
        }
      })
    },
    /**
     * 获取列表
     */
    async getList() {
      this.$loading.show()
      const res = await getHelpList({
        current: this.current,
        size: 10
      })
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.dataList = this.dataList.concat(res.data.records)
        this.pages = res.data.pages
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    // 下拉刷新
    onPullingDown() {
      // debugger
      this.current = 1
      this.dataList = []
      this.getList()
    }, // 上拉加载
    onPullingUp() {
      // debugger
      this.clearPullingUp && clearTimeout(this.clearPullingUp)
      this.clearPullingUp = setTimeout(() => {
        if (this.current < this.pages) {
          this.current++
          this.getList()
        } else {
          this.$refs.scroll.forceUpdate(false, true)
        }
      }, 1000)
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.getList()
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
.dorm {
  background: $TCE-Background-Grey;
  width: 100%;
  height: 100%;
}
.d-title{
    height: 140px;
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
.d-list {
  height: calc(100% - 160px);
  padding: rem(30) rem(20);
  .d-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 0 0 rem(20);
    height: rem(100);
    background: #f1f3f5;
    border-radius: 5px;
    padding: 0 rem(30);
    background: #fff;
    overflow: hidden;
  }
  .left {
    display: flex;
    align-items: center;
    overflow: hidden;
    > div {
      overflow: hidden;
    }
  }
  .title {
    color: #333;
    font-size: 16px;
  }
  .tip {
    color: #999;
    font-size: 12px;
  }
  .-arrow {
    display: inline-block;
    width: rem(40);
  }
}
</style>
