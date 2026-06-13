<!--
- @name 物品放行-生活区-发起提交
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-10
-->

<template>
  <div class="page3">
    <page3Tab :list="tabList" :curIndex="0" @doApply="doApply" @checkList="checkList"></page3Tab>
    <tce-form ref="tceForm">
      <tce-form-group>
        <tce-form-item
          required
          field="articles"
          type="picker"
          :valueData="{ label: '宿舍生活物品', value: 3 }"
          :formOption="{
            opt: letGoodsType
          }"
          label="物品类型"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item required field="articlesDesc" requiredMessage="请输入物品名称" type="input" label="物品名称" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="carrier" :readonly="true" :valueData="baseInfo.employeeName" requiredMessage="请输入携带人名称" type="input" label="携带人名称" placeholder="请输入"></tce-form-item>
        <tce-form-item
          required
          field="roomInfo"
          type="picker"
          :formOption="{
            opt: rooms
          }"
          label="房间信息"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item required requiredMessage="请选择预计离厂日期" field="plannedDepartureTime" type="time-picker" label="预计离厂日期" placeholder="请输入"></tce-form-item>
        <tce-form-item requiredMessage="请输入车牌号" field="licensePlate" type="plate-number" label="车牌号" placeholder="请输入"></tce-form-item>
      </tce-form-group>
      <tce-form-group>
        <tce-form-item field="remarks" requiredMessage="请输入" type="input" label="备注" placeholder="请输入"></tce-form-item>
      </tce-form-group>
      <tce-form-group>
        <tce-form-item required field="imgs" type="upload-image-base64" label="上传物品照片" placeholder="请输入"></tce-form-item>
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
import { getRoomDetail, saveApi } from '@/services/goodRreleaseLive'
import store from '@/store'

import {
  letGoodsTypeOption
} from './const'
export default {
  components: {
    page3Tab,
    page3Bottom
  },
  data() {
    return {
      letGoodsType: letGoodsTypeOption,
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
      applyGoodsLiveInfo: {},
      rooms: [],
      baseInfo: {},
      parkInfo: {}
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    async apply() {
      if (!this.rooms || this.rooms.length === 0) {
        this.$tceMobile.toast('没有查询到您的房间信息，不能进行物品放行（生活区）申请')
        return
      }
      await this.$refs.tceForm.verification()
      let formData = this.$refs.tceForm.formData
      let obj = {
        articlesDesc: formData.articlesDesc,
        articlesType: formData.articles.value,
        badge: this.baseInfo.employeeBadge,
        parkId: this.parkInfo.id,

        dormitoryId: formData.roomInfo.value.split('/')[0],
        floorId: formData.roomInfo.value.split('/')[1],
        roomId: formData.roomInfo.value.split('/')[2],
        bedId: formData.roomInfo.value.split('/')[3],
        carrier: formData.carrier,
        licensePlate: formData.licensePlate,
        name: this.baseInfo.employeeName,
        plannedDepartureTime: formData.plannedDepartureTime,
        remarks: formData.remarks,
        status: 1, // 固定的
        oneImg: formData.imgs[0] ? formData.imgs[0] : '',
        twoImg: formData.imgs[1] ? formData.imgs[1] : '',
        threeImg: formData.imgs[2] ? formData.imgs[2] : ''
      }
      // return
      this.$loading.show()
      const res = await saveApi(obj)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.checkList()
      } else {
        this.$tceMobile.toast(res.message)
      }
    },
    /**
     * 发起提交
     */
    async doApply() {
      this.$router.push({
        path: '/xuchang/goodReleaseLive'
      })
    },
    /**
     * 查看数据
     */
    checkList() {
      this.$router.push({
        path: '/xuchang/goodReleaseLive/list'
      })
    },
    async getRoomDetail() {
      const res = await getRoomDetail(this.baseInfo.employeeBadge)
      if (res.code === 0) {
        let arr = []
        if (res.data.data && res.data.data.length > 0) {
          res.data.data.forEach(el => {
            arr.push(
              {
                value: `${el.dormitoryId}/${el.floorId}/${el.roomId}/${el.id}`,
                label: `${el.dormitoryName}/${el.floorName}层/${el.roomName}号房/${el.bedNumber}床`
              }
            )
          })
          this.rooms = arr
        } else {
          this.$tceMobile.toast('没有查询到您的房间信息，不能进行物品放行（生活区）申请')
        }
      } else {
        this.$tceMobile.toast(res.message)
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.baseInfo = store.getters.userInfoBase
    this.parkInfo = store.getters.parkInfo
    this.getRoomDetail()
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
