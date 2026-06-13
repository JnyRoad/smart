<!--
- @name 入厂申请-添加车辆
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-26
-->
<template>
  <div class="page3">
    <tce-form ref="tceForm">
      <tce-form-group>
        <tce-form-item required field="plate" :valueData="itemInfo.plate" type="plate-number" label="车牌号" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="name" :valueData="itemInfo.name" requiredMessage="请输入司机姓名" type="input" label="司机姓名" placeholder="请输入"></tce-form-item>
        <!-- <tce-form-item required field="nativePlace" :valueData="itemInfo.nativePlace" requiredMessage="请输入司机籍贯" type="input" label="司机籍贯" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="licenseNo" :valueData="itemInfo.licenseNo" requiredMessage="请输入驾驶证号" type="input" label="驾驶证号" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="emergencyName" :valueData="itemInfo.emergencyName" requiredMessage="请输入紧急联系人" type="input" label="紧急联系人" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="emergencyPhone" :valueData="itemInfo.emergencyPhone" requiredMessage="请输入紧急联络人方式" type="phone" label="紧急联络人方式" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="modle" :valueData="itemInfo.modle" requiredMessage="请输入车型" type="input" label="车型" placeholder="请输入"></tce-form-item>
        <tce-form-item
          required
          field="vehicleType"
          type="picker"
          :valueData="itemInfo.vehicleType"
          :formOption="{
            opt: carTypeList
          }"
          :defaultProps="{label:'desc', value:'code'}"
          label="车辆类型"
          placeholder="请选择车辆类型"
        ></tce-form-item>
        <tce-form-item
          required
          field="colour"
          type="picker"
          :valueData="itemInfo.colour"
          :formOption="{
            opt: carColorList
          }"
          :defaultProps="{label:'desc', value:'code'}"
          label="颜色"
          placeholder="请选择颜色"
        ></tce-form-item> -->
        <tce-form-item
          required
          field="certType"
          type="picker"
          :valueData="itemInfo.certType"
          :formOption="{
            opt: carCertList
          }"
          :defaultProps="{label:'desc', value:'code'}"
          label="证件类型"
          placeholder="请选择证件类型"
        ></tce-form-item>
        <tce-form-item required field="certImg" :valueData="itemInfo.certImg" type="upload-image-single" label="证件照片" placeholder="请上传证件照片"></tce-form-item>
      </tce-form-group>
    </tce-form>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button @click="apply" class="tce-button tce-button--primary is-round is-plain">
          <template v-if="isEdit">确认修改车辆</template>
          <template v-else>确认添加车辆</template>
        </button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { enumCarCertApi, enumCarTypeApi, enumcarColorApi } from '@/services/visitor'
export default {
  components: {
    page3Bottom
  },
  data() {
    return {
      carTypeList: [], // 车辆类型集合
      carColorList: [], // 车辆颜色集合
      carCertList: [], // 车辆证件集合
      carList: [],
      itemInfo: {},
      itemIndex: '',
      isEdit: false,
      visitorInfo: {}
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    /**
     * 提交
     */
    async apply() {
      await this.$refs.tceForm.verification()
      let formData = this.$refs.tceForm.formData
      if (this.isEdit) {
        this.carList.splice(this.itemIndex, 1, formData)
      } else {
        this.carList.push(formData)
      }
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/visitor/addCarList'
      })
    },
    /**
     *车辆类型枚举
     */
    async getEnumCarType() {
      const res = await enumCarTypeApi()
      this.carTypeList = res.data
    },
    /**
     * 车辆颜色枚举
     */
    async getEnumCarColor() {
      const res = await enumcarColorApi()
      this.carColorList = res.data
    },
    /**
     * 车辆证件类型枚举
     */
    async getEnumCarCert() {
      const res = await enumCarCertApi()
      this.carCertList = res.data
    },
    initData() {
      this.itemInfo = {
        // vehicleType: {value: 0, label: "外部车辆"},
        certType: { value: 2, label: '身份证复印件' }
      }
    },
    localStorageSave() {
      localStorage.setItem('carList', JSON.stringify(this.carList))
    },
    storageLoad() {
      // 司机姓名，默认访客姓名
      let obj = JSON.parse(localStorage.getItem('visitorInfo')) // 访客信息
      if (obj) {
        this.visitorInfo = obj
        if (this.visitorInfo.visitorName) {
          this.itemInfo.name = this.visitorInfo.visitorName
        }
      }
      let arr = JSON.parse(localStorage.getItem('carList')) // 车辆信息
      if (arr && arr.length > 0) {
        this.carList = arr
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.getEnumCarType()
    this.getEnumCarColor()
    this.getEnumCarCert()

    this.initData()
    this.storageLoad()
    if (this.$route.query.itemInfo) {
      this.itemInfo = JSON.parse(this.$route.query.itemInfo)
    }
    this.itemIndex = this.$route.query.itemIndex
    this.isEdit = this.$route.query.isEdit
    // localStorage.setItem("carList", JSON.stringify([]))
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
    padding: rem(20) rem(20) rem(190);
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
