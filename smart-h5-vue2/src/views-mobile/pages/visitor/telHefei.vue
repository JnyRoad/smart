<!--
- @name 入厂申请-手机验证
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-30
-->

<template>
  <div class="page3">
    <steps :curIndex="3"></steps>
    <tce-form ref="tceForm">
      <tce-form-group>
        <tce-form-item type="item-holder" label="手机验证" placeholder="" class="title"></tce-form-item>
        <tce-form-item type="item-holder" label="手机号" placeholder="请输入">
          <template slot="itemholder">
            <div class="input-outer">
              <input v-model="visitorPhone" type="tel" inputmode="numeric" maxlength="11" placeholder="请输入"/>
            </div>
          </template>
        </tce-form-item>
        <tce-form-item type="item-holder" label="验证码" placeholder="请输入">
          <template slot="itemholder">
            <div class="input-outer">
              <input placeholder="请输入" :type="inputType" inputmode="numeric" maxlength="6" v-model="smsCode"/>
              <identifyingCode class="identifying-code" :phone="visitorPhone" :duration="120"></identifyingCode>
            </div>
          </template>
        </tce-form-item>
      </tce-form-group>
    </tce-form>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button :loading="addLoading" class="tce-button tce-button--primary"  v-if="addLoading">
          <template>正在提交</template>
        </button>
         <button @click="apply" :loading="addLoading" class="tce-button tce-button--primary" v-else>
          <template>下一步</template>
        </button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import steps from './components/visitor-steps'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { verifyPhoneMessage } from '@/services/other'
import { applySaveApi, checkBlackList } from '@/services/visitor'
import identifyingCode from '@/components/identifying-code/index.vue'
import executeOnce from '@/util/executeOnce'
import isMobile from '@/util/isMobile'
const executeOnceSubmit = executeOnce().executeOnce
export default {
  components: {
    steps,
    page3Bottom,
    identifyingCode
  },
  data() {
    return {
      inputType: 'tel',
      visitorPhone: '',
      smsCode: '',
      hostInfor: {},
      visitorInfo: {},
      fellowInfo: [],
      addLoading: false
    }
  },
  computed: {},
  props: {},
  watch: {
    smsCode(newV) {
      if (newV.length > 6) {
        newV = newV.slice(0, 6)
        this.$nextTick(() => {
          this.smsCode = newV
        })
      }
    }
  },
  methods: {
    async apply() {
      if (!this.visitorPhone) {
        this.$tceMobile.toast('请输入手机号')
        return
      } else if (!isMobile(this.visitorPhone)) {
        this.$tceMobile.toast('手机号格式不正确')
        return
      }
      this.localStorageSave()
      if (!this.smsCode) {
        this.$tceMobile.toast('请输入验证码')
        return
      }
      this.verifyPhoneMessage()
    //  this.applySaveApi()
    },
    async verifyPhoneMessage() {
      this.addLoading = true
      const res = await verifyPhoneMessage({
        mobile: this.visitorPhone,
        smsCode: this.smsCode
      })
      if (res.code === 0) {
        this.applySaveApi()
      } else {
        this.addLoading = false
        this.$tceMobile.toast(res.message)
      }
    },
    async applySaveApi() {
      let fellowList = []
      if (this.fellowInfo && this.fellowInfo.length > 0) {
        this.fellowInfo.forEach(el => {
          fellowList.push(
            {
              certNo: el.certNo,
              fellowName: el.fellowName,
              fellowPhotoId: el.fellowPhotoId,
              certType: el.certType,
              certPic: el.certPic,
              isMain: 0,
              nativePlace: ''
            }
          )
        })
      }
      let obj = {
        parkId: this.hostInfor.parkId, // 园区id
        receptionistBadge: this.hostInfor.receptionistBadge, // 被访人工号
        receptionistName: this.hostInfor.receptionistName, // 被访人名称
        receptionistPhone: this.hostInfor.receptionistPhone, // 被访人电话
        unionId: this.hostInfor.code,
        visitorName: this.visitorInfo.visitorName, // 访客姓名
        visitorPhone: this.visitorPhone, // 访客电话
        visitorPhoto: this.visitorInfo.visitorPhotoId, // 访客照片
        certType: this.visitorInfo.certType, // 证件类型
        certNo: this.visitorInfo.certType, // 证件号码
        certPic: this.visitorInfo.certPic, // 证件照片
        startTime: this.visitorInfo.startTime + ':00', // 来访开始时间
        endTime: this.visitorInfo.endTime + ':00', // 来访结束时间
        company: this.visitorInfo.company, // 访客单位
        personType: this.visitorInfo.personType.value, // 访客类别
        cause: this.visitorInfo.cause ? this.visitorInfo.cause.value : '', // 来访事由
        carryThing: this.visitorInfo.carryThing ? this.visitorInfo.carryThing.value : '', // 携带物品
        vehiclePlate: this.visitorInfo.plate, // 访客车牌号
        remark: '',
        fellowVisitorList: fellowList // 随行人员
      }
      // 验证黑名单 不为贵宾
      if (this.visitorInfo.personType.value !== 2) {
        const res = await checkBlackList({
          visitorName: obj.visitorName,
          certNo: this.visitorInfo.certNo,
          parkId: this.hostInfor.parkId
        })
        // 明确是false就是黑名单，其他返回值都通过
        if (res.data === false) {
          this.$tceMobile.toast('抱歉，你已被加入访客黑名单，不能进行入厂申请!')
          this.addLoading = false
          return
        }
      }
      const awaitRes = await executeOnceSubmit.done(applySaveApi(obj))
      if (awaitRes.code === 0 && awaitRes.data) {
        await awaitRes.done('申请成功')
        this.$router.push({
          path: '/xuchang/visitor/result'
        })
      } else {
        this.addLoading = false
        await awaitRes.done(awaitRes.message)
      }
    },
    localStorageSave() {
      localStorage.setItem('visitorPhone', this.visitorPhone)
    },
    storageLoad() {
      let obj = JSON.parse(localStorage.getItem('hostInfor')) // 被访人信息
      if (obj) {
        this.hostInfor = obj
      }

      let obj2 = JSON.parse(localStorage.getItem('visitorInfo')) // 访客信息
      if (obj2) {
        this.visitorInfo = obj2
      }
      let arr1 = JSON.parse(localStorage.getItem('fellowInfo')) // 随行人员信息
      if (arr1 && arr1.length > 0) {
        this.fellowInfo = arr1
      }

      let tel = localStorage.getItem('visitorPhone') // 访客手机号
      if (tel) {
        this.visitorPhone = tel
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    localStorage.setItem('receptionistPhone', null)
    this.storageLoad()
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
  .identifying-code{
    ::v-deep {
      > div {
        color: $TCE-Color;
      }
    }
  }
  .page3 ::v-deep{
    background: $TCE-Background-Grey;
    width: 100%;
    min-height: 100%;
    padding: rem(170) rem(20) rem(190);
    .business-form-group{
      margin-bottom: rem(20);
    }
    .title{
      .business-form-item__label{
        font-weight: bold;
        font-size: 15px;
      }
    }
    .business-form-item__value{
      input{
        text-align: left;
      }
    }
    .input-outer{
      display: flex;
      align-items: center;
      height: 100%;
      justify-content: space-between;
      padding: 0 12px;
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
