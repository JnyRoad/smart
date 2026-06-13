<!--
- @name 待我审批-物品放行（生活区）-申请列表
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-26
-->

<template>
  <div class="page3">
    <page3Tab :list="tabList" :curIndex="curTabIndex" @doApply="noApproval" @checkList="approvalDone"></page3Tab>
    <div class="page3-list">
      <cube-scroll ref="scroll" :options="options" :data="dataList" @pulling-down="onPullingDown" @pulling-up="onPullingUp">
        <div class="item" @click="toDetail(item)" v-for="(item, index) in dataList" :key="index">
          <div class="item-title">
            <div class="item-title_left">{{ item.name }}提交的退宿申请</div>
            <div class="item-title_right">
              <div class="status-tag status-tag--gray" v-if="curTabIndex === 0">{{ item.statusDesc }}</div>
              <div class="status-tag status-tag--gray" v-if="curTabIndex === 1" v-bind:class="returnColor(item.status)">{{ item.statusDesc }}</div>
            </div>
          </div>
          <div class="item-info">
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="房间信息:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis" >
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
        <tce-empty v-if="!dataList || dataList.length === 0"></tce-empty>
      </cube-scroll>
    </div>
    <searchByStaff ref="searchByStaff" :cutomHeader="true" @doAnther="doAntherStaff" @successCustom="successCustom"></searchByStaff>
    <!-- 当前员工是否是保安  isSecurityGuard: 0 是， 1否 （是保安的时候才有搜索和扫一扫）-->
    <!-- v-if="dataList && dataList.length>0 && baseInfo.isSecurityGuard===0" -->
    <page3Bottom v-if="baseInfo.isSecurityGuard === 0">
      <div slot="inner" class="btn-inner">
        <!-- <cube-input v-model="testId"></cube-input>
        <button @click="onScan" class="tce-button tce-button--textbtn"><span class="tce-icons tce-icons--scan"></span>扫一扫</button> -->
        <button @click="search" class="tce-button tce-button--textbtn"><span class="tce-icons tce-icons--search"></span>搜 索</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Tab from '@/views-mobile/components/page3-tab'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import searchByStaff from './components/search-by-staff.vue'
import { getDormExitList, getWxSignature } from '@/services/backLog'
import { getDetailApi } from '@/services/goodRreleaseLive'
import store from '@/store'
import wx from 'weixin-js-sdk'
export default {
  components: {
    page3Tab,
    page3Bottom,
    searchByStaff
  },
  data() {
    return {
      curTabIndex: 0,
      searchIndex: 1, // 默认员工信息搜索， 2车牌号搜索
      licensePlate: '',
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
      parkInfo: {},
      resCode: '',
      clearPullingUp: null,
      testId: null
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
    returnColor(s) {
      switch (s) {
        case 1:
          return 'tag-green'
        case 3:
          return 'tag-red'
      }
    },
    toDetail(item) {
      const _this = this
      this.$router.push({
        path: '/xuchang/backLog/dormExit/detail',
        query: {
          id: item.id,
          curTabIndex: _this.curTabIndex
          // sort: item.sort
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
      const me = this
      const obj = {
        isSecurityGuard: me.baseInfo.isSecurityGuard,
        parkId: me.parkInfo.id,
        status: type, // 状态固定为待审批
        current: me.current,
        size: 10
      }
      this.$loading.show()
      const res = await getDormExitList(Object.assign(obj, params))
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.dataList = this.dataList.concat(res.data.records)
        this.pages = res.data.pages
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    /**
     * 扫一扫
     */
    onScan() {
      this.getCofig()
      const that = this
      wx.ready(function () {
        wx.checkJsApi({
          // 需要使用的JS接口列表，在这里只需要用到scanQRCode
          jsApiList: ['scanQRCode'],
          success: function (res1) {
            if (res1.checkResult.scanQRCode) {
              // 当scanQRCode可使用时
              wx.scanQRCode({
                needResult: 1, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
                scanType: ['qrCode', 'barCode'], // 可以指定扫二维码还是一维码，默认二者都有
                success: function (res2) {
                  let result = res2.resultStr
                  that.toScan(result)
                  // window.location.href = result;
                  // 也可以对扫描结果处理过之后再使用
                  // 比如可以这样使用：
                  // window.location.href = result.split('?')[0] + '/detail?' + result.split('?')[1]
                },
                error: function (response) {
                  that.$tceMobile.toast('扫码失败')
                }
              })
            }
          }
        })
      })
    },
    // onScan() {
    //   this.toScan(this.testId);
    // },
    async toScan(id) {
      const _this = this
      // console.log(_this.testId)
      _this.$loading.show()
      const res = await getDetailApi(id)
      _this.$loading.hide()
      if (res.code === 0 && res.data) {
        let pathStr = ''
        const at = res.data.articlesType
        const queryData = {
          id: id,
          curTabIndex: _this.curTabIndex
        }
        switch (at) {
          case 3:
            pathStr = '/xuchang/backLog/goodReleaseLive/detail'
            queryData['sort'] = 3
            break
          case 4:
            pathStr = '/xuchang/backLog/goodReleaseWork/detail'
            break
        }
        _this.$router.push({
          path: pathStr,
          query: queryData
        })
      } else {
        _this.$tceMobile.toast(res.message || '未查到审批信息')
      }
    },
    async getCofig() {
      let url = ''
      let ua = navigator.userAgent.toLowerCase()
      if (/iphone|ipad|ipod/.test(ua)) {
        url = window.location.href.split('#')[0]
      } else if (/android/.test(ua)) {
        url = window.location.href
      }
      // GetWeixinScan 后端提供
      // let WxAuthUrl = GetWeixinScan + '?url=' + url;
      let WxAuthUrl = url
      try {
        const res = await getWxSignature({ url: WxAuthUrl })
        if (res.code === 0) {
          // console.log(res.data.data, '这个是获取调用扫描返回的参数？~~');
          // 微信的配置~~
          this.wxConfig(res.data.timestamp, res.data.nonceStr, res.data.signature)
        } else {
          this.$tceMobile.toast(res.message || '微信配置失败')
        }
      } catch (e) {
        this.$tceMobile.toast('微信配置失败')
      }
    },
    /**
     * wx.config的配置
     */
    wxConfig(timestamp, nonceStr, signature) {
      wx.config({
        debug: false, // 开启调试模式,
        appId: 'wx5c0d26056102d41e', // 必填，企业号的唯一标识
        timestamp: timestamp, // 必填，生成签名的时间戳
        nonceStr: nonceStr, // 必填，生成签名的随机串
        signature: signature, // 必填，签名
        jsApiList: ['scanQRCode', 'checkJsApi'] // 必填，需要使用的JS接口列表
      })
      const me = this
      wx.error(function (res) {
        me.$tceMobile.toast(`微信配置错误：${res.errMsg}`)
      })
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
     * 搜索
     */
    search() {
      this.$refs.searchByStaff && this.$refs.searchByStaff.start()
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
    this.parkInfo = store.getters.parkInfo
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
           text-align: right;
        }
        .roomLi{
          width: 100%;
          text-align: right;
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
  .item-info_label {
    min-width: 70px;
  }
}
</style>
