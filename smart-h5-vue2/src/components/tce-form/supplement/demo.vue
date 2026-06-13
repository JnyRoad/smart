<template>
  <div class="business-form-item clearance">
    <div class="business-form-item__label -no-required" :class="{ '-required': required }">{{ label }}</div>
    <div class="business-form-item__value">
      <div class="business-input">
        <input class="business-input__inner no-border" type="number" cursor-spacing="100" :maxlength="11" @blur="blurVerify($event)" v-model="formData" :placeholder="placeholder" />
      </div>
    </div>
  </div>
</template>

<script>
import mixin from './mixin.js'
import isMobile from '@/utils/isMobile'
export default {
  mixins: [mixin],
  data() {
    return {}
  },
  props: {
    placeholder: {
      type: String,
      default: '请输入11位有效手机号'
    }
  },
  methods: {
    blurVerify(val) {
      this.verification()
    },
    verification() {
      if (!isMobile(this.formData)) {
        this.clearFormData()
        return this.verifyFail('手机号格式错误')
      }
    }
  }
}
</script>

<style lang="scss">
.business-form-item {
  position: relative;
  @include border-bottom();
  &.is-noborder {
    &::after {
      border-bottom-color: transparent;
    }
  }
  .business-form-item__label {
    margin-left: 10px;
    line-height: 45px;
    height: 45px;
    font-size: 14px;
    color: #666666;
  }
  .business-form-item__value {
    flex: 1;
  }
  .business-input__inner {
    line-height: 45px;
    height: 45px;
    text-align: right;
    padding: 0 10px;
  }
}
</style>
