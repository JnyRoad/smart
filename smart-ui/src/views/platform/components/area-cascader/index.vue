<template>
  <el-cascader
    :options="dataList"
    v-model="currVal"
    :placeholder="placeholder"
    :change-on-select="changeOnSelect"
    clearable
  ></el-cascader>
</template>

<script>

import request from "axios"
const getList = function() {
  return request({
    url: `/platform/device/area/tree`,
    method: "get"
  });
};
export default {
  data() {
    return {
      currVal: [],
      dataList: []
    };
  },
  props: {
    value: {
      type: Array,
      default: function(){
        return []
      }
    },
    changeOnSelect: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: '请选择'
    }
  },
  mounted() {
    this.currVal = this.value
    this.getDataList()
  },
  watch: {
    value(val, oldval) {
      this.currVal = val;
    },
    currVal(val) {
      this.$emit("input", val);
    }
  },
  methods: {
    async getDataList() {
      let res = await getList()
      this.dataList = res.data.data
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
