<!--
- @name 宿舍报修
-->

<template>
  <div class="page3">
    <page3Tab :list="tabList" :curIndex="1" @doApply="doApply" @checkList="checkList"></page3Tab>
    <div class="page3-list">
      <cube-scroll ref="scroll" :options="options" :data="dataList" @pulling-down="onPullingDown" @pulling-up="onPullingUp">
        <div class="item" @click="toDetail(item)" v-for="(item, index) in dataList" :key="index">
          <div class="item-title">
            <div class="item-title_left">{{item.name}}提交的退宿申请</div>
            <div class="item-title_right">
              <div class="status-tag status-tag--gray" v-bind:class="returnColor(item.status)">{{item.statusDesc}}</div>
            </div>
          </div>
          <div class="item-info">
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="房间信息:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">
                <ul>
                  <li class="roomLi" v-for="(_item, index) in item.dorDetailStr" :key="index">
                    {{ _item }}
                  </li>
                </ul>
              </div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="退宿原因:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">{{item.quitReasonDesc}}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="预计离开时间:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">{{item.applyLeaveTime}}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="申请时间:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">{{item.createTime}}</div>
            </div>
          </div>
        </div>
        <tce-empty v-if="!dataList || dataList.length===0"></tce-empty>
      </cube-scroll>
    </div>
  </div>
</template>

<script>
import page3Tab from '@/views-mobile/components/page3-tab'
import { getExitList } from '@/services/dormExit'
import store from '@/store'

export default {
  components: {
    page3Tab
  },
  data() {
    return {
      tabList: [
        {
          label: '发起提交',
          value: 0
        },
        {
          label: '查看数据',
          value: 1
        }
      ],
      current: 1,
      pages: 1,
      dataList: [],
      options: {
        pullDownRefresh: {
          threshold: 60,
          stop: 44,
          stopTime: 1000,
          txt: '更新成功'
        },
        pullUpLoad: true
      },
      baseInfo: {}
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    /**
     * 获取列表
     */
    async getList() {
      this.$loading.show()
      const res = await getExitList(
        {
          current: this.current,
          size: 10,
          badge: this.baseInfo.employeeBadge
        }
      )
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.dataList = this.dataList.concat(res.data.records)
        this.pages = res.data.pages
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    toDetail(item) {
      this.$router.push({
        path: '/xuchang/dormExit/detail',
        query: {
          id: item.id
        }
      })
    },
    returnColor(s) {
      switch (s) {
        case 2:
          return 'tag-green'
        case 3:
          return 'tag-red'
      }
    },
    // 下拉刷新
    onPullingDown() {
      // debugger
      this.current = 1
      this.dataList = []
      this.getList()
    },
    // 上拉加载
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
    },
    /**
     * 发起提交
     */
    async doApply() {
      this.$router.push({
        path: '/xuchang/dormExit'
      })
    },
    /**
     * 查看数据
     */
    checkList() {
      this.$router.push({
        path: '/xuchang/dormExit/list'
      })
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.baseInfo = store.getters.userInfoBase
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
  .page3{
    background: $TCE-Background-Grey;
    width: 100%;
    height: 100%;
    padding: rem(120) rem(20) rem(20);
    .page3-list{
      height: 100%;
      .item{
        background: #fff;
        border-radius: 5px;
        padding: rem(30) rem(30) rem(20);
        margin-bottom: rem(20);
        .item-title{
          display: flex;
          justify-content: space-between;
          margin-bottom: rem(30);
          &_left{
            font-size: 16px;
            font-weight: bold;
          }
        }
        .item-info{
          &_row{
            display: flex;
            margin-bottom: rem(15);
          }
          &_label{
            color: #999;
            width: rem(120);
          }
          &_value{
            flex: 1;
            padding-left: rem(20);
            text-align: right;
          }
        }
        .roomLi{
          width: 100%;
          text-align: right;
        }
      }
    }
  }
  .item-info_label{
    min-width: 70px;
  }
  .tag-green {
    color: #07c160;
  }
  .tag-red {
    color: red;
  }
</style>
