<template>
  <el-select
    v-model="currVal"
    placeholder="请选择（可多选）"
    @change="handleChange"
    :disabled="myDisabled"
    multiple
    collapse-tags
    clearable
  >
    <template v-for="(item, index) in dataList">
      <el-option :label="item.label" :value="item.value" :key="index"></el-option>
    </template>
  </el-select>
</template>

<script>

import request from "axios";
const getList = function() {
  return request({
    url: `/platform/device/tag/list`,
    method: "get"
  });
};
export default {
  name: "deptSelect",
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
    parkId: undefined,
    dormitoryId: undefined,
    myDisabled: false
  },
  mounted() {
    this.currVal = this.value;
    this.getDataList()
  },
  watch: {
    value(val, oldval) {
      this.currVal = val;
    },
    currVal(val) {
      this.$emit("input", val);
    },
  },
  methods: {
    async getDataList() {
      let res = await getList();
      this.dataList = [];
      res.data.data.forEach(element => {
        this.dataList.push({ value: element.id, label: element.tagName });
      });
    },
    handleChange(id) {
      this.$emit("doChange", id);
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
