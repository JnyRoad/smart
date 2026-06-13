<!--设备类型下拉-->
<template>
  <el-select v-model="selectValue" @change="handleChange" :placeholder="placeholder" :clearable="clearable" :disabled="disabled" :filterable="filterable">
    <el-option
      v-for="(item, index) in dataList"
      :key="index"
      :label="item.label"
      :value="item.id"
    ></el-option>
  </el-select>
</template>

<script>
const OPTION = [
  { id: 1, label: '闸机' },
  { id: 2, label: '门禁' },
  { id: 3, label: '道闸' }
]
export default {
  data() {
    return {
      selectValue: '',
      dataList: OPTION
    }
  },
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    filterable: {
      type: Boolean,
      default: true
    },
    clearable: {
      type: Boolean,
      default: true
    },
    placeholder: {
      type: String,
      default: ''
    },
    value: [String, Number]
  },
  watch: {
    value(val, oldVal) {
      if (val === oldVal) {
        return
      }
      this.selectValue = val
    },
    selectValue(val, old) {
      if (val === old) {
        return
      }
      this.$emit('input', val)
    }
  },
  async mounted() {},
  methods: {
    handleChange() {
      this.$emit('change', this.selectValue)
    }
  }
}
</script>

<style>
</style>
