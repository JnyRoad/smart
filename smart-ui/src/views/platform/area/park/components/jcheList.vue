<template>
  <el-select
    v-model="currVal"
    placeholder="请选择"
    @change="handleChange"
    :disabled="myDisabled"
    clearable
    multiple
  >
    <template v-for="(item, index) in dataList">
      <el-option :label="item.label" :value="item.value" :key="index"></el-option>
    </template>
  </el-select>
</template>

<script>
import request from "axios";
import { validatenull } from "@/util/validate";
const baseUrl = "/platform/recruitment";
const getList = function() {
  return request({
    url: `${baseUrl}/getJche`,
    method: "GET"
  });
};
export default {
  name: "jcheSelect",
  data() {
    return {
      currVal: undefined,
      dataList: []
    };
  },
  props: {
    value: undefined,
    myDisabled: false
  },
  mounted() {
    this.currVal = this.value;
    this.getDataList();
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
      let res = await getList();
      res.data.data.forEach(element => {
        this.dataList.push({
          value: element.typeCode,
          label: element.typeName
        });
      });
    },
    handleChange(id) {
      return
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
