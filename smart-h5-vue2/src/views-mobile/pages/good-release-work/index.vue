<!--
- @name 物品放行-办公区-发起提交
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-26
-->

<template>
  <div class="page3">
    <page3Tab :list="tabList" :curIndex="0" @doApply="doApply" @checkList="checkList"></page3Tab>
    <tce-form ref="tceForm">
      <tce-form-group>
        <tce-form-item
          required
          field="fxqc"
          type="picker"
          :valueData="applyGoodsWorkInfo.fxqc"
          :formOption="{
            opt: goWhere
          }"
          label="放行去处"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item
          required
          field="sffc"
          type="picker"
          :valueData="applyGoodsWorkInfo.sffc"
          :formOption="{
            opt: isReturns
          }"
          label="是否返厂"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item
          required
          field="fxdd"
          type="picker"
          :valueData="applyGoodsWorkInfo.fxdd"
          :formOption="{
            opt: startAreas
          }"
          label="出发地点"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item required field="fxddxq" :valueData="applyGoodsWorkInfo.fxddxq" requiredMessage="请输入出发备注" type="input" label="出发备注" placeholder="请输入"></tce-form-item>
        <tce-form-item
          required
          field="dddd"
          type="picker"
          :valueData="applyGoodsWorkInfo.dddd"
          :formOption="{
            opt: endAreas
          }"
          label="到达地点"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item required field="ddddxq" :valueData="applyGoodsWorkInfo.ddddxq" requiredMessage="请输入到达备注" type="input" label="到达备注" placeholder="请输入"></tce-form-item>
        <tce-form-item
          required
          field="sqrjb"
          type="picker"
          :valueData="applyGoodsWorkInfo.sqrjb"
          :formOption="{
            opt: letPeopleLevel
          }"
          label="放行人级别"
          placeholder="请选择"
        ></tce-form-item>
      </tce-form-group>
      <tce-form-group>
        <tce-form-item
          required
          field="fxsx"
          type="picker"
          :valueData="applyGoodsWorkInfo.fxsx"
          :formOption="{
            opt: letItems
          }"
          label="放行事项"
          placeholder="请选择"
          @pickerSelectHandle="fxsxSelectHandle"
        ></tce-form-item>
        <tce-form-item
          required
          field="wpfxlb"
          type="picker"
          :valueData="applyGoodsWorkInfo.wpfxlb"
          :formOption="{
            opt: letGoodsType
          }"
          label="物品放行类别"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item field="fjsc" :valueData="applyGoodsWorkInfo.fjsc" type="upload-image-single-base64" label="附件上传" placeholder="请选择"></tce-form-item>
      </tce-form-group>
      <tce-form-group v-if="fxsxValue === 0 || fxsxValue ===7">
        <tce-form-item type="item-holder" label="人员放行" placeholder="请选择">
          <template slot="itemholder">
            <div @click="openAddPerson" class="personOuter">
              <personList :list="goodsPersonInfo"></personList>
              <div style="text-align: right; color: #999" v-if="!goodsPersonInfo || goodsPersonInfo.length===0">请添加</div>
              <span class="-arrow"></span>
            </div>
          </template>
        </tce-form-item>
      </tce-form-group>
      <tce-form-group v-else>
        <tce-form-item type="item-holder" label="物品放行" placeholder="请选择">
          <template slot="itemholder">
            <div @click="openAddGoods" class="personOuter">
              <goodsList :list="releaseGoodsInfo"></goodsList>
              <div style="text-align: right; color: #999" v-if="!releaseGoodsInfo || releaseGoodsInfo.length===0">请添加</div>
              <span class="-arrow"></span>
            </div>
          </template>
        </tce-form-item>
      </tce-form-group>
    </tce-form>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button @click="apply" class="tce-button tce-button--primary is-round">申请</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Tab from '@/views-mobile/components/page3-tab'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import personList from './components/person-tag'
import goodsList from './components/goods-tag'
import { saveApi } from '@/services/goodRreleaseOffice'
import store from '@/store'

import {
  goWhereListOption,
  isReturnsOption,
  startAreasOption,
  endAreasOption,
  letPeopleLevelOption,
  letItemsOption,
  letGoodsTypeOption
} from './const'
export default {
  components: {
    page3Tab,
    page3Bottom,
    personList,
    goodsList
  },
  data() {
    return {
      goWhere: goWhereListOption, // 放行去处
      isReturns: isReturnsOption, // 是否返厂
      startAreas: startAreasOption, // 出发地点
      endAreas: endAreasOption, // 到达地点
      letPeopleLevel: letPeopleLevelOption, // 放行人级别
      letItems: letItemsOption, // 放行事项
      letGoodsType: letGoodsTypeOption, // 物品放行类别
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
      applyGoodsWorkInfo: {},
      goodsPersonInfo: [],
      releaseGoodsInfo: [],
      baseInfo: {},
      parkInfo: {},
      fxsxValue: null
    }
  },
  computed: {},
  props: {},
  watch: {
    'applyGoodsWorkInfo.fxsx'(val) {
      if (val && val.label) {
        this.fxsxValue = val.value
      }
    }
  },
  methods: {
    /**
     * 添加人员
     */
    openAddPerson() {
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/goodReleaseWork/addPersonList'
      })
    },
    /**
     * 添加物品
     */
    openAddGoods() {
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/goodReleaseWork/addGoodsList'
      })
    },
    // 放行事项change
    fxsxSelectHandle(obj) {
      this.fxsxValue = obj.selectedVal[0]
    },
    /**
     * 提交
     */
    async apply() {
      this.localStorageSave()
      await this.$refs.tceForm.verification()
      let formData = this.$refs.tceForm.formData

      let personList = []
      let thingList = []

      // 可能有放行人员数据
      if (formData.fxsx.value === 0 || formData.fxsx.value === 7) {
        this.goodsPersonInfo.forEach(el => {
          personList.push(
            {
              gh: el.gh,
              xm: el.xm,
              name: el.name,
              lcsy: el.lcsy,
              lcrq: el.lcDate.split(' ')[0],
              lcsj: el.lcDate.split(' ')[1]
            }
          )
        })
      } else {
        this.releaseGoodsInfo.forEach(el => {
          thingList.push(
            {
              wpbm: el.wpbm,
              wpmc: el.wpmc,
              wpdw: el.wpdw,
              wpsl: el.wpsl,
              jsdw: el.jsdw,
              fxrq: el.fxrq.split(' ')[0],
              bz: el.bz,
              ysfs: el.ysfs.value,
              xm: el.xm,
              name: el.name,
              cph: el.cph
            }
          )
        })
      }
      let obj = {
        applyMain: {
          fxqc: formData.fxqc.value,
          sffc: formData.sffc.value,
          fxdd: formData.fxdd.value,
          fxddxq: formData.fxddxq,
          dddd: formData.dddd.value,
          ddddxq: formData.ddddxq,
          sqrjb: formData.sqrjb.value,
          fxsx: formData.fxsx.value,
          wpfxlb: formData.wpfxlb.value,
          fjsc: formData.fjsc
        },
        badge: this.baseInfo.employeeBadge,
        parkId: this.parkInfo.id,
        status: 1, // 固定的
        personList: personList,
        thingList: thingList
      }
      this.$loading.show()
      const res = await saveApi(obj)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.clearStorage()
        this.$router.push({
          path: '/xuchang/goodReleaseWork/list'
        })
      } else {
        this.$tceMobile.toast(res.message)
      }
    },
    /**
     * 发起提交
     */
    doApply() {
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/goodReleaseWork'
      })
    },
    /**
     * 查看数据
     */
    checkList() {
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/goodReleaseWork/list'
      })
    },
    localStorageSave() {
      let formData = this.$refs.tceForm.formData
      this.applyGoodsWorkInfo = formData
      localStorage.setItem('applyGoodsWorkInfo', JSON.stringify(this.applyGoodsWorkInfo))
    },
    storageLoad() {
      let obj = JSON.parse(localStorage.getItem('applyGoodsWorkInfo')) // 物品放行（办公区）申请信息
      if (obj) {
        this.applyGoodsWorkInfo = obj
      }
      let arr1 = JSON.parse(localStorage.getItem('goodsPersonInfo')) // 放行人员信息
      if (arr1 && arr1.length > 0) {
        this.goodsPersonInfo = arr1
      }
      let arr2 = JSON.parse(localStorage.getItem('releaseGoodsInfo')) // 放行物品辆信息
      if (arr2 && arr2.length > 0) {
        this.releaseGoodsInfo = arr2
      }
    },
    clearStorage() {
      localStorage.setItem('applyGoodsWorkInfo', null)
      localStorage.setItem('goodsPersonInfo', null)
      localStorage.setItem('releaseGoodsInfo', null)
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.baseInfo = store.getters.userInfoBase
    this.parkInfo = store.getters.parkInfo
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
  .page3{
    background: $TCE-Background-Grey;
    width: 100%;
    min-height: 100%;
    padding: rem(120) rem(20) rem(190);
    .business-form-group{
      margin-bottom: rem(20);
    }
    .btn-inner{
      width: 100%;
      height: 100%;
      padding: 0 rem(52);
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .personOuter{
      position: relative;
      padding: rem(30) rem(60) rem(10) rem(30);
      .-arrow{
        position: absolute;
        top: rem(50);
        right: rem(30);
      }
    }
  }
</style>
