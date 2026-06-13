<template>
  <el-select
    v-model="currVal"
    placeholder="请选择"
    @change="handleChange"
    :disabled="myDisabled"
    clearable
  >
    <template v-for="(item, index) in dataList">
      <el-option :label="item.label" :value="item.value" :key="index"></el-option>
    </template>
  </el-select>
</template>

<script>

import request from "axios";
import { validatenull } from "@/util/validate";
const getList = function(params) {
  return request({
    url: `/platform/dormitory/floor/queryFloor`,
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
    dormitoryId: undefined,
    myDisabled: false
  },
  mounted() {
    this.currVal = this.value;
    this.parkId && this.dormitoryId && this.getDataList({
      parkId: this.parkId,
      dormitoryId: this.dormitoryId
    });
  },
  watch: {
    value(val, oldval) {
      this.currVal = val;
    },
    currVal(val) {
      this.$emit("input", val);
    },
    dormitoryId:{
			handler: function(val){
        val && this.getDataList({
          parkId: this.parkId,
          dormitoryId: val
        });
      },
			immediate: true
		}
  },
  methods: {
    async getDataList(obj) {
      let res = await getList(obj);
      this.dataList = [];
      res.data.data.forEach(element => {
        this.dataList.push({ value: element.id, label: element.floorName });
      });
    },
    handleChange(id) {
      this.$emit("doChange", id);
      if (!validatenull(id)) {
        let obj = this.dataList.find(item => {
          if (item.value === id) {
            return item;
          }
        });
        if (obj !== null) {
          // 父组件通过getItem方法获取选中的item
          this.$emit("getItem", obj);
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
