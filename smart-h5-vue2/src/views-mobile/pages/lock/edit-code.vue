<template>
  <compoentModal v-model="visible" ref="compoentModal" @load="load" @cancel="cancel" @ok="success" :customOpenAfter="true">
    <template slot="header" v-if="cutomHeader">
      <div class="plate-custom-header">
        <button class="tce-button tce-button--textbtn">修改动态码</button>
        <button class="tce-button tce-button--textbtn textbtn2" @click="successCustom">确定</button>
      </div>
    </template>
    <div class="wrap search-by-staff" slot="content">
      <tce-form ref="tceForm">
        <tce-form-group>
          <tce-form-item field="newPwd" required requiredMessage="请输入6位数字动态码" type="input" label="动态码" placeholder="请输入6位数字动态码"></tce-form-item>
        </tce-form-group>
      </tce-form>
    </div>
  </compoentModal>
</template>
<script>
import compoentModal from '@/components/modal/index'
import { editPwd } from '@/services/lock'
import store from '@/store'
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
    },
    curCode: {
      type: String,
      default: ''
    }
  },
  watch: {},
  methods: {
    async editPwd(newPwd) {
      const baseInfo = store.getters.userInfoBase
      const employeeBadge = baseInfo.employeeBadge
      const res = await editPwd({ badge: employeeBadge, newPwd })
      if (res.code === 0) {
        this.$emit('successCustom')
        this.$refs.compoentModal.ok()
      } else {
        this.$createDialog({
          type: 'alert',
          title: '错误',
          content: res.msg,
          icon: 'cubeic-alert'
        }).show()
      }
    },
    async successCustom() {
      let newPwd = this.$refs.tceForm.formData.newPwd
      if (!/^[0-9]{6}$/.test(newPwd)) {
        this.$tceMobile.toast('请输入6位数字动态码')
        return
      } else if (newPwd === this.curCode) {
        this.$tceMobile.toast('请输入跟当前动态码不一样的新的动态码')
        return
      }
      this.editPwd(newPwd)
    },
    success(next) {
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
  height: rem(500);
  .business-form__message{
    display: none;
  }
}
</style>
