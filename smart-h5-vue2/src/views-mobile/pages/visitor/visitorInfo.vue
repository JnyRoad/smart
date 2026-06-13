<!--
- @name 入厂申请-访客信息
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-29
-->

<template>
  <div class="page3">
    <steps :curIndex="2"></steps>
    <tce-form ref="tceForm">
      <tce-form-group>
        <tce-form-item type="item-holder" label="访客信息" placeholder="" class="title"></tce-form-item>
        <tce-form-item
          required
          field="visitorName"
          :valueData="visitorInfo.visitorName"
          requiredMessage="请输入访客姓名"
          type="input"
          label="访客姓名"
          placeholder="请输入"
        ></tce-form-item>
        <!-- 检测人脸 -->
        <tce-form-item required field="visitorPhotoId" :valueData="visitorInfo.visitorPhotoId" type="upload-image-single-face" label="访客照片"></tce-form-item>
        <!-- 普通上传 -->
        <!-- <tce-form-item required field="visitorPhotoId" :valueData="visitorInfo.visitorPhotoId" type="upload-image-single" label="访客照片"></tce-form-item> -->
        <!--<tce-form-item
          required
          field="personType"
          @pickerSelectHandle="personTypeChange"
          type="picker"
          :valueData="visitorInfo.personType"
          :formOption="{
            opt: personTypeList
          }"
          label="来访类型"
          placeholder="请选择"
        ></tce-form-item>-->
        <tce-form-item
          required
          field="certNo"
          :valueData="visitorInfo.certNo"
          requiredMessage="请输入证件号码"
          type="input"
          label="证件号码"
          placeholder="请输入"
        ></tce-form-item>
        <tce-form-item
          required
          field="company"
          :valueData="visitorInfo.company"
          requiredMessage="请输入来访单位"
          type="input"
          label="来访单位"
          placeholder="请输入"
        ></tce-form-item>
        <tce-form-item
          required
          field="cause"
          type="picker"
          :valueData="visitorInfo.cause"
          :formOption="{
            opt: causeList
          }"
          :defaultProps="{ label: 'desc', value: 'code' }"
          label="来访事由"
          placeholder="请选择"
        ></tce-form-item>
        <!-- <tce-form-item
          required
          field="thing"
          type="picker"
          :valueData="visitorInfo.thing"
          :formOption="{
            opt: carryList
          }"
          :defaultProps="{ label: 'desc', value: 'code' }"
          label="携带物品"
          placeholder="请选择"
        ></tce-form-item> -->
        <tce-form-item type="item-holder" required field="factoryTypeFlag" :valueData="permitFactoryType" label="区域类型" placeholder="请选择">
          <template slot="itemholder">
            <div class="factory-select-inline">
              <button
                v-for="factory in factoryCards"
                :key="factory.factoryType"
                type="button"
                class="factory-select-btn"
                :class="{ 'is-active': String(permitFactoryType) === String(factory.factoryType) }"
                @click="selectFactoryType(factory.factoryType)"
              >
                <span class="factory-name">{{ factory.factoryName }}</span>
                <span class="factory-count">已选 {{ getFactoryAreaLength(factory.factoryType) }}</span>
              </button>
            </div>
          </template>
        </tce-form-item>
        <tce-form-item type="item-holder" required field="areaTypeFlag" :valueData="areaTypeFlag" label="授权区域" placeholder="请选择">
          <template slot="itemholder">
            <div slot="inner" class="area-select-panel">
              <div class="area-chip-list" v-if="currentInlineAreaList.length > 0">
                <button
                  v-for="area in currentInlineAreaList"
                  :key="area.value"
                  type="button"
                  class="area-chip"
                  :class="{ 'is-selected': isInlineAreaChecked(area) }"
                  @click="toggleInlineArea(area)"
                >
                  <span class="area-chip-check"></span>
                  <span class="area-chip-name">{{ area.areaName }}</span>
                </button>
              </div>
              <button v-if="hasMoreAreas" type="button" class="more-area-btn" @click="addAreaType(currentAreaFlag)"
                >更多区域（已选 {{ currentSelectedAreaLength }}）</button
              >
            </div>
          </template>
        </tce-form-item>
        <tce-form-item
          required
          field="startTime"
          :valueData="visitorInfo.startTime"
          type="time-picker"
          @timeSelectHandle="startTimeHandle"
          label="来访时间"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item
          required
          field="endTime"
          :valueData="visitorInfo.endTime"
          type="time-picker"
          @timeSelectHandle="endTimeHandle"
          label="离开时间"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item type="item-holder" label="随行人员" placeholder="请选择">
          <template slot="itemholder">
            <div @click="addPerson" class="num-outer">
              <div class="input" v-if="fellowInfo.length > 0"><input placeholder="请选择" :value="fellowInfo.length + '位'" /></div>
              <div class="input" v-else><input placeholder="请选择" /></div>
              <span class="-arrow"></span>
            </div>
          </template>
        </tce-form-item>
      </tce-form-group>
      <!-- <div class="tip">
        <p class="p1">如开车入园，请填写以下信息</p>
      </div>
      <tce-form-group>
        <tce-form-item type="item-holder" label="车辆通行证办理（选填）" placeholder="" class="title"></tce-form-item>
        <tce-form-item type="item-holder" label="车辆信息" placeholder="请选择">
          <template slot="itemholder">
            <div @click="addCar" class="num-outer">
              <div class="input" v-if="carList.length > 0"><input placeholder="请选择" :value="carList.length + '辆'" /></div>
              <div class="input" v-else><input placeholder="请选择" /></div>
              <span class="-arrow"></span>
            </div>
          </template>
        </tce-form-item>
      </tce-form-group> -->
    </tce-form>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button :loading="addLoading" class="tce-button tce-button--primary" v-if="addLoading">
          <template>正在验证</template>
        </button>
        <button @click="next" :loading="addLoading" class="tce-button tce-button--primary" v-else>
          <template>下一步</template>
        </button>
        <!-- <button @click="next" class="tce-button tce-button--primary">下一步</button> -->
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import steps from './components/visitor-steps'
import page3Bottom from '@/views-mobile/components/page3-bottom'
// import componentCamera from '@/components/camera/index'
import { enumPersonCertApi, enumCauseApi, enumCarryApi, enumZoneTypeApi, validateRepeat, getAreaOptions, getAreaType } from '@/services/visitor'
import { PARKID } from '@/conf'
import { transformDate } from '@/util/date'
import { cardid } from '@/util/validate'

export default {
  components: {
    steps,
    page3Bottom
    // componentCamera
  },
  data() {
    return {
      personCertList: [], // 人员证件类型
      causeList: [], // 来访事由
      carryList: [], // 携带物品类型
      zoneList: [], // 授权进入区域类别
      factoryList: [],
      areaOptions: {
        parkId: '',
        inlineAreaLimit: 4,
        factories: []
      },
      areaTypeFlag: 1,
      newAreaTypeObj: {
        custom: '',
        list: []
      },
      newAreaTypeLength: 0,
      oldAreaTypeObj: {
        custom: '',
        list: []
      },
      oldAreaTypeLength: 0,
      areaTypeByFactory: {},
      permitFactoryType: '',
      personTypeList: [
        { label: '普通来访', value: 3 },
        { label: '贵宾来访', value: 2 }
      ],
      visitorInfo: {
        personType: { label: '普通来访', value: 3 },
        factoryType: { label: '', value: '' },
        startTime: '',
        endTime: ''
      },
      personType: 3,
      hostInfor: {},
      fellowInfo: [],
      carList: [],
      addLoading: false
    }
  },
  computed: {
    factoryCards() {
      return this.areaOptions.factories
    },
    currentFactory() {
      const factoryType = String(this.permitFactoryType)
      return this.factoryCards.find((factory) => String(factory.factoryType) === factoryType) || this.factoryCards[0]
    },
    currentFactoryAreas() {
      return this.currentFactory && this.currentFactory.areas ? this.currentFactory.areas : []
    },
    currentInlineAreaList() {
      const areas = this.currentFactoryAreas
      const limit = Number(this.areaOptions.inlineAreaLimit) > 0 ? Number(this.areaOptions.inlineAreaLimit) : 4
      if (areas.length <= limit) {
        return areas
      }
      const commonAreas = areas.filter((area) => area.isCommon)
      return (commonAreas.length > 0 ? commonAreas : areas).slice(0, limit)
    },
    currentAreaFlag() {
      return this.currentFactory ? this.currentFactory.areaFlag : 1
    },
    currentSelectedAreaLength() {
      return this.getFactoryAreaLength(this.permitFactoryType)
    },
    hasMoreAreas() {
      return this.currentFactoryAreas.length > this.currentInlineAreaList.length
    }
  },
  props: {},
  watch: {},
  methods: {
    async next() {
      let formData = this.$refs.tceForm.formData
      this.localStorageSave()
      await this.$refs.tceForm.verification()
      if (this.visitorInfo.areaType.length === 0) {
        this.$tceMobile.toast('授权区域不能为空！')
        return
      }
      /* if (new Date(formData.endTime) < new Date(formData.startTime)) {
        this.$tceMobile.toast('离开时间应大于来访时间')
        return
      } */
      let endTimeDate = new Date(formData.endTime)
      let startTimeDate = new Date(transformDate(formData.startTime))
      if (endTimeDate <= startTimeDate) {
        this.$tceMobile.toast('离开时间应大于来访时间!')
        return
      } else if (endTimeDate - startTimeDate > 365 * 24 * 60 * 60 * 1000) {
        this.$tceMobile.toast('申请时间最多不允许超过365天!')
        return
      }
      if (this.personType === 3) {
        // 普通
        // 身份证验证
        const res = cardid(formData.certNo)
        if (res[0]) {
          this.$tceMobile.toast(res[1])
          return
        }
        this.validateRepeat()
      } else {
        // 贵宾
        this.$router.push({
          path: '/xuchang/visitor/tel'
        })
      }
    },
    async validateRepeat() {
      let formData = this.$refs.tceForm.formData
      let fellowList = []
      fellowList.push({
        certNo: formData.certNo,
        fellowName: formData.visitorName,
        fellowPhotoId: formData.visitorPhotoId,
        isMain: 1,
        nativePlace: ''
      })
      if (this.fellowInfo && this.fellowInfo.length > 0) {
        this.fellowInfo.forEach((el) => {
          fellowList.push({
            certNo: el.certNo,
            fellowName: el.fellowName,
            fellowPhotoId: el.fellowPhotoId,
            isMain: 0,
            nativePlace: ''
          })
        })
      }
      let obj = {
        receptionistBadge: this.hostInfor.receptionistBadge,
        visitorName: formData.visitorName,
        visitorPhotoId: formData.visitorPhotoId,
        startTime: formData.startTime + ':00',
        endTime: formData.endTime + ':00',
        company: formData.company,
        permitArea: this.visitorInfo.permitArea ? this.visitorInfo.permitArea : '',
        permitOldArea: this.visitorInfo.permitOldArea ? this.visitorInfo.permitOldArea : '',
        permitFactoryType: this.visitorInfo.permitFactoryType,
        areaType: this.visitorInfo.areaType,
        cause: formData.cause ? formData.cause.value : '',
        thing: 4, // formData.thing ? formData.thing.value : '', //默认 无（携带物品列表接口返回的，{code:4 desc:无}）
        fellowList: fellowList
      }
      this.addLoading = true
      const res = await validateRepeat(obj)
      if (res.code === 0 && res.data) {
        this.$router.push({
          path: '/xuchang/visitor/tel'
        })
      } else {
        this.addLoading = false
        this.$tceMobile.toast(res.message)
      }
      // console.log(obj)
    },
    selectFactoryType(factoryType) {
      this.permitFactoryType = String(factoryType)
      const factory = this.getFactoryByType(this.permitFactoryType)
      this.visitorInfo.factoryType = {
        label: factory ? factory.factoryName : this.getFactoryName(this.permitFactoryType),
        value: this.permitFactoryType
      }
      this.clearOtherFactorySelections(this.permitFactoryType)
      this.syncVisitorAreaFields()
    },
    getFactoryByType(factoryType) {
      const targetFactoryType = String(factoryType)
      return this.factoryCards.find((factory) => String(factory.factoryType) === targetFactoryType)
    },
    getFactoryName(factoryType) {
      const factory = this.getFactoryByType(factoryType)
      return factory ? factory.factoryName : ''
    },
    getFactoryAreaLength(factoryType) {
      const areaTypeObj = this.getAreaTypeByFactory(factoryType)
      return areaTypeObj && areaTypeObj.list ? areaTypeObj.list.length : 0
    },
    getCurrentAreaObject() {
      return this.getAreaTypeByFactory(this.permitFactoryType)
    },
    setCurrentAreaObject(areaTypeObj) {
      this.setAreaTypeByFactory(this.permitFactoryType, areaTypeObj)
    },
    isInlineAreaChecked(area) {
      const areaTypeObj = this.getCurrentAreaObject()
      return areaTypeObj && areaTypeObj.list ? areaTypeObj.list.indexOf(area.value) !== -1 : false
    },
    toggleInlineArea(area) {
      const areaTypeObj = this.getCurrentAreaObject() || { custom: '', list: [] }
      const selectedList = areaTypeObj.list ? areaTypeObj.list.slice() : []
      const selectedIndex = selectedList.indexOf(area.value)
      if (selectedIndex === -1) {
        selectedList.push(area.value)
      } else {
        selectedList.splice(selectedIndex, 1)
      }
      this.setCurrentAreaObject({
        custom: areaTypeObj.custom || '',
        list: selectedList
      })
      this.syncVisitorAreaFields()
    },
    normalizeAreaCode(code) {
      if (code === undefined || code === null) {
        return ''
      }
      return String(code)
    },
    normalizeSort(sort) {
      const sortValue = Number(sort)
      return Number.isNaN(sortValue) ? 0 : sortValue
    },
    normalizeFactory(factory) {
      const factoryType = this.normalizeAreaCode(factory.factoryType)
      const areas = factory.areas && factory.areas.length > 0 ? factory.areas : []
      const areaFlagValue = Number(factory.areaFlag)
      return {
        factoryType: factoryType,
        factoryName: factory.factoryName || this.getFactoryName(factoryType),
        areaFlag: Number.isNaN(areaFlagValue) ? null : areaFlagValue,
        sort: this.normalizeSort(factory.sort),
        areas: areas.map((area) => {
          const areaCode = area.areaCode !== undefined ? area.areaCode : (area.value !== undefined ? area.value : area.code)
          const areaName = area.areaName || area.label || area.desc
          return {
            areaCode: areaCode,
            areaName: areaName,
            isCommon: Number(area.isCommon) === 1 || area.isCommon === true,
            sort: this.normalizeSort(area.sort),
            value: this.normalizeAreaCode(areaCode)
          }
        }).filter((area) => area.value !== '' && area.areaName)
          .sort((leftArea, rightArea) => leftArea.sort - rightArea.sort)
      }
    },
    normalizeAreaOptions(data) {
      const factories = data && data.factories && data.factories.length > 0 ? data.factories : []
      return {
        parkId: data && data.parkId ? data.parkId : this.getParkId(),
        inlineAreaLimit: Number(data && data.inlineAreaLimit) > 0 ? Number(data.inlineAreaLimit) : 4,
        factories: factories.map((factory) => this.normalizeFactory(factory))
          .filter((factory) => factory.factoryType)
          .sort((leftFactory, rightFactory) => leftFactory.sort - rightFactory.sort)
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
    clearAreaOptionsCache() {
      localStorage.removeItem(this.getAreaOptionsCacheKey())
    },
    applyAreaOptions(areaOptions) {
      if (!areaOptions || areaOptions.factories.length === 0) {
        return false
      }
      this.areaOptions = areaOptions
      this.applyLegacyAreaTypeObjects()
      this.factoryList = areaOptions.factories.map((factory) => {
        return {
          areaTypeId: factory.factoryType,
          areaTypeName: factory.factoryName,
          areaOaId: factory.factoryType
        }
      })
      const currentFactory = this.getFactoryByType(this.permitFactoryType)
      if (!currentFactory) {
        this.permitFactoryType = areaOptions.factories[0].factoryType
      }
      this.clearOtherFactorySelections(this.permitFactoryType)
      this.pruneSelectedAreaCodes()
      this.syncVisitorAreaFields()
      return true
    },
    handleUnavailableAreaOptions() {
      this.clearAreaOptionsCache()
      this.areaOptions = {
        parkId: this.getParkId(),
        inlineAreaLimit: 4,
        factories: []
      }
      this.factoryList = []
      this.permitFactoryType = ''
      this.areaTypeByFactory = {}
      this.syncVisitorAreaFields()
      this.$tceMobile.toast('授权区域配置不可用，请联系管理员')
    },
    async getAreaOptionsData() {
      const cachedAreaOptions = this.loadAreaOptionsCache()
      if (cachedAreaOptions) {
        this.applyAreaOptions(cachedAreaOptions)
      }
      try {
        const res = await getAreaOptions({ parkId: this.getParkId() })
        if (res.code === 0 && res.data) {
          const areaOptions = this.normalizeAreaOptions(res.data)
          if (areaOptions.factories.length > 0) {
            this.saveAreaOptionsCache(areaOptions)
            this.applyAreaOptions(areaOptions)
            return
          }
          this.handleUnavailableAreaOptions()
          return
        }
        await this.loadLegacyAreaOptions()
      } catch (error) {
        if (window.console) {
          window.console.warn(error)
        }
        await this.loadLegacyAreaOptions()
      }
    },
    async loadLegacyAreaOptions() {
      try {
        const newFactory = await this.loadLegacyFactory(1, '15', '新工厂')
        const oldFactory = await this.loadLegacyFactory(0, '16', '老工厂')
        const areaOptions = {
          parkId: this.getParkId(),
          inlineAreaLimit: 4,
          factories: [newFactory, oldFactory]
        }
        this.saveAreaOptionsCache(areaOptions)
        this.applyAreaOptions(areaOptions)
      } catch (error) {
        this.$tceMobile.toast(error.message || '授权区域加载失败')
      }
    },
    async loadLegacyFactory(flag, factoryType, factoryName) {
      const res = await getAreaType({ flag: flag })
      if (res.code !== 0 || !res.data) {
        throw new Error(res.message || '授权区域加载失败')
      }
      return {
        factoryType: factoryType,
        factoryName: factoryName,
        areaFlag: flag,
        sort: flag === 1 ? 1 : 2,
        areas: res.data.map((area, index) => {
          return {
            areaCode: area.code,
            areaName: area.desc,
            isCommon: false,
            sort: index,
            value: this.normalizeAreaCode(area.code)
          }
        }).filter((area) => area.value !== '' && area.areaName)
      }
    },
    normalizeStoredAreaTypeObj(areaTypeObj) {
      return {
        custom: areaTypeObj && areaTypeObj.custom ? areaTypeObj.custom : '',
        list: areaTypeObj && areaTypeObj.list
          ? areaTypeObj.list.map((code) => this.normalizeAreaCode(code)).filter((code) => code !== '')
          : []
      }
    },
    getAreaTypeByFactory(factoryType) {
      const key = this.normalizeAreaCode(factoryType)
      if (!key) {
        return { custom: '', list: [] }
      }
      return this.normalizeStoredAreaTypeObj(this.areaTypeByFactory[key])
    },
    setAreaTypeByFactory(factoryType, areaTypeObj) {
      const key = this.normalizeAreaCode(factoryType)
      if (!key) {
        return
      }
      const normalizedAreaTypeObj = this.normalizeStoredAreaTypeObj(areaTypeObj)
      if (this.$set) {
        this.$set(this.areaTypeByFactory, key, normalizedAreaTypeObj)
      } else {
        this.areaTypeByFactory[key] = normalizedAreaTypeObj
      }
    },
    clearOtherFactorySelections(factoryType) {
      const key = this.normalizeAreaCode(factoryType)
      const currentAreaTypeObj = this.getAreaTypeByFactory(key)
      this.areaTypeByFactory = {}
      this.setAreaTypeByFactory(key, currentAreaTypeObj)
    },
    getAreaTypeByFactoryCacheKey() {
      return `visitorAreaTypeByFactory_${this.getParkId()}`
    },
    loadAreaTypeByFactoryCache() {
      const cacheValue = localStorage.getItem(this.getAreaTypeByFactoryCacheKey())
      if (!cacheValue) {
        return {}
      }
      try {
        const parsedValue = JSON.parse(cacheValue)
        const areaTypeByFactory = {}
        Object.keys(parsedValue || {}).forEach((factoryType) => {
          areaTypeByFactory[this.normalizeAreaCode(factoryType)] = this.normalizeStoredAreaTypeObj(parsedValue[factoryType])
        })
        return areaTypeByFactory
      } catch (error) {
        if (window.console) {
          window.console.warn(error)
        }
        localStorage.removeItem(this.getAreaTypeByFactoryCacheKey())
        return {}
      }
    },
    saveAreaTypeByFactoryCache() {
      localStorage.setItem(this.getAreaTypeByFactoryCacheKey(), JSON.stringify(this.areaTypeByFactory))
    },
    getFactoryByAreaFlag(areaFlag) {
      const targetAreaFlag = Number(areaFlag)
      return this.factoryCards.find((factory) => Number(factory.areaFlag) === targetAreaFlag)
    },
    applyLegacyAreaTypeObjects() {
      const newFactory = this.getFactoryByAreaFlag(1)
      const oldFactory = this.getFactoryByAreaFlag(0)
      if (newFactory && !this.areaTypeByFactory[this.normalizeAreaCode(newFactory.factoryType)]) {
        this.setAreaTypeByFactory(newFactory.factoryType, this.newAreaTypeObj)
      }
      if (oldFactory && !this.areaTypeByFactory[this.normalizeAreaCode(oldFactory.factoryType)]) {
        this.setAreaTypeByFactory(oldFactory.factoryType, this.oldAreaTypeObj)
      }
    },
    syncLegacyAreaTypeObjects() {
      const newFactory = this.getFactoryByAreaFlag(1)
      const oldFactory = this.getFactoryByAreaFlag(0)
      this.newAreaTypeObj = newFactory ? this.getAreaTypeByFactory(newFactory.factoryType) : this.normalizeStoredAreaTypeObj(this.newAreaTypeObj)
      this.oldAreaTypeObj = oldFactory ? this.getAreaTypeByFactory(oldFactory.factoryType) : this.normalizeStoredAreaTypeObj(this.oldAreaTypeObj)
      localStorage.setItem('newAreaTypeList', JSON.stringify(this.newAreaTypeObj))
      localStorage.setItem('oldAreaTypeList', JSON.stringify(this.oldAreaTypeObj))
    },
    getFactoryAreaValueSet(factoryType) {
      const factory = this.getFactoryByType(factoryType)
      const valueSet = {}
      if (!factory || !factory.areas) {
        return valueSet
      }
      factory.areas.forEach((area) => {
        const areaCode = area.value || this.normalizeAreaCode(area.areaCode)
        if (areaCode !== '') {
          valueSet[areaCode] = true
        }
      })
      return valueSet
    },
    pruneAreaTypeObj(areaTypeObj, factoryType) {
      const normalizedAreaTypeObj = this.normalizeStoredAreaTypeObj(areaTypeObj)
      const valueSet = this.getFactoryAreaValueSet(factoryType)
      if (Object.keys(valueSet).length === 0) {
        return normalizedAreaTypeObj
      }
      return {
        custom: normalizedAreaTypeObj.custom,
        list: normalizedAreaTypeObj.list.filter((code) => valueSet[code])
      }
    },
    pruneSelectedAreaCodes() {
      const prunedAreaTypeByFactory = {}
      Object.keys(this.areaTypeByFactory).forEach((factoryType) => {
        if (!this.getFactoryByType(factoryType)) {
          return
        }
        prunedAreaTypeByFactory[factoryType] = this.pruneAreaTypeObj(this.areaTypeByFactory[factoryType], factoryType)
      })
      this.areaTypeByFactory = prunedAreaTypeByFactory
      this.syncLegacyAreaTypeObjects()
      this.saveAreaTypeByFactoryCache()
    },
    syncVisitorAreaFields() {
      Object.keys(this.areaTypeByFactory).forEach((factoryType) => {
        this.setAreaTypeByFactory(factoryType, this.areaTypeByFactory[factoryType])
      })
      if (this.areaOptions.factories.length > 0) {
        this.pruneSelectedAreaCodes()
      }
      this.syncLegacyAreaTypeObjects()
      this.newAreaTypeLength = this.newAreaTypeObj && this.newAreaTypeObj.list ? this.newAreaTypeObj.list.length : 0
      this.oldAreaTypeLength = this.oldAreaTypeObj && this.oldAreaTypeObj.list ? this.oldAreaTypeObj.list.length : 0
      const currentAreaTypeObj = this.getCurrentAreaObject()
      this.visitorInfo.permitFactoryType = this.permitFactoryType
      this.visitorInfo.factoryType = {
        label: this.currentFactory ? this.currentFactory.factoryName : this.getFactoryName(this.permitFactoryType),
        value: this.permitFactoryType
      }
      this.visitorInfo.permitArea = Number(this.currentAreaFlag) === 1 && currentAreaTypeObj.custom ? currentAreaTypeObj.custom : ''
      this.visitorInfo.permitOldArea = Number(this.currentAreaFlag) === 0 && currentAreaTypeObj.custom ? currentAreaTypeObj.custom : ''
      this.visitorInfo.areaType = currentAreaTypeObj && currentAreaTypeObj.list ? currentAreaTypeObj.list : []
      this.saveAreaTypeByFactoryCache()
    },
    personTypeChange(item) {
      this.personType = item.selectedVal[0]
      if (Number(this.personType) === 2) {
        // 贵宾
        // this.newAreaTypeObj = {
        //   list: ['3'],
        //   custom: ''
        // }
        this.visitorInfo.certNo = ''
        this.newAreaTypeLength = 1
        // localStorage.setItem('newAreaTypeList', JSON.stringify(this.newAreaTypeObj))
      }
    },
    addAreaType(type) {
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/visitor/addAreaType',
        query: {
          type: type,
          factoryType: this.permitFactoryType,
          parkId: this.getParkId()
        }
      })
    },
    addPerson() {
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/visitor/addPersonlist'
      })
    },
    addCar() {
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/visitor/addCarlist'
      })
    },
    startTimeHandle(time) {
      this.visitorInfo.startTime = time
      this.clearEndTime()
    },
    endTimeHandle(time) {
      let endTimeDate = new Date(time)
      let startTimeDate = new Date(transformDate(this.visitorInfo.startTime))
      if (endTimeDate <= startTimeDate) {
        this.$tceMobile.toast('离开时间应大于来访时间!')
        this.clearEndTime()
        return
      } else if (endTimeDate - startTimeDate > 365 * 24 * 60 * 60 * 1000) {
        this.$tceMobile.toast('申请时间最多不允许超过365天!')
        this.clearEndTime()
        return
      }
      this.visitorInfo.endTime = time
    },
    clearEndTime() {
      this.visitorInfo.endTime = ''
      if (this.$refs.tceForm) {
        this.$refs.tceForm.formData.endTime = ''
      }
    },
    /**
     * OA人员证件类型枚举
     */
    async getEnumPersonCert() {
      const res = await enumPersonCertApi()
      this.personCertList = res.data
    },
    /**
     * OA入厂申请事由枚举
     */
    async getEnumCause() {
      const res = await enumCauseApi()
      this.causeList = res.data
    },
    async getFactoryListData() {
      this.factoryList = this.factoryCards.map((factory) => {
        return {
          areaTypeId: factory.factoryType,
          areaTypeName: factory.factoryName,
          areaOaId: factory.factoryType
        }
      })
    },
    /**
     * 携带类型枚举
     */
    async getEnumCarry() {
      const res = await enumCarryApi()
      this.carryList = res.data
    },
    /**
     * OA区域类别
     */
    async getEnumZoneType() {
      const res = await enumZoneTypeApi({ type: 2 })
      this.zoneList = res.data
    },
    localStorageSave() {
      let formData = this.$refs.tceForm.formData
      this.visitorInfo = formData
      this.visitorInfo['thing'] = 4
      this.visitorInfo['personType'] = { label: '普通来访', value: 3 } // 普通来访
      this.syncVisitorAreaFields()
      localStorage.setItem('oldAreaTypeList', JSON.stringify(this.oldAreaTypeObj))
      localStorage.setItem('newAreaTypeList', JSON.stringify(this.newAreaTypeObj))
      this.saveAreaTypeByFactoryCache()
      localStorage.setItem('visitorInfo', JSON.stringify(this.visitorInfo))
    },
    async storageLoad() {
      let obj = JSON.parse(localStorage.getItem('visitorInfo')) // 访客信息
      if (obj) {
        this.visitorInfo = Object.assign({ startTime: '', endTime: '' }, obj)
        this.personType = this.visitorInfo.personType && this.visitorInfo.personType.value
          ? this.visitorInfo.personType.value
          : 3
        this.permitFactoryType = this.visitorInfo.permitFactoryType || ''
      }
      let obj2 = JSON.parse(localStorage.getItem('hostInfor')) // 被访人信息
      if (obj2) {
        this.hostInfor = obj2
      }
      this.areaTypeByFactory = this.loadAreaTypeByFactoryCache()
      let obj3 = JSON.parse(localStorage.getItem('oldAreaTypeList'))
      if (obj3) {
        this.oldAreaTypeObj = this.normalizeStoredAreaTypeObj(obj3)
        this.oldAreaTypeLength = this.oldAreaTypeObj.list.length
      } else {
        this.oldAreaTypeObj = {
          custom: '',
          list: []
        }
        localStorage.setItem('oldAreaTypeList', JSON.stringify(this.oldAreaTypeObj))
      }
      let obj4 = JSON.parse(localStorage.getItem('newAreaTypeList'))
      if (obj4) {
        this.newAreaTypeObj = this.normalizeStoredAreaTypeObj(obj4)
        this.newAreaTypeLength = this.newAreaTypeObj.list.length
      } else {
        this.newAreaTypeObj = {
          custom: '',
          list: []
        }
        localStorage.setItem('newAreaTypeList', JSON.stringify(this.newAreaTypeObj))
      }
      let arr1 = JSON.parse(localStorage.getItem('fellowInfo')) // 随行人员信息
      if (arr1 && arr1.length > 0) {
        this.fellowInfo = arr1
      }
      this.syncVisitorAreaFields()
      // let arr2 = JSON.parse(localStorage.getItem('carList')) //车辆信息
      // if (arr2 && arr2.length > 0) {
      //   this.carList = arr2
      //   console.log('this.carList---', this.carList)
      // }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.getEnumPersonCert()
    this.getEnumCause()
    // this.getEnumCarry()
    this.getEnumZoneType()
    this.storageLoad()
    this.getAreaOptionsData()
  },
  /**
   * 生命周期 mounted
   */
  mounted() {},
  /**
   * 生命周期 beforeDestroy
   */
  beforeDestroy() {
    // this.localStorageSave()
  }
}
</script>

<style lang="scss" scoped>
.page3 {
  background: $TCE-Background-Grey;
  width: 100%;
  min-height: 100%;
  padding: rem(170) rem(20) rem(190);
  .title ::v-deep {
    .business-form-item__label {
      font-weight: bold;
      font-size: 15px;
    }
  }
  .num-outer {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding: rem(25) rem(36);
    .input {
      flex: 1;
      input {
        width: 100%;
        text-align: right;
      }
    }
  }
  .factory-select-inline {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    align-items: center;
    padding: rem(16) rem(20) rem(16) 0;
    .factory-select-btn {
      max-width: rem(210);
      min-height: rem(64);
      margin: rem(8) 0 rem(8) rem(16);
      padding: rem(10) rem(18);
      border: 1px solid #dcdfe6;
      border-radius: rem(8);
      background: #fff;
      color: #333;
      line-height: 1.3;
      text-align: left;
      white-space: normal;
      word-break: break-all;
      overflow-wrap: anywhere;
      .factory-name {
        display: block;
        font-size: 14px;
        font-weight: 600;
      }
      .factory-count {
        display: block;
        margin-top: rem(4);
        color: #999;
        font-size: 12px;
      }
      &.is-active {
        border-color: #00c297;
        background: #f0fff9;
        color: #00a77f;
        .factory-count {
          color: #00a77f;
        }
      }
    }
  }
  .area-select-panel {
    width: 100%;
    padding: rem(16) rem(20) rem(16) 0;
    .area-chip-list {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-end;
      align-items: center;
      margin: rem(-8) 0 0 rem(-12);
    }
    .area-chip {
      max-width: 100%;
      min-height: rem(56);
      margin: rem(8) 0 0 rem(12);
      padding: rem(8) rem(14);
      border: 1px solid #dcdfe6;
      border-radius: rem(8);
      background: #fff;
      color: #333;
      display: inline-flex;
      align-items: center;
      line-height: 1.3;
      text-align: left;
      white-space: normal;
      word-break: break-all;
      overflow-wrap: anywhere;
      .area-chip-check {
        width: rem(24);
        height: rem(24);
        border: 1px solid #bfc5cc;
        border-radius: rem(4);
        margin-right: rem(8);
        flex: none;
      }
      .area-chip-name {
        min-width: 0;
      }
      &.is-selected {
        border-color: #00c297;
        background: #f0fff9;
        color: #00a77f;
        .area-chip-check {
          border-color: #00c297;
          background: #00c297;
          box-shadow: inset 0 0 0 rem(4) #f0fff9;
        }
      }
    }
    .more-area-btn {
      max-width: 100%;
      min-height: rem(56);
      margin-top: rem(18);
      padding: rem(8) rem(18);
      border: 1px solid #00c297;
      border-radius: rem(8);
      background: #fff;
      color: #00a77f;
      line-height: 1.3;
      white-space: normal;
      word-break: break-all;
      overflow-wrap: anywhere;
    }
  }
  // .areaTypeBtn{
  //   height: 30px;
  // }
  .page3-bottom {
    box-shadow: none;
  }
  .btn-inner {
    width: 100%;
    height: 100%;
    padding: 0 rem(52);
    background: $TCE-Background-Grey;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .tip {
    padding: rem(20) 0;
    color: #999;
    .p1 {
      font-size: 12px;
      margin-bottom: rem(10);
    }
    .p2 {
      font-weight: bold;
    }
  }
}
</style>
