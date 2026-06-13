<!--
- @name 宿舍报修
-->

<template>
  <div class="page3">
    <page3Tab :list="tabList" :curIndex="0" @doApply="doApply" @checkList="checkList"></page3Tab>
    <tce-form ref="tceForm">
      <tce-form-group>
        <tce-form-item
          required
          field="rangeType"
          type="picker"
          :formOption="{
            opt: rangeTypeList
          }"
          label="维修区域"
          placeholder="请选择"
          @pickerSelectHandle="rangeTypeChange"
        ></tce-form-item>
        <tce-form-item
          required
          field="dormitoryName"
          :valueData="rangeInfo.dormitoryName"
          type="picker"
          :formOption="{
            opt: dormitoryList
          }"
          label="所在楼栋"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item
          required
          field="repairType"
          type="picker"
          :formOption="{
            opt: repairTypeList
          }"
          label="维修类别"
          placeholder="请选择"
        ></tce-form-item>
        <!-- <tce-form-item required field="dormitoryName" requiredMessage="请输入所在楼栋" type="input" label="所在楼栋" placeholder="请输入"></tce-form-item> -->
        <tce-form-item required field="roomName" requiredMessage="请输入所在房间" type="input" label="所在房间" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="faultDesc" requiredMessage="请输入故障描述" type="textarea" label="故障描述" placeholder="请输入"></tce-form-item>
      </tce-form-group>

      <tce-form-group>
        <tce-form-item field="imgs" type="upload-image-base64" label="上传物品照片" placeholder="请输入"></tce-form-item>
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
import { dormRepairRange, saveRepairRange } from '@/services/dormRepairs'
import store from '@/store'

import {
  REPAIRTYPEOPTION,
  AREAOPTION
} from './const'
export default {
  components: {
    page3Tab,
    page3Bottom
  },
  data() {
    return {
      rangeTypeList: AREAOPTION,
      repairTypeList: REPAIRTYPEOPTION,
      dormitoryList: [],
      rangeInfo: {},
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
      baseInfo: {},
      parkInfo: {}
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    rangeTypeChange(item) {
      let formData = this.$refs.tceForm.formData
      const me = this
      switch (item.selectedVal[0]) {
        case 1:
          me.dormitoryList = [
            { label: '老工厂1号宿舍', value: '老工厂1号宿舍' },
            { label: '老工厂2号宿舍', value: '老工厂2号宿舍' },
            { label: '老工厂3号宿舍', value: '老工厂3号宿舍' },
            { label: '新工厂宿舍楼', value: '新工厂宿舍楼' }]
          break
        case 2:
          me.dormitoryList = [
            { label: '餐厅三楼', value: '餐厅三楼' },
            { label: '北门岗', value: '北门岗' },
            { label: '东门岗', value: '东门岗' },
            { label: '辅房', value: '辅房' }]
          break
        case 3:
          me.dormitoryList = [
            { label: '一楼', value: '一楼' },
            { label: '二楼', value: '二楼' },
            { label: '三楼', value: '三楼' }]
          break
        case 4:
          me.dormitoryList = [
            { label: '园区周边', value: '园区周边' }]
          break
      }
      if (formData.rangeType && formData.rangeType.value !== item.selectedVal[0]) {
        me.rangeInfo['dormitoryName'] = me.dormitoryList[0]
      } else if (!formData.rangeType) {
        me.rangeInfo['dormitoryName'] = me.dormitoryList[0]
      }
    },
    async apply() {
      await this.$refs.tceForm.verification()
      let formData = this.$refs.tceForm.formData
      const obj = {
        rangeType: formData.rangeType.value,
        repairType: formData.repairType.value,
        dormitoryName: formData.dormitoryName.value,
        roomName: formData.roomName,
        faultDesc: formData.faultDesc,
        faultImgs: formData.imgs,
        parkId: this.parkInfo.id
      }
      this.$loading.show()
      const res = await saveRepairRange(obj)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.checkList()
      } else {
        this.$tceMobile.toast(res.message)
      }
    },
    async getDormRepairRange() {
      const res = await dormRepairRange()
      if (res.code === 0 && res.data) {
        this.rangeTypeList = res.data
        this.rangeTypeList.forEach(element => {
          element['label'] = element['desc']
          element['value'] = element['code']
        })
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    /**
     * 发起提交
     */
    async doApply() {
      this.$router.push({
        path: '/xuchang/dormRepairs'
      })
    },
    /**
     * 查看数据
     */
    checkList() {
      this.$router.push({
        path: '/xuchang/dormRepairs/list'
      })
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.baseInfo = store.getters.userInfoBase
    this.parkInfo = store.getters.parkInfo
    this.getDormRepairRange()
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
  }
</style>
