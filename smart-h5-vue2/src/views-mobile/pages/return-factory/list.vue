<!--
- @name 返厂确认-列表
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-09-09
-->

<template>
  <div class="page3">
    <page3Tab :list="tabList" :curIndex="curTabIndex" @doApply="noApproval" @checkList="approvalDone"></page3Tab>
    <div class="page3-list">
      <cube-scroll ref="scroll" :options="options" :data="dataList" @pulling-down="onPullingDown" @pulling-up="onPullingUp">
        <div class="item" @click="toDetail(item)" v-for="(item, index) in dataList" :key="index">
          <div class="item-title">
            <div class="item-title_left">{{ item.name }}提交的放行条</div>
            <div class="item-title_right">
              <div class="status-tag status-tag--gray">{{ item.backStatus }}</div>
            </div>
          </div>
          <div class="item-info">
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="申请部门:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">{{ item.deptName }}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="放行事项:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">{{ item.releaseItemDesc }}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="申请时间:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">{{ item.createTime }}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="OA节点:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis">{{ item.oaNode || '-' }}</div>
            </div>
          </div>
        </div>
        <tce-empty v-if="!dataList || dataList.length === 0"></tce-empty>
      </cube-scroll>
    </div>
    <searchByStaff ref="searchByStaff" :cutomHeader="true" @doAnther="doAntherStaff" @successCustom="successCustom"></searchByStaff>
    <plateNumber ref="plateNumber" v-model="licensePlate" :cutomHeader="true" @clear="clearPlate" @doAnther="doAntherCar"></plateNumber>
    <!-- v-if="dataList && dataList.length>0" -->
    <page3Bottom v-if="dataList && dataList.length > 0">
      <div slot="inner" class="btn-inner">
        <!-- <button @click="scan" class="tce-button tce-button--textbtn"><span class="tce-icons tce-icons--scan"></span>扫一扫</button> -->
        <button @click="search" class="tce-button tce-button--textbtn"><span class="tce-icons tce-icons--search"></span>搜 索</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Tab from '@/views-mobile/components/page3-tab'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import searchByStaff from '../backLog/good-release-live/components/search-by-staff.vue'
import plateNumber from '@/components/plateNumber/plateNumber'
import { getBackList } from '@/services/returnFactory'
import store from '@/store'

export default {
  components: {
    page3Tab,
    page3Bottom,
    searchByStaff,
    plateNumber
  },
  data() {
    return {
      curTabIndex: 0,
      licensePlate: '',
      tabList: [
        {
          label: '待确认的',
          value: 0
        },
        {
          label: '我确认的',
          value: 1
        }
      ],
      dataList: [],
      current: 1,
      pages: 1,
      options: {
        pullDownRefresh: {
          threshold: 60,
          stop: 44,
          stopTime: 1000,
          txt: '更新成功'
        },
        pullUpLoad: true
      },
      baseinfo: {}
    }
  },
  computed: {},
  props: {},
  watch: {
    licensePlate(val) {
      if (val) {
        this.getList(this.curTabIndex, {
          licensePlate: val
        })
      }
    }
  },
  methods: {
    toDetail(item) {
      this.$router.push({
        path: '/xuchang/returnFactory/detail',
        query: {
          id: item.id
        }
      })
    },
    successCustom(obj) {
      if (obj.releaseItem) {
        obj.releaseItem = obj.releaseItem.value
      }
      this.dataList = []
      this.getList(this.curTabIndex, obj)
    },
    /**
     * 获取列表
     */
    async getList(type, params = {}) {
      this.$loading.show()
      const obj = {
        approvalStatus: type, // 0待审批，1已审批
        current: this.current,
        size: 10
      }
      // if (type == 1) {
      //   obj['badge'] = this.baseInfo.employeeBadge
      // }
      const res = await getBackList(
        Object.assign(obj, params)
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
     * 扫一扫
     */
    scan() {},
    /**
     * 搜索
     */
    search() {
      // console.log('licensePlate', this.licensePlate)
      this.$refs.searchByStaff && this.$refs.searchByStaff.start()
      // if (this.searchIndex === 1) {
      //   this.$refs.searchByStaff && this.$refs.searchByStaff.start()
      // } else {
      //   this.$refs.plateNumber && this.$refs.plateNumber.start()
      // }
    },
    doAntherStaff() {
      this.searchIndex = 2
      this.$refs.plateNumber && this.$refs.plateNumber.start()
    },
    doAntherCar() {
      this.searchIndex = 1
      this.$refs.searchByStaff && this.$refs.searchByStaff.start()
    },
    /**
     * 清空车牌
     */
    clearPlate() {
      this.licensePlate = ''
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
}
</style>
