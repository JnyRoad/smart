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
import { applySaveApi, checkBlackList, getAreaOptions, getAreaType } from '@/services/visitor'
import identifyingCode from '@/components/identifying-code/index.vue'
import executeOnce from '@/util/executeOnce'
import isMobile from '@/util/isMobile'
import { PARKID } from '@/conf'
const executeOnceSubmit = executeOnce().executeOnce
// import VConsole from 'vconsole'
// var vConsole = new VConsole()
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
      carList: [],
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
    // this.applySaveApi()
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
      const canSubmitAreaType = await this.pruneVisitorAreaTypesBeforeSubmit()
      if (!canSubmitAreaType) {
        this.addLoading = false
        return
      }
      let vehicleList = []
      let fellowList = []
      if (this.fellowInfo && this.fellowInfo.length > 0) {
        fellowList.push(
          {
            certNo: this.visitorInfo.certNo,
            fellowName: this.visitorInfo.visitorName,
            fellowPhotoId: this.visitorInfo.visitorPhotoId,
            isMain: 1,
            nativePlace: ''
          }
        )
        this.fellowInfo.forEach(el => {
          fellowList.push(
            {
              certNo: el.certNo,
              fellowName: el.fellowName,
              fellowPhotoId: el.fellowPhotoId,
              isMain: 0,
              nativePlace: ''
            }
          )
        })
      } else {
        fellowList = [
          {
            certNo: this.visitorInfo.certNo,
            fellowName: this.visitorInfo.visitorName,
            fellowPhotoId: this.visitorInfo.visitorPhotoId,
            isMain: 1,
            nativePlace: ''
          }
        ]
      }
      if (this.carList && this.carList.length > 0) {
        this.carList.forEach(el => {
          vehicleList.push(
            {
              certImg: el.certImg,
              certType: el.certType.value,
              emergencyName: el.emergencyName,
              emergencyPhone: el.emergencyPhone,
              licenseNo: el.licenseNo,
              modle: el.modle,
              name: el.name,
              nativePlace: el.nativePlace,
              plate: el.plate,
              colour: el.colour ? el.colour.value : '',
              vehicleType: el.vehicleType ? el.vehicleType.value : ''
            }
          )
        })
      }
      if (this.visitorInfo.areaType && this.visitorInfo.areaType.length > 0) {
        const a = []
        this.visitorInfo.areaType.forEach(element => {
          a.push(parseInt(element))
        })
        this.visitorInfo.areaType = a
      }
      let obj = {
        parkId: this.hostInfor.parkId,
        receptionistBadge: this.hostInfor.receptionistBadge,
        receptionistName: this.hostInfor.receptionistName,
        receptionistPhone: this.hostInfor.receptionistPhone,
        unionId: this.hostInfor.code,
        visitorName: this.visitorInfo.visitorName,
        visitorPhone: this.visitorPhone,
        visitorPhotoId: this.visitorInfo.visitorPhotoId,
        startTime: this.visitorInfo.startTime + ':00',
        endTime: this.visitorInfo.endTime + ':00',
        company: this.visitorInfo.company,
        permitArea: this.visitorInfo.permitArea ? this.visitorInfo.permitArea : '',
        permitOldArea: this.visitorInfo.permitOldArea ? this.visitorInfo.permitOldArea : '',
        permitFactoryType: this.visitorInfo.permitFactoryType,
        areaType: this.visitorInfo.areaType,
        personType: this.visitorInfo.personType.value,
        // permitAreaTypeId: this.visitorInfo.permitAreaTypeId ? this.visitorInfo.permitAreaTypeId.value : '',
        cause: this.visitorInfo.cause ? this.visitorInfo.cause.value : '',
        thing: this.visitorInfo.thing ? this.visitorInfo.thing : '',
        remark: '',
        vehicleList: vehicleList,
        fellowList: fellowList
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
    normalizeAreaCode(code) {
      if (code === undefined || code === null) {
        return ''
      }
      return String(code)
    },
    normalizeAreaOptions(data) {
      const factories = data && data.factories && data.factories.length > 0 ? data.factories : []
      return {
        parkId: data && data.parkId ? data.parkId : this.getParkId(),
        factories: factories.map((factory) => {
          const factoryType = this.normalizeAreaCode(factory.factoryType)
          const areas = factory.areas && factory.areas.length > 0 ? factory.areas : []
          return {
            factoryType: factoryType,
            areaFlag: factory.areaFlag,
            areas: areas.map((area) => {
              const areaCode = area.areaCode !== undefined ? area.areaCode : (area.value !== undefined ? area.value : area.code)
              return {
                value: this.normalizeAreaCode(areaCode)
              }
            }).filter((area) => area.value !== '')
          }
        }).filter((factory) => factory.factoryType)
      }
    },
    getParkId() {
      return this.hostInfor && this.hostInfor.parkId ? this.hostInfor.parkId : PARKID
    },
    getAreaOptionsCacheKey() {
      return `visitorAreaOptions_${this.getParkId()}`
    },
    loadAreaOptionsCache() {
      const cacheValue = localStorage.getItem(this.getAreaOptionsCacheKey())
      if (!cacheValue) {
        return null
      }
      try {
        return this.normalizeAreaOptions(JSON.parse(cacheValue))
      } catch (error) {
        if (window.console) {
          window.console.warn(error)
        }
        localStorage.removeItem(this.getAreaOptionsCacheKey())
        return null
      }
    },
    saveAreaOptionsCache(areaOptions) {
      localStorage.setItem(this.getAreaOptionsCacheKey(), JSON.stringify(areaOptions))
    },
    async loadAreaOptionsForSubmit() {
      const cachedAreaOptions = this.loadAreaOptionsCache()
      try {
        const res = await getAreaOptions({ parkId: this.getParkId() })
        if (res.code === 0 && res.data) {
          const areaOptions = this.normalizeAreaOptions(res.data)
          if (areaOptions.factories.length > 0) {
            this.saveAreaOptionsCache(areaOptions)
            return areaOptions
          }
          if (areaOptions.factories.length === 0) {
            throw new Error('授权区域配置不可用，请返回重试')
          }
        }
        if (cachedAreaOptions && cachedAreaOptions.factories.length > 0) {
          return cachedAreaOptions
        }
        const legacyAreaOptions = await this.loadLegacyAreaOptions()
        if (legacyAreaOptions) {
          return legacyAreaOptions
        }
        throw new Error('授权区域加载失败，请返回重试')
      } catch (error) {
        if (window.console) {
          window.console.warn(error)
        }
        if (error && error.message === '授权区域配置不可用，请返回重试') {
          throw error
        }
        if (cachedAreaOptions && cachedAreaOptions.factories.length > 0) {
          return cachedAreaOptions
        }
        const legacyAreaOptions = await this.loadLegacyAreaOptions()
        if (legacyAreaOptions) {
          return legacyAreaOptions
        }
        throw new Error('授权区域加载失败，请返回重试')
      }
    },
    async loadLegacyAreaOptions() {
      try {
        const newFactory = await this.loadLegacyFactory(1, '15')
        const oldFactory = await this.loadLegacyFactory(0, '16')
        const areaOptions = {
          parkId: this.getParkId(),
          factories: [newFactory, oldFactory]
        }
        this.saveAreaOptionsCache(areaOptions)
        return areaOptions
      } catch (error) {
        if (window.console) {
          window.console.warn(error)
        }
        return null
      }
    },
    async loadLegacyFactory(flag, factoryType) {
      const res = await getAreaType({ flag: flag })
      if (res.code !== 0 || !res.data) {
        throw new Error(res.message || '授权区域加载失败')
      }
      return {
        factoryType: factoryType,
        areaFlag: flag,
        areas: res.data.map((area) => {
          return {
            value: this.normalizeAreaCode(area.code)
          }
        }).filter((area) => area.value !== '')
      }
    },
    getSubmitAreaValueSet(areaOptions) {
      const valueSet = {}
      const factories = areaOptions && areaOptions.factories ? areaOptions.factories : []
      const permitFactoryType = this.normalizeAreaCode(this.visitorInfo && this.visitorInfo.permitFactoryType)
      const activeFactories = permitFactoryType
        ? factories.filter((factory) => this.normalizeAreaCode(factory.factoryType) === permitFactoryType)
        : factories
      const factoriesForSubmit = permitFactoryType ? activeFactories : factories
      factoriesForSubmit.forEach((factory) => {
        const areas = factory.areas && factory.areas.length > 0 ? factory.areas : []
        areas.forEach((area) => {
          const areaCode = area.value || this.normalizeAreaCode(area.areaCode)
          if (areaCode !== '') {
            valueSet[areaCode] = true
          }
        })
      })
      return valueSet
    },
    pruneVisitorAreaTypes(areaOptions) {
      if (!this.visitorInfo) {
        this.visitorInfo = {}
      }
      const currentAreaType = Array.isArray(this.visitorInfo.areaType) ? this.visitorInfo.areaType : []
      const normalizedAreaType = currentAreaType.map((code) => this.normalizeAreaCode(code)).filter((code) => code !== '')
      const valueSet = this.getSubmitAreaValueSet(areaOptions)
      this.visitorInfo.areaType = normalizedAreaType.filter((code) => valueSet[code])
      localStorage.setItem('visitorInfo', JSON.stringify(this.visitorInfo))
    },
    async pruneVisitorAreaTypesBeforeSubmit() {
      try {
        const areaOptions = await this.loadAreaOptionsForSubmit()
        this.pruneVisitorAreaTypes(areaOptions)
        if (!this.visitorInfo.areaType || this.visitorInfo.areaType.length === 0) {
          this.$tceMobile.toast('请选择授权区域')
          this.$router.push({
            path: '/xuchang/visitor/visitorInfo'
          })
          return false
        }
        return true
      } catch (error) {
        if (window.console) {
          window.console.warn(error)
        }
        this.$tceMobile.toast(error.message || '授权区域加载失败，请返回重试')
        return false
      }
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
      let arr2 = JSON.parse(localStorage.getItem('carList')) // 车辆信息
      if (arr2 && arr2.length > 0) {
        this.carList = arr2
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
