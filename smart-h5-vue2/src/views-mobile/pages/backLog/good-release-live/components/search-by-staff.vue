<template>
  <compoentModal v-model="visible" ref="compoentModal" @load="load" @cancel="cancel" @ok="success" :customOpenAfter="true">
    <template slot="header" v-if="cutomHeader">
      <div class="plate-custom-header">
        <button class="tce-button tce-button--textbtn">按员工信息查找</button>
         <!-- <button class="tce-button tce-button--textbtn" @click="doAnther">按车牌号搜索</button> -->
        <button class="tce-button tce-button--textbtn textbtn2" @click="successCustom">确定</button>
      </div>
    </template>
    <div class="wrap search-by-staff" slot="content">
      <tce-form ref="tceForm">
        <tce-form-group>
          <tce-form-item field="badge" requiredMessage="请输入" type="input" label="工号" placeholder="请输入"></tce-form-item>
          <tce-form-item field="name" requiredMessage="请输入" type="input" label="携带人姓名" placeholder="请输入"></tce-form-item>
          <tce-form-item field="startTime" type="time-picker" label="申请开始时间" placeholder="请输入"></tce-form-item>
          <tce-form-item field="endTime" type="time-picker" label="申请结束时间" placeholder="请输入"></tce-form-item>
        </tce-form-group>
      </tce-form>
    </div>
  </compoentModal>
</template>
<script>
import compoentModal from '@/components/modal/index'
export default {
  data() {
    return {
      visible: false
    }
  },
  components: {
    compoentModal
  },
  props: {
    value: {
      type: String,
      default: ''
    },
    cutomHeader: {
      type: Boolean,
      default: false
    }
  },
  watch: {},
  methods: {
    doAnther() {
      this.$emit('doAnther')
      this.$refs.compoentModal.cancel()
    },
    successCustom() {
      let formData = this.$refs.tceForm.formData
      this.$emit('successCustom', formData)
      this.$refs.compoentModal.ok()
    },
    success(next) {
      this.firstWrapStatus = false
      this.keyBoardStatus = false
      this.activeIndex = -1
      this.submitFn(next)
    },
    submitFn(next) {
      next()
      // this.$emit('getPlateLicense', plateLicense)
      // this.$emit('input', plateLicense)
    },
    start() {
      this.clear()
      this.visible = true
    },
    cancel(next) {
      this.clear()
      next()
    },
    clear() {
      this.$emit('clear', '')
    },
    plate(value) {
      this.formData.commonCard = value
    },
    load() {
      if (this.value) {
        this.edit(this.value)
      }
    }
  },
  computed: {},
  mounted() {}
}
</script>
<style lang="scss">
.search-by-staff{
  height: rem(600);
  .business-form__message{
    display: none;
  }
}
</style>
