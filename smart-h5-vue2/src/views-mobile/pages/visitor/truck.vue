<!--
- @name 货车预约
-->
<template>
  <div class="page3">
    <tce-form ref="tceForm">
      <tce-form-group>
        <tce-form-item type="item-holder" label="货车预约信息" placeholder="" class="title"></tce-form-item>
        <tce-form-item required field="plate" :valueData="visitorInfo.plate" type="plate-number" label="车牌号" placeholder="请输入"></tce-form-item>
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
        <tce-form-item required field="name" :valueData="visitorInfo.name" requiredMessage="请输入访客姓名" type="input" label="访客姓名" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="company" :valueData="visitorInfo.company" requiredMessage="请输入出发地" type="input" label="出发地" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="startTime" :valueData="visitorInfo.startTime" type="time-picker" label="预约时间" placeholder="请选择"></tce-form-item>
        <tce-form-item field="remark" :valueData="visitorInfo.remark" type="input" label="备注" placeholder="请填写内托/原材/成品/其他"></tce-form-item>
        <tce-form-item type="item-holder" label="手机验证" placeholder="" class="title"></tce-form-item>
        <!-- <tce-form-item required type="input" label="手机号" field="visitorPhone" :valueData="visitorInfo.visitorPhone" placeholder="请输入"> </tce-form-item> -->
        <tce-form-item type="item-holder" label="手机号" placeholder="请输入">
          <template slot="itemholder">
            <div class="input-outer">
              <input v-model="visitorPhone" style="text-align: right;width:100%" placeholder="请输入"/>
            </div>
          </template>
        </tce-form-item>
        <tce-form-item type="item-holder" label="验证码" placeholder="请输入">
          <template slot="itemholder">
            <div class="input-outer">
              <input placeholder="请输入" :type="inputType" v-model="smsCode" />
              <identifyingCode class="identifying-code" :phone="visitorPhone" :duration="120"></identifyingCode>
            </div>
          </template>
        </tce-form-item>
      </tce-form-group>
    </tce-form>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button @click="next" :loading="addLoading" class="tce-button tce-button--primary">
          <template>提交申请</template>
        </button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { truckCauseApi, carApplySaveApi } from '@/services/visitor'
import { verifyPhoneMessage } from '@/services/other'
import identifyingCode from '@/components/identifying-code/index.vue'
import isMobile from '@/util/isMobile'
import executeOnce from '@/util/executeOnce'
const executeOnceSubmit = executeOnce().executeOnce

export default {
  components: {
    page3Bottom,
    identifyingCode
  },
  data() {
    return {
      addLoading: false,
      causeList: [], // 来访事由
      visitorInfo: {
        plate: null,
        cause: null,
        name: null,
        company: null,
        startTime: null,
        remark: null
      },
      smsCode: null,
      visitorPhone: null,
      inputType: 'number'
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
    phoneChange(val) {
    },
    async next() {
      if (!this.visitorPhone) {
        this.$tceMobile.toast('请输入手机号')
        return
      } else if (!isMobile(this.visitorPhone)) {
        this.$tceMobile.toast('手机号格式不正确')
        return
      }
      if (!this.smsCode) {
        this.$tceMobile.toast('请输入验证码')
        return
      }
      await this.$refs.tceForm.verification()
      this.verifyPhoneMessage()
    },
    async verifyPhoneMessage() {
      this.addLoading = true
      this.$loading.show('正在提交...')
      const res = await verifyPhoneMessage({
        mobile: this.visitorPhone,
        smsCode: this.smsCode
      })
      if (res.code === 0) {
        this.applySaveApi()
      } else {
        this.addLoading = false
        this.$loading.hide()
        this.$tceMobile.toast(res.message)
      }
    },
    async applySaveApi() {
      let formData = this.$refs.tceForm.formData
      const carData = {
        name: formData.name,
        plate: formData.plate
      }
      const obj = {
        visitorName: formData.name,
        visitorPhone: this.visitorPhone,
        remark: formData.remark,
        cause: formData.cause.value,
        startTime: formData.startTime + ':00',
        company: formData.company,
        // parkId: PARKID,
        vehicleList: [carData]
      }
      // this.addLoading = true
      const awaitRes = await executeOnceSubmit.done(carApplySaveApi(obj))
      this.addLoading = false
      this.$loading.hide()
      if (awaitRes.code === 0) {
        await awaitRes.done('申请成功')
        this.$router.push({
          path: '/xuchang/visitor/resultTruck'
        })
      } else {
        this.$tceMobile.toast(awaitRes.message)
      }
    },
    /**
     * OA入厂申请事由枚举
     */
    async getEnumCause() {
      const res = await truckCauseApi()
      this.causeList = res.data
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.getEnumCause()
  },
  /**
   * 生命周期 mounted
   */
  mounted() {}
}
</script>

<style lang="scss" scoped>
.page3 {
  background: $TCE-Background-Grey;
  width: 100%;
  min-height: 100%;
  padding: rem(20) rem(20) rem(190);
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
  .input-outer {
    display: flex;
    align-items: center;
    height: 100%;
    justify-content: space-between;
    padding: 0 12px;
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
  .page3-bottom {
    box-shadow: none;
    z-index: 1;
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
