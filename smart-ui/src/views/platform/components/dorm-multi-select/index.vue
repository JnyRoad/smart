<template>
  <el-select
    v-model="currVal"
    :placeholder="placeholder"
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
const getList = function(params) {
  return request({
    url: `/platform/dormitory/queryDormitory`,
    method: "post",
    data: params
  });
};
export default {
  name: "deptSelect",
  data() {
    return {
      currVal: undefined,
      dataList: []
    };
  },
  props: {
    value: undefined,
    parkId: undefined,
    defaultSelected: false,
    myDisabled: false,
    placeholder: {
      type: String,
      default: '请选择'
    }
  },
  mounted() {
    this.currVal = this.value;
    // this.parkId && this.getDataList(this.parkId);
  },
  watch: {
    value(val, oldval) {
      this.currVal = val;
    },
    currVal(val) {
      this.$emit("input", val);
    },
    parkId:{
			handler: function(val){
        val && this.getDataList(val);
      },
			immediate: true
		}
  },
  methods: {
    async getDataList(parkId) {
      let res = await getList({parkId});
      this.dataList = [];
      res.data.data.forEach(element => {
        this.dataList.push({ value: element.id, label: element.dormitoryName });
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
