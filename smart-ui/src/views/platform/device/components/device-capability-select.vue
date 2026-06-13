<!--卡机类型下拉-->
<template>
  <el-select
    :value="value"
    @input="$emit('input', $event)"
    @change="$emit('change', $event)"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    :filterable="filterable"
  >
    <el-option
      v-for="item in dataList"
      :key="item.id"
      :label="item.label"
      :value="item.id"
    ></el-option>
  </el-select>
</template>

<script>
const OPTION = [
  { id: 1, label: '刷脸机' },
  { id: 2, label: '刷卡机' },
  { id: 3, label: '刷脸+刷卡' }
]

export default {
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
      default: '请选择卡机类型'
    },
    value: [String, Number]
  },
  data() {
    return {
      dataList: OPTION
    }
  }
}
</script>
