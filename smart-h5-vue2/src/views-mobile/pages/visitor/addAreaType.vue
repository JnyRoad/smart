<template>
  <div class="page3">
    <div class="area-search-block">
      <cube-input v-model="searchValue" placeholder="搜索授权区域"></cube-input>
      <div class="area-toolbar">
        <button type="button" class="select-all-btn" @click="toggleAllAreas">{{ allChecked ? '取消全选' : '全选' }}</button>
        <span class="selected-count">已选 {{ checkList.length }}/{{ allAreaTypeList.length }}</span>
      </div>
    </div>
    <cube-checkbox-group v-if="areaTypeList.length > 0" v-model="checkList" :options="areaTypeList" />
    <div v-else class="area-empty">暂无匹配区域</div>
    <div class="-line-20 -line-background"></div>
    <div class="block">
      <div class="row">
        <div class="label">
          <tce-label-justify label="详细位置:"></tce-label-justify>
        </div>
        <div class="value">
          <cube-input v-model="value"></cube-input>
        </div>
      </div>
    </div>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button @click="apply()" class="tce-button tce-button--primary" style="margin-right: 20px">
          <template>确定</template>
        </button>
        <button @click="clear()" class="tce-button tce-button--primary">
          <template>重置</template>
        </button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { getAreaOptions, getAreaType } from '@/services/visitor'
import { PARKID } from '@/conf'

export default {
  components: {
    page3Bottom
  },
  data() {
    return {
      allAreaTypeList: [],
      areaTypeList: [],
      checkList: [],
      searchValue: '',
      value: '',
      type: 0,
      factoryType: '',
      parkId: PARKID
    }
  },
  computed: {
    allChecked() {
      return this.allAreaTypeList.length > 0 && this.allAreaTypeList.every((area) => this.checkList.indexOf(area.value) !== -1)
    }
  },
  props: {},
  watch: {
    searchValue() {
      this.filterAreaTypeList()
    }
  },
  methods: {
    /**
     * 提交
     */
    async apply() {
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/visitor/visitorInfo'
      })
    },
    clear() {
      this.checkList = []
      this.value = ''
    },
    toggleAllAreas() {
      if (this.allChecked) {
        this.checkList = []
        return
      }
      this.checkList = this.allAreaTypeList.map((area) => area.value)
    },
    filterAreaTypeList() {
      const keyword = this.searchValue ? String(this.searchValue).trim().toLowerCase() : ''
      if (!keyword) {
        this.areaTypeList = this.allAreaTypeList
        return
      }
      this.areaTypeList = this.allAreaTypeList.filter((area) => String(area.label).toLowerCase().indexOf(keyword) !== -1)
    },
    async getAreaTypeList() {
      const cachedAreaOptions = this.loadAreaOptionsCache()
      if (this.applyFactoryAreas(cachedAreaOptions)) {
        this.filterAreaTypeList()
      }
      try {
        const res = await getAreaOptions({ parkId: this.parkId })
        if (res.code === 0 && res.data) {
          const areaOptions = this.normalizeAreaOptions(res.data)
          if (this.applyFactoryAreas(areaOptions)) {
            this.saveAreaOptionsCache(areaOptions)
            this.filterAreaTypeList()
            return
          }
          this.handleUnavailableAreaOptions()
          return
        }
        if (this.canLoadLegacyAreaType()) {
          await this.loadLegacyAreaTypeList()
        }
      } catch (error) {
        if (window.console) {
          window.console.warn(error)
        }
        if (this.canLoadLegacyAreaType()) {
          await this.loadLegacyAreaTypeList()
        }
      }
    },
    canLoadLegacyAreaType() {
      return !this.normalizeAreaCode(this.factoryType)
    },
    handleUnavailableAreaOptions() {
      localStorage.removeItem(this.getAreaOptionsCacheKey())
      this.allAreaTypeList = []
      this.areaTypeList = []
      this.checkList = []
      this.$tceMobile.toast('授权区域配置不可用，请联系管理员')
    },
    async loadLegacyAreaTypeList() {
      const res = await getAreaType({ flag: this.type })
      if (res.code === 0 && res.data) {
        this.allAreaTypeList = res.data.map((area, index) => {
          return {
            label: area.desc,
            value: this.normalizeAreaCode(area.code),
            disabled: false,
            sort: index
          }
        }).filter((area) => area.value !== '' && area.label)
        this.filterAreaTypeList()
      } else {
        this.$tceMobile.toast(res.message)
      }
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
    normalizeAreaOptions(data) {
      const factories = data && data.factories && data.factories.length > 0 ? data.factories : []
      return {
        parkId: data && data.parkId ? data.parkId : this.parkId,
        inlineAreaLimit: Number(data && data.inlineAreaLimit) > 0 ? Number(data.inlineAreaLimit) : 4,
        factories: factories.map((factory) => {
          const factoryType = this.normalizeAreaCode(factory.factoryType)
          const areas = factory.areas && factory.areas.length > 0 ? factory.areas : []
          const areaFlagValue = Number(factory.areaFlag)
          return {
            factoryType: factoryType,
            factoryName: factory.factoryName || '',
            areaFlag: Number.isNaN(areaFlagValue) ? null : areaFlagValue,
            sort: this.normalizeSort(factory.sort),
            areas: areas.map((area) => {
              const areaCode = area.areaCode !== undefined ? area.areaCode : (area.value !== undefined ? area.value : area.code)
              const areaName = area.areaName || area.label || area.desc
              return {
                areaCode: areaCode,
                areaName: areaName,
                label: areaName,
                value: this.normalizeAreaCode(areaCode),
                disabled: false,
                isCommon: Number(area.isCommon) === 1 || area.isCommon === true,
                sort: this.normalizeSort(area.sort)
              }
            }).filter((area) => area.value !== '' && area.label)
              .sort((leftArea, rightArea) => leftArea.sort - rightArea.sort)
          }
        }).filter((factory) => factory.factoryType)
          .sort((leftFactory, rightFactory) => leftFactory.sort - rightFactory.sort)
      }
    },
    applyFactoryAreas(areaOptions) {
      if (!areaOptions || !areaOptions.factories || areaOptions.factories.length === 0) {
        return false
      }
      const factory = this.findFactoryByType(areaOptions.factories)
      if (!factory || !factory.areas) {
        return false
      }
      this.factoryType = factory.factoryType
      this.type = factory.areaFlag
      this.allAreaTypeList = factory.areas.map((area) => {
        return {
          label: area.label || area.areaName,
          value: area.value || this.normalizeAreaCode(area.areaCode),
          disabled: false
        }
      }).filter((area) => area.value !== '' && area.label)
      this.pruneCheckList()
      return true
    },
    findFactoryByType(factories) {
      const factoryType = this.normalizeAreaCode(this.factoryType)
      if (factoryType) {
        return factories.find((item) => String(item.factoryType) === factoryType)
      }
      return factories.find((item) => String(item.areaFlag) === String(this.type))
    },
    getAvailableAreaValues() {
      const valueSet = {}
      this.allAreaTypeList.forEach((area) => {
        if (area.value !== '') {
          valueSet[area.value] = true
        }
      })
      return valueSet
    },
    pruneCheckList() {
      const valueSet = this.getAvailableAreaValues()
      if (Object.keys(valueSet).length === 0) {
        return
      }
      this.checkList = this.checkList.map((code) => this.normalizeAreaCode(code)).filter((code) => valueSet[code])
    },
    getAreaOptionsCacheKey() {
      return `visitorAreaOptions_${this.parkId}`
    },
    getAreaTypeByFactoryCacheKey() {
      return `visitorAreaTypeByFactory_${this.parkId}`
    },
    loadAreaTypeByFactoryCache() {
      const cacheValue = localStorage.getItem(this.getAreaTypeByFactoryCacheKey())
      if (!cacheValue) {
        return {}
      }
      try {
        return JSON.parse(cacheValue) || {}
      } catch (error) {
        if (window.console) {
          window.console.warn(error)
        }
        localStorage.removeItem(this.getAreaTypeByFactoryCacheKey())
        return {}
      }
    },
    saveAreaTypeByFactory(areaTypeObj) {
      if (!this.factoryType) {
        return
      }
      const areaTypeByFactory = this.loadAreaTypeByFactoryCache()
      areaTypeByFactory[this.normalizeAreaCode(this.factoryType)] = areaTypeObj
      localStorage.setItem(this.getAreaTypeByFactoryCacheKey(), JSON.stringify(areaTypeByFactory))
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
    localStorageSave() {
      this.pruneCheckList()
      const obj = {
        list: this.checkList.map((code) => this.normalizeAreaCode(code)).filter((code) => code !== ''),
        custom: this.value || ''
      }
      if (String(this.type) === '1') {
        localStorage.setItem('newAreaTypeList', JSON.stringify(obj))
      } else if (String(this.type) === '0') {
        localStorage.setItem('oldAreaTypeList', JSON.stringify(obj))
      }
      this.saveAreaTypeByFactory(obj)
    },
    storageLoad() {
      const areaTypeByFactory = this.loadAreaTypeByFactoryCache()
      const storedAreaTypeObj = areaTypeByFactory[this.normalizeAreaCode(this.factoryType)]
      if (storedAreaTypeObj) {
        const normalizedAreaTypeObj = this.normalizeStoredAreaTypeObj(storedAreaTypeObj)
        this.checkList = normalizedAreaTypeObj.list
        this.value = normalizedAreaTypeObj.custom
      } else if (String(this.type) === '1') {
        let obj1 = JSON.parse(localStorage.getItem('newAreaTypeList'))
        if (obj1) {
          const newAreaTypeObj = this.normalizeStoredAreaTypeObj(obj1)
          this.checkList = newAreaTypeObj.list
          this.value = newAreaTypeObj.custom
        }
      } else {
        let obj2 = JSON.parse(localStorage.getItem('oldAreaTypeList'))
        if (obj2) {
          const oldAreaTypeObj = this.normalizeStoredAreaTypeObj(obj2)
          this.checkList = oldAreaTypeObj.list
          this.value = oldAreaTypeObj.custom
        }
      }
      this.checkList = this.checkList.map((code) => this.normalizeAreaCode(code)).filter((code) => code !== '')
    },
    normalizeStoredAreaTypeObj(areaTypeObj) {
      return {
        custom: areaTypeObj && areaTypeObj.custom ? areaTypeObj.custom : '',
        list: areaTypeObj && areaTypeObj.list
          ? areaTypeObj.list.map((code) => this.normalizeAreaCode(code)).filter((code) => code !== '')
          : []
      }
    },
    getStoredParkId() {
      const hostInforValue = localStorage.getItem('hostInfor')
      if (!hostInforValue) {
        return PARKID
      }
      try {
        const hostInfor = JSON.parse(hostInforValue)
        return hostInfor && hostInfor.parkId ? hostInfor.parkId : PARKID
      } catch (error) {
        if (window.console) {
          window.console.warn(error)
        }
        return PARKID
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.type = this.$route.query.type
    this.factoryType = this.$route.query.factoryType || ''
    this.parkId = this.$route.query.parkId || this.getStoredParkId()
    this.storageLoad()
    this.getAreaTypeList()
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
  min-height: 100%;
  padding: rem(20) rem(20) rem(190);
  .area-search-block {
    background: #fff;
    padding: rem(20) rem(24);
    margin-bottom: rem(20);
    .area-toolbar {
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      align-items: center;
      margin-top: rem(18);
      .select-all-btn {
        max-width: 100%;
        min-height: rem(54);
        padding: rem(8) rem(22);
        border: 1px solid #00c297;
        border-radius: rem(8);
        background: #fff;
        color: #00a77f;
        line-height: 1.3;
        white-space: normal;
        word-break: break-all;
        overflow-wrap: anywhere;
      }
      .selected-count {
        color: #666;
        line-height: 1.4;
        white-space: normal;
        word-break: break-all;
        overflow-wrap: anywhere;
      }
    }
  }
  .area-empty {
    background: #fff;
    color: #999;
    padding: rem(36) rem(24);
    text-align: center;
    line-height: 1.4;
  }
  ::v-deep {
    .cube-checkbox {
      white-space: normal;
      word-break: break-all;
      overflow-wrap: anywhere;
    }
    .cube-checkbox-label {
      line-height: 1.4;
    }
  }
  .block {
    background: #fff;
    padding: 0 rem(30);
    .row {
      display: flex;
      border-bottom: 1px solid #eee;
      padding: rem(30) 0;
      .label {
        color: #999;
        width: rem(130);
        flex: none;
        height: 40px;
        line-height: 40px;
      }
      .value {
        flex: 1;
        padding-left: rem(30);
        text-align: right;
      }
    }
    & .row:last-child {
      border: none;
    }
  }
  .btn-inner {
    width: 100%;
    height: 100%;
    padding: 0 rem(52);
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style>
