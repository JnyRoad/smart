<!--
- @name 入厂申请（访客预约）
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-29
-->

<template>
  <div class="page3">
    <steps :curIndex="1"></steps>
    <tce-form ref="tceForm">
      <tce-form-group>
        <tce-form-item type="item-holder" label="被访者信息" placeholder="" class="title"></tce-form-item>
        <tce-form-item field="parkName" :valueData="hostInfor.parkName" requiredMessage="请输入" type="input" label="所选园区" placeholder="请输入" :readonly="true"></tce-form-item>
        <tce-form-item required field="employeeName" :valueData="hostInfor.employeeName" requiredMessage="请输入被访人姓名" type="input" label="姓名" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="employeeMobile" :valueData="hostInfor.employeeMobile" requiredMessage="请输入被访人手机号" type="phone" label="手机号" placeholder="请输入"></tce-form-item>
      </tce-form-group>
    </tce-form>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button @click="next" class="tce-button tce-button--primary">下一步</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import steps from './components/visitor-steps'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { getNoticeApi, searchReceptionistApi, getOpenId } from '@/services/visitor'
import { APPIP } from '@/conf'

export default {
  components: {
    steps,
    page3Bottom
  },
  data() {
    return {
      parkName: '',
      formData: {},
      hostInfor: {},
      code: null
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    toBadge () {
      let url = encodeURIComponent(`${window.location.origin}/#/xuchang/visitor`)
      window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${APPIP}&redirect_uri=${url}&response_type=code&scope=snsapi_base&state=123#wechat_redirect`
    },
    getUrlParameter(name, url = window.location.href) {
      this.lhref = window.location.href
      var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)')
      var r = window.location.search.substr(1).match(reg)
      if (url) {
        if (url.indexOf('?') > -1) {
          url = url.substr(url.indexOf('?') + 1)
        }
        r = url.match(reg)
      }
      if (r != null) return r[2]
      return null
    },
    async getNoticeApi() { // 获取温馨提示
      const res = await getNoticeApi({ parkId: this.hostInfor.parkId })
      if (res.code === 0) {
        let resData = res.data
        if (resData.isNeedNotice === 1) {
          this.$alert({
            content: resData.content,
            btn: '知道了',
            htmlSupport: true
          })
        }
      }
    },
    async getOpenId() {
      const res = await getOpenId({ code: this.code })
      if (res.code === 0) {
        this.hostInfor.code = res.data
      }
    },
    async next() {
      await this.$refs.tceForm.verification()
      let formData = this.$refs.tceForm.formData
      this.$loading.show()
      const res = await searchReceptionistApi(
        Object.assign({
          parkId: this.hostInfor.parkId
        }, formData)
      )
      this.$loading.hide()
      if (res.code === 0) {
        this.hostInfor.receptionistBadge = res.data.receptionistBadge
        this.hostInfor.receptionistName = res.data.receptionistName
        this.hostInfor.receptionistPhone = res.data.receptionistPhone
        // this.hostInfor.code = this.code
        this.localStorageSave()
        this.$router.push({
          path: '/xuchang/visitor/visitorInfoHefei'
        })
      } else {
        this.toast = this.$createToast({
          txt: res.message,
          type: 'txt',
          mask: true
        })
        this.toast.show()
      }
    },
    localStorageSave() {
      localStorage.setItem('hostInfor', JSON.stringify(this.hostInfor))
    },
    storageLoad() {
      let obj = JSON.parse(localStorage.getItem('hostInfor')) // 被访人信息
      if (obj) {
        this.hostInfor = obj
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    // this.code = this.getUrlParameter('code')
    // console.log('code', this.code)
    this.storageLoad()
    this.hostInfor.parkId = 20381
    this.hostInfor.parkName = '裕同科技合肥园区'
    this.getNoticeApi()
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
    min-height: 100%;
    padding: rem(170) rem(20) rem(190);
    .business-form-group{
      margin-bottom: rem(20);
    }
    .title ::v-deep{
      .business-form-item__label{
        font-weight: bold;
        font-size: 15px;
      }
    }
    .page3-bottom{
      box-shadow: none;
    }
    .btn-inner{
      width: 100%;
      height: 100%;
      padding: 0 rem(52);
      background: $TCE-Background-Grey;
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }
</style>
