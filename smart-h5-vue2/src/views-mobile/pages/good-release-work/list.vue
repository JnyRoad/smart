<!--
- @name 物品放行-办公区-查看数据
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-26
-->

<template>
  <div class="page3">
    <page3Tab :list="tabList" :curIndex="1" @doApply="doApply" @checkList="checkList"></page3Tab>
    <div class="page3-list">
      <cube-scroll ref="scroll" :options="options" :data="dataList" @pulling-down="onPullingDown" @pulling-up="onPullingUp">
        <div class="item" @click="toDetail(item)" v-for="(item, index) in dataList" :key="index">
          <div class="item-title">
            <div class="item-title_left">{{item.name}}提交的放行条</div>
            <div class="item-title_right">
              <div class="status-tag status-tag--default">{{item.oaNode}}</div>
            </div>
          </div>
          <div class="item-info">
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="申请部门:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">{{item.compName}}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="放行事项:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">{{item.releaseItemDesc}}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="申请时间:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">{{item.createTime}}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="确认状态:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">{{item.backStatus}}</div>
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
import { getPageApi } from '@/services/goodRreleaseOffice'
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
    toDetail(item) {
      this.$router.push({
        path: '/xuchang/goodReleaseWork/detail',
        query: {
          id: item.id
        }
      })
    },
    /**
     * 获取列表
     */
    async getList(type) {
      this.$loading.show()
      const res = await getPageApi(
        {
          badge: this.baseInfo.employeeBadge,
          type: 5, // 固定 4 办公区
          current: this.current,
          size: 10
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
    doApply() {
      this.$router.push({
        path: '/xuchang/goodReleaseWork'
      })
    },
    /**
     * 查看数据
     */
    checkList() {
      this.$router.push({
        path: '/xuchang/goodReleaseWork/list'
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
          }
        }
      }
    }
  }
  .item-info_label{
    min-width: 70px;
  }
</style>
