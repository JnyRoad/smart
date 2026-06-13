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
    myDisabled: false
  },
  mounted() {
    this.currVal = this.value;
    this.parkId && this.getDataList(this.parkId);
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
      if(this.defaultSelected){
        if(this.dataList.length>0){
          this.currVal = this.dataList[0].value
          this.$emit("defaultHandle", this.dataList[0]);
          this.$emit("getItem", this.dataList[0]);
        }
      }
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
