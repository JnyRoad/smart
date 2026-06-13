<template>
  <div class="business-form-item clearance"  v-if="!disable">
    <div class="business-form-item__label -no-required" :class="{ '-required': required }">{{ label }}</div>
    <div class="business-form-item__value">
      <slot name="itemholder"></slot>
      <!-- <div class="business-input">
        <slot></slot>
        <input class="business-input__inner no-border tce-input__inner" type="file" cursor-spacing="100" :placeholder="placeholder" />
      </div> -->
    </div>
  </div>
</template>

<script>
import mixin from './mixin.js'
export default {
  mixins: [mixin],
  data() {
    return {}
  },
  computed: {
    computedFormOption() {
      return Object.assign(
        {
          maxlength: 20
        },
        this.formOption
      )
    }
  },
  props: {
    placeholder: {
      type: String,
      default: '请输入'
    }
  },
  methods: {}
}
</script>

<style lang="scss" scoped>
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
    padding: 0 12px;
  }
}
</style>
