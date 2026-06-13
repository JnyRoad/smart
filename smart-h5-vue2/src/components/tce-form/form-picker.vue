<template>
  <div class="business-form-item clearance" v-if="!disable">
    <div class="business-form-item__label -no-required" :class="{ '-required': required }">{{ label }}</div>
    <div class="business-form-item__value">
      <div class="tce-list-item" :class="{ placeholder: !formData }" @click="showPicker">
        {{ (formData && formData.label) || placeholder }}
        <div class="tce-list__arrow"></div>
      </div>
    </div>
  </div>
</template>

<script>
import mixin from './mixin.js'
export default {
  mixins: [mixin],
  components: {},
  data() {
    return {
      enumerationData: [],
      selectedIndex: 0
    }
  },
  props: {
    placeholder: {
      type: String,
      default: '请选择'
    },
    defaultProps: {
      type: Object,
      default: function() {
        return {
          label: 'label',
          value: 'value'
        }
      }
    }
  },
  watch: {
    formOption: {
      handler: function (val) {
        if (val && val.opt) {
          this.enumerationData = val.opt
        }
      },
      deep: true
    },
    formData(val) {
      if (val) {
        const i = this.enumerationData.findIndex((item) => {
          return item.value === val.value
        })
        this.selectedIndex = i <= 0 ? 0 : i
      }
    },
    enumerationData(val) {
      if (val && val.length > 0) {
        val.forEach(el => {
          el.label = el[this.defaultProps.label]
          el.value = el[this.defaultProps.value]
        })
      }
    }
  },
  methods: {
    showPicker() {
      // if (!this.picker) {
      this.picker = this.$createPicker({
        title: '',
        selectedIndex: [this.selectedIndex],
        data: [this.enumerationData],
        alias: { value: 'value', text: 'label' },
        onSelect: this.selectHandle
      })
      // }
      this.picker.show()
    },
    selectHandle(selectedVal, selectedIndex, selectedText) {
      this.formData = this.enumerationData[selectedIndex]
      this.selectedIndex = selectedIndex
      this.$emit('pickerSelectHandle', { selectedVal, selectedIndex, selectedText })
    }
  },
  async mounted() {
    if (this.formOption.opt) {
      this.enumerationData = this.formOption.opt
    }
  },
  beforeDestroy() {
    this.enumerationData = null
    this.picker = null
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
