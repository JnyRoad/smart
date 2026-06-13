<template>
  <div class="business-form-item clearance textarea" v-if="!disable">
    <div class="business-form-item__label">
      <div class="-no-required" :class="{ '-required': required }">{{ label }}</div>
      <div class="limit -no-required">{{ textLen }}/{{ computedFormOption.maxlength }}</div>
    </div>
    <div class="business-form-item__value">
      <textarea
        class="business-form_textarea"
        :style="{ height: computedFormOption.height }"
        v-model="textString"
        :placeholder="placeholder"
        :maxlength="computedFormOption.maxlength"
        :autoHeight="true"
      ></textarea>
    </div>
  </div>
</template>

<script>
import mixin from './mixin.js'
export default {
  mixins: [mixin],
  data() {
    return {
      textString: ''
    }
  },
  computed: {
    computedFormOption() {
      return Object.assign(
        {
          maxlength: 50,
          height: '88px'
        },
        this.formOption
      )
    },
    textLen() {
      return this.textString.length
    }
  },
  watch: {
    textString(val) {
      this.formData = val
    }
  },
  props: {
    placeholder: {
      type: String,
      default: '请输入'
    }
  },
  methods: {
    reset() {
      this.textString = ''
    },
    syncValue(val) {
      this.textString = val
    }
  }
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
    padding: 0 10px;
  }
  .limit {
    position: absolute;
    top: 35px;
    left: 10px;
    line-height: 1;
    font-size: 12px;
  }
}

.business-form_textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 12px;
  border: none;
  text-align: right;
  padding-top: 0;
  font-size: 14px;
}
</style>
