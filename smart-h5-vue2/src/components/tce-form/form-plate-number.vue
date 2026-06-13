<template>
  <div class="business-form-item clearance" v-if="!disable">
    <div class="business-form-item__label -no-required" :class="{ '-required': required }">{{ label }}</div>
    <div class="business-form-item__value">
      <div class="tce-list-item" :class="{ placeholder: !formData }" @click="$refs.plateNumber.start()">
        {{ formData || placeholder }}
        <div class="tce-list__arrow"></div>
      </div>
    </div>
    <plateNumber ref="plateNumber" v-model="formData"></plateNumber>
  </div>
</template>

<script>
import plateNumber from '@/components/plateNumber/plateNumber'
import mixin from './mixin.js'
export default {
  mixins: [mixin],
  components: {
    plateNumber
  },
  data() {
    return {
      valueDate: new Date()
    }
  },
  props: {
    placeholder: {
      type: String,
      default: '请选择'
    }
  },
  watch: {},
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
    text-align: right;
  }
}

.tce-list-item {
  line-height: 45px;
  text-align: right;
  align-items: flex-end;
  display: block;
  padding-right: 27px;
  &.placeholder {
    color: #c0c0c0;
  }
}
</style>
