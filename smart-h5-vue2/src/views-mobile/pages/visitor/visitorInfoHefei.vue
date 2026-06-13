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
        <tce-form-item type="item-holder" label="访客信息（必填）" placeholder="" class="title"></tce-form-item>
        <tce-form-item
          required
          field="visitorName"
          :valueData="visitorInfo.visitorName"
          requiredMessage="请输入访客姓名"
          type="input"
          label="访客姓名"
          placeholder="请输入"
        ></tce-form-item>
        <tce-form-item
          required
          field="cause"
          type="picker"
          @pickerSelectHandle="causeChange"
          :valueData="visitorInfo.cause"
          :formOption="{
            opt: causeList
          }"
          :defaultProps="{ label: 'desc', value: 'code' }"
          label="来访事由"
          placeholder="请选择"
        ></tce-form-item>
        <!-- 检测人脸 -->
        <tce-form-item required field="visitorPhotoId" :valueData="visitorInfo.visitorPhotoId" type="upload-image-single-face" label="访客照片"></tce-form-item>
        <!-- 普通上传 -->
        <!-- <tce-form-item required field="visitorPhotoId" :valueData="visitorInfo.visitorPhotoId" type="upload-image-single" label="访客照片"></tce-form-item> -->
        <tce-form-item
          required
          field="certType"
          type="picker"
          :valueData="visitorInfo.certType"
          :formOption="{
            opt: personCertList
          }"
          :defaultProps="{ label: 'desc', value: 'code' }"
          label="证件类型"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item
          required
          field="certNo"
          :valueData="visitorInfo.certNo"
          requiredMessage="请输入证件号码"
          type="input"
          label="证件号码"
          placeholder="请输入"
        ></tce-form-item>
        <tce-form-item required v-if="cause == 5 || cause == 7" field="visitorFrontPhoto" :valueData="visitorInfo.visitorFrontPhoto"  type="upload-image-single" label="证件照片" placeholder="请上传证件照片"></tce-form-item>
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
          field="carryThing"
          type="picker"
          :valueData="visitorInfo.carryThing"
          :formOption="{
            opt: carryList
          }"
          :defaultProps="{ label: 'desc', value: 'code' }"
          label="携带物品"
          placeholder="请选择"
        ></tce-form-item>
        <tce-form-item required field="tripCode" :valueData="visitorInfo.tripCode" v-if="isShowCode.isTripCode == 1" type="upload-image-single" label="行程码" placeholder="请上传行程码照片"></tce-form-item>
        <tce-form-item required field="healthcode" :valueData="visitorInfo.healthcode" v-if="isShowCode.isHealthCode == 1" type="upload-image-single" label="健康码" placeholder="请上传健康码照片"></tce-form-item>
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
        <div class="tip">
          <p class="p1"></p>
        </div>
        <tce-form-item type="item-holder" label="其他信息（选填）" placeholder="" class="title"></tce-form-item>
        <tce-form-item field="plate" :valueData="visitorInfo.plate" type="plate-number" label="车牌号" placeholder="请输入"></tce-form-item>
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
import { enumPersonCertApi, enumCauseApiHefei, enumCarryApi, getIsShowCode } from '@/services/visitor'
import { transformDate } from '@/util/date'

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
      cause: null,
      carryList: [], // 携带物品类型
      // personTypeList: [
      //   { label: '普通来访', value: 3 },
      //   { label: '贵宾来访', value: 2 }
      // ],
      // personType: 3,
      visitorInfo: {
        startTime: '',
        endTime: ''
      },
      isShowCode: {
        isHealthCode: 1,
        isTripCode: 1
      },
      hostInfor: {},
      fellowInfo: [],
      carList: [],
      addLoading: false
    }
  },
  computed: {},
  props: {},
  watch: {
  },
  methods: {
    async next() {
      // let formData = this.$refs.tceForm.formData
      this.localStorageSave()
      await this.$refs.tceForm.verification()
      this.$router.push({
        path: '/xuchang/visitor/telHefei'
      })
    },
    addPerson() {
      if (this.cause !== null) {
        this.localStorageSave()
        this.$router.push({
          path: '/xuchang/visitor/addPersonlist',
          query: {
            idHefei: true,
            cause: this.cause
          }
        })
      } else {
        this.$tceMobile.toast('请选择来访事由!')
      }
    },
    addCar() {
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/visitor/addCarlist'
      })
    },
    causeChange(item) {
      this.cause = item.selectedVal[0]
    },
    startTimeHandle(time) {
      this.visitorInfo.startTime = time
      this.clearEndTime()
    },
    endTimeHandle(time) {
      let endTimeDate = new Date(time)
      let startTimeDate = new Date(transformDate(this.visitorInfo.startTime))
      if (endTimeDate <= startTimeDate) {
        this.$tceMobile.toast('离开时间应大于开始时间!')
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
      const res = await enumCauseApiHefei()
      this.causeList = res.data
    },
    /**
     * 是否显示健康码和行程码
     */
    async getIsShowCode() {
      const res = await getIsShowCode(20381)
      this.isShowCode = res.data
    },
    /**
     * 携带类型枚举
     */
    async getEnumCarry() {
      const res = await enumCarryApi()
      this.carryList = res.data
    },
    localStorageSave() {
      let formData = this.$refs.tceForm.formData
      this.visitorInfo = formData
      localStorage.setItem('visitorInfo', JSON.stringify(this.visitorInfo))
    },
    async storageLoad() {
      let obj = JSON.parse(localStorage.getItem('visitorInfo')) // 访客信息
      if (obj) {
        this.visitorInfo = Object.assign({ startTime: '', endTime: '' }, obj)
        this.cause = obj.cause && obj.cause.code
      }
      let obj2 = JSON.parse(localStorage.getItem('hostInfor')) // 被访人信息
      if (obj2) {
        this.hostInfor = obj2
      }

      let arr1 = JSON.parse(localStorage.getItem('fellowInfo')) // 随行人员信息
      if (arr1 && arr1.length > 0) {
        this.fellowInfo = arr1
      }
      // let arr2 = JSON.parse(localStorage.getItem('carList')) // 车辆信息
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
    this.getEnumCarry()
    this.getIsShowCode()
    this.storageLoad()
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
  .area-btn-inner {
    width: 100%;
    height: 100%;
    padding-right: rem(60);
    display: flex;
    justify-content: right;
    align-items: center;
    button {
      height: rem(60);
      margin-left: rem(30);
    }
    .is-check {
      i {
        color: #00c297;
      }
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
