<!--
- @name 待我审批-宿舍报修
-->

<template>
  <div class="page3">
    <page3Tab :list="tabList" :curIndex="curTabIndex" @doApply="noApproval" @checkList="approvalDone"></page3Tab>
    <div class="page3-list">
      <cube-scroll ref="scroll" :options="options" :data="dataList" @pulling-down="onPullingDown" @pulling-up="onPullingUp">
        <div class="item" @click="toDetail(item)" v-for="(item, index) in dataList" :key="index">
          <div class="item-title">
            <div class="item-title_left">{{item.approveName}}</div>
            <div class="item-title_right">
              <div class="status-tag status-tag--gray">{{item.statusDesc}}</div>
            </div>
          </div>
          <div class="item-info">
             <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="维修区域:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis" style="text-align: right;">{{item.rangeTypeDesc}}</div>
            </div>
             <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="维修类别:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis" style="text-align: right;">{{item.repairTypeDesc}}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="所在楼栋:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis" style="text-align: right;">{{item.dormitoryName}}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="故障描述:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis" style="text-align: right;">{{item.faultDesc}}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="申请时间:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis" style="text-align: right;">{{item.createTime}}</div>
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
// import page3Bottom from '@/views-mobile/components/page3-bottom'
import { getDormRepairsList } from '@/services/backLog'
import store from '@/store'
export default {
  components: {
    page3Tab
    // page3Bottom,
  },
  data() {
    return {
      curTabIndex: 0,
      tabList: [
        {
          label: '待我审批的',
          value: 0
        },
        {
          label: '我审批的',
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
      baseinfo: {},
      resCode: '',
      clearPullingUp: null,
      testId: null
    }
  },
  computed: {},
  props: {},
  watch: {
  },
  methods: {
    returnColor(s) {
      // switch (s) {
      //   case '1':
      //     return 'tag-green'
      //     break
      //   case '2':
      //     return 'tag-red'
      //     break
      // }

      const colorMap = {
        '1': 'tag-green',
        '2': 'tag-red'
      }
      return colorMap[s] || 'tag-unknown' // 添加默认返回值，以防传入的s不在映射中
    },
    toDetail(item) {
      const _this = this
      this.$router.push({
        path: '/xuchang/backLog/dormRepairs/detail',
        query: {
          id: item.approveId,
          curTabIndex: _this.curTabIndex
        }
      })
    },
    /**
     * 获取列表
     */
    async getList(type, params = {}) {
      const obj = {
        recordType: 5,
        recordState: type,
        current: this.current,
        size: 10
      }
      this.$loading.show()
      const res = await getDormRepairsList(Object.assign(obj, params))
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
      this.getList(this.curTabIndex)
    }, // 上拉加载
    onPullingUp() {
      // debugger
      this.clearPullingUp && clearTimeout(this.clearPullingUp)
      this.clearPullingUp = setTimeout(() => {
        if (this.current < this.pages) {
          this.current++
          this.getList(this.curTabIndex)
        } else {
          this.$refs.scroll.forceUpdate(false, true)
        }
      }, 1000)
    },
    /**
     * 待审批
     */
    noApproval() {
      this.curTabIndex = 0
      this.current = 1
      this.dataList = []
      this.getList(this.curTabIndex)
    },
    /**
     * 已审批
     */
    approvalDone() {
      this.curTabIndex = 1
      this.current = 1
      this.dataList = []
      this.getList(this.curTabIndex)
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.baseInfo = store.getters.userInfoBase
    if (this.$route.query.curTabIndex === 1) {
      this.curTabIndex = 1
    } else {
      this.curTabIndex = 0
    }
    this.getList(this.curTabIndex)
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
.page3 {
  background: $TCE-Background-Grey;
  width: 100%;
  height: 100%;
  padding: rem(120) rem(20) rem(180);
  .page3-list {
    height: 100%;
    .item {
      background: #fff;
      border-radius: 5px;
      padding: rem(30) rem(30) rem(20);
      margin-bottom: rem(20);
      .item-title {
        display: flex;
        justify-content: space-between;
        margin-bottom: rem(30);
        &_left {
          font-size: 16px;
          font-weight: bold;
        }
      }
      .item-info {
        &_row {
          display: flex;
          margin-bottom: rem(15);
        }
        &_label {
          color: #999;
          width: rem(120);
        }
        &_value {
          flex: 1;
          padding-left: rem(20);
        }
      }
    }
  }
  .page3-bottom {
    background: $TCE-Background-Grey;
    box-shadow: none;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .btn-inner {
    width: 90%;
    padding: 0 rem(52);
    border-radius: rem(50);
    background: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .tce-icons {
    vertical-align: middle;
    margin: -3px rem(10px) 0 0;
  }
  .tag-green {
    color: #07c160;
  }
  .tag-red {
    color: red;
  }
  .tag-unknown {
    color: #000;
  }
  .item-info_label {
    min-width: 70px;
  }
}
</style>
