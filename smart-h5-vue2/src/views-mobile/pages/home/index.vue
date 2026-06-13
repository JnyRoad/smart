<!--
- @name 首页
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-10
-->

<template>
  <div class="home">
    <top :info="fullinfo"></top>
    <div class="home_bottom">
      <div class="block-item msg-item">
        <msg></msg>
      </div>
      <div class="block-item">
        <navList :list="naveList"></navList>
      </div>
      <div class="block-item">
        <div class="item-top">
          <span class="cur-title">园区服务</span>
        </div>
        <!-- <navList :list="serviceList"></navList> -->
        <navList :list="serviceModule"></navList>
      </div>
    </div>
  </div>
</template>

<script>
import hm1 from './img/hm_1.png'
import hm2 from './img/hm_2.png'
import hm3 from './img/hm_3.png'
import hm4 from './img/hm_4.png'
import hm5 from './img/hm_5.png'
import hm6 from './img/hm_6.png'
import hm7 from './img/hm_7.png'
import top from './components/home-top'
import msg from './components/home-msg'
import navList from './components/nav-list'
import { getServiceModule } from '@/services/home'
import { getFullinfo, getBaseinfo } from '@/services/mine'
import { getBackLogList, getBackLogListGoodReleaseWork, getDormRepairsList, getDormExitList } from '@/services/backLog'
import { PARKID, APPIP } from '@/conf'
import store from '@/store'
export default {
  components: {
    top,
    msg,
    navList
  },
  data() {
    return {
      naveList: [
        {
          imgSrc: hm1,
          title: '宿舍物品放行审批',
          // tip: '生活区',
          num: 0,
          pageSrc: '/xuchang/backLog/goodReleaseLive'
        },
        {
          imgSrc: hm6,
          title: '园区报修审批',
          num: 0,
          pageSrc: '/xuchang/backLog/dormRepairs'
        },
        // {
        //   imgSrc: hm4,
        //   title: '宿舍申请',
        //   num: 0,
        //   pageSrc: '/xuchang/checkIn'
        // },
        //  {
        //   imgSrc: hm2,
        //   title: '退宿申请',
        //   num: 0,
        //   pageSrc: '/xuchang/dormExit'
        // },
        {
          imgSrc: hm4,
          title: '退宿审批',
          num: 0,
          pageSrc: '/xuchang/backLog/dormExit'
        }
      ],
      serviceModule: [],
      serviceList: [
        {
          imgSrc: hm1,
          title: '物品放行',
          tip: '生活区',
          pageSrc: '/xuchang/goodReleaseLive'
        },
        {
          imgSrc: hm2,
          title: '返厂确认',
          pageSrc: '/xuchang/returnFactory'
        },
        {
          imgSrc: hm3,
          title: '宿舍申请'
        },
        {
          imgSrc: hm4,
          title: '退宿申请',
          pageSrc: '/xuchang/dormExit'
        },
        {
          imgSrc: hm5,
          title: '园区报修'
        },
        {
          imgSrc: hm6,
          title: '物品放行',
          tip: '办公区',
          pageSrc: '/xuchang/goodReleaseWork'
        },
        {
          imgSrc: hm7,
          title: '待办事项',
          pageSrc: '/xuchang/backLog'
        }
      ],
      fullinfo: {},
      parkInfo: {},
      baseInfo: {}
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    initData() {
      this.$loading.show()
      Promise.all([
        this.getFullinfo(),
        this.getBaseinfo(),
        this.getServiceModule(),
        this.getparkInfo(),
        this.getBackLogNum(3), // 物品放行生活区 待审批条数
        this.getDormRepairsList(),
        this.getDormExitList()
        // this.getBackLogNumGoodReleaseWork(), //物品放行办公区 待审批条数
      ])
        .then((arr) => {})
        .catch((err) => {
          if (typeof window !== 'undefined') {
            window.__SMART_LAST_HOME_ERROR__ = {
              message: err && err.message,
              code: err && err.code,
              status: err && err.response && err.response.status
            }
          }
          this.$tceMobile.toast('数据加载失败')
        })
        .finally(() => {
          this.$loading.hide()
        })
    },
    async getServiceModule() {
      this.serviceModule = []
      const res = await getServiceModule()
      if (res.code === 0 && res.data.serviceModule) {
        let arr = res.data.serviceModule
        // console.log('a' , arr)
        arr.forEach((el) => {
          this.serviceModule.push({
            imgSrc: el.moduleIcon,
            //  title: this.flitterTitle(el.moduleUrl, el.moduleName),
            // tip: this.flitterTip(el.moduleUrl, el.moduleName),
            title: el.moduleName,
            tip: '',
            num: '',
            pageSrc: this.flitterUrl(el.moduleUrl)
          })
        })
      } else {
        this.serviceModule = []
      }
    },
    flitterUrl(url) {
      let linkUrl = ''
      switch (url) {
        case '/dormRepairs': // 园区报修
          linkUrl = '/xuchang/dormRepairs'
          break
        case '/xuchang/checkIn': // 申请宿舍
          linkUrl = '/xuchang/checkIn'
          break
        case '/xuchang/dormExit': // 退宿申请
          linkUrl = '/xuchang/dormExit'
          break
        case '/approve': // 待审批
          linkUrl = '/xuchang/backLog'
          break
        case '/releaseGoods': // 物品放行（生活区）
          linkUrl = '/xuchang/goodReleaseLive'
          break
        case '/articlesrelease': // 物品放行（办公区）
          linkUrl = '/xuchang/goodReleaseWork'
          break
        case '/returnFactory': // 返厂确认
          linkUrl = '/xuchang/returnFactory'
          break
        case '/dorm': // 我的宿舍
          linkUrl = '/xuchang/dorm'
          break
        default:
          break
      }
      return linkUrl
    },
    flitterTitle(url, title) {
      let tempTitle = title
      switch (url) {
        case '/releaseGoods': // 物品放行（生活区）
          tempTitle = '物品放行'
          break
        case '/articlesrelease': // 物品放行（办公区）
          tempTitle = '物品放行'
          break
        default:
          break
      }
      return tempTitle
    },
    flitterTip(url, title) {
      let tempTip = ''
      switch (url) {
        case '/releaseGoods': // 物品放行（生活区）
          tempTip = '生活区'
          break
        case '/articlesrelease': // 物品放行（办公区）
          tempTip = '办公区'
          break
        default:
          break
      }
      return tempTip
    },
    /**
     * 获取用户完整信息
     */
    async getFullinfo() {
      const res = await getFullinfo()
      if (res.code === 0) {
        this.fullinfo = res.data
        store.commit('SET_USERIFNO', this.fullinfo)
      } else {
        store.commit('SET_USERIFNO', null)
      }
    },
    /**
     * 获取园区信息，当前固定写许昌的
     */
    async getparkInfo() {
      let obj = {
        id: PARKID,
        parkName: '裕同科技许昌园区',
        parkAddress: '许昌数字经济产业园'
      }
      this.parkInfo = obj
      store.commit('SET_PARKINFO', obj)
    },
    /**
     * 获取用户基本信息
     */
    async getBaseinfo() {
      const res = await getBaseinfo()
      if (res.code === 0) {
        store.commit('SET_USERIFNOBASE', res.data)
        this.baseInfo = res.data
        if (this.baseInfo.status === 0) {
          // 已离职
          this.$createDialog({
            type: 'alert',
            title: '提示',
            content: '该用户已离职，已为你自动退出登录',
            confirmBtn: {
              text: '确定',
              active: true,
              disabled: false,
              href: 'javascript:;'
            },
            onConfirm: () => {
              store.commit('SET_ACCESS_TOKEN', '')
              let url = encodeURIComponent(`${window.location.origin}/#/xuchang/login/logon_badge`)
              window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${APPIP}&redirect_uri=${url}&response_type=code&scope=snsapi_base&state=123#wechat_redirect`
            }
          }).show()
        }
        // store.commit('SET_ACCESS_TOKEN', '')
      } else {
        store.commit('SET_USERIFNOBASE', null)
      }
    },
    async getBackLogNum(recordType) {
      // recordType 1、离职审批 2、访客审批 3、物品放行审批 4、申诉审批 5、园区报修
      const obj = {
        recordType: recordType,
        recordState: 0, // 状态固定为待审批
        current: 1,
        size: 10
      }
      const res = await getBackLogList(obj)
      if (res.code === 0 && res.data) {
        if (recordType === 3) {
          // 物品放行 待审批个数
          this.naveList[0].num = res.data.total
        }
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    /**
     * 获取报修
     */
    async getDormRepairsList(type, params = {}) {
      const obj = {
        recordType: 5,
        recordState: 0,
        current: this.current,
        size: 10
      }
      this.$loading.show()
      const res = await getDormRepairsList(Object.assign(obj, params))
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.naveList[1].num = res.data.total
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    /**
     * 获取退宿
     */
    async getDormExitList(type, params = {}) {
      const me = this
      const obj = {
        isSecurityGuard: me.baseInfo.isSecurityGuard,
        parkId: me.parkInfo.id,
        status: 0, // 状态固定为待审批
        current: this.current,
        size: 10
      }
      this.$loading.show()
      const res = await getDormExitList(Object.assign(obj, params))
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.naveList[2].num = res.data.total
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    async getBackLogNumGoodReleaseWork() {
      const obj = {
        approvalStatus: 0, // 状态固定为待审批
        current: 1,
        size: 10
      }
      const res = await getBackLogListGoodReleaseWork(obj)
      if (res.code === 0 && res.data) {
        this.naveList[1].num = res.data.total
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.initData()
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
.home {
  background: $TCE-Background-Grey;
  width: 100%;
  min-height: 100%;
  padding-bottom: rem(30);
  &_bottom {
    padding: 0 rem(30) 0 rem(20);
    .block-item {
      background: #fff;
      border-radius: 5px;
      margin-bottom: rem(20);
      .item-top {
        padding: rem(30) rem(20) rem(20);
      }
      .cur-title {
        font-size: 18px;
        color: #ec6c01;
        padding-bottom: 5px;
        border-bottom: 2px solid #ec6c01;
      }
    }
  }
}
</style>
