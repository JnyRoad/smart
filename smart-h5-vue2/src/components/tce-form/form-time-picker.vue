<template>
  <div class="business-form-item clearance" v-if="!disable">
    <div class="business-form-item__label -no-required" :class="{ '-required': required }">{{ label }}</div>
    <div class="business-form-item__value">
      <div class="tce-list-item" :class="{ placeholder: !formData }" @click="showDateTimePicker">
        {{ formData || placeholder }}
        <div class="tce-list__arrow"></div>
      </div>
    </div>
  </div>
</template>

<script>
import mixin from './mixin.js'
import { transformDate } from '@/util/date'
export default {
  mixins: [mixin],
  components: {},
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
  watch: {
    valueData(val) {
      if (val || val === 0) {
        this.formData = val
      } else if (val === '') {
        this.formData = ''
      }
    },
    formData(val) {
      if (val) {
        this.valueDate = new Date(transformDate(val))
      }
    }
  },
  methods: {
    showDateTimePicker() {
      if (!this.dateTimePicker) {
        this.dateTimePicker = this.$createDatePicker({
          title: '',
          min: new Date(),
          max: new Date(2045, 12, 1, 0, 0, 0),
          value: this.valueDate,
          columnCount: 5,
          format: {
            year: 'YYYY',
            month: 'MM',
            date: 'DD'
          },
          onSelect: this.selectHandle,
          onCancel: null
        })
      }
      this.dateTimePicker.show()
    },
    selectHandle(date, selectedVal, selectedText) {
      this.formData = `${selectedText[0]}-${selectedText[1]}-${selectedText[2]} ${selectedText[3]}:${selectedText[4]}`
      this.$emit('timeSelectHandle', this.formData)
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
